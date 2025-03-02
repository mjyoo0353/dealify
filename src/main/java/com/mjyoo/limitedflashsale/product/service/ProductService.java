package com.mjyoo.limitedflashsale.product.service;

import com.mjyoo.limitedflashsale.common.util.RedisKeys;
import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleItem;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleStatus;
import com.mjyoo.limitedflashsale.flashsale.repository.FlashSaleItemRepository;
import com.mjyoo.limitedflashsale.product.dto.*;
import com.mjyoo.limitedflashsale.product.entity.Product;
import com.mjyoo.limitedflashsale.product.repository.ProductRepository;
import com.mjyoo.limitedflashsale.user.entity.User;
import com.mjyoo.limitedflashsale.user.entity.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final InventoryService inventoryService;
    private final FlashSaleItemRepository flashSaleItemRepository;

    // 단건 Active 상품 조회 (재고 정보 포함)
    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(Long productId) {
        // FlashSale 상품 조회
        FlashSaleItem activeSaleItem = flashSaleItemRepository.findByProductIdAndFlashSaleStatus(productId, FlashSaleStatus.ACTIVE)
                .orElse(null);

        // FlashSale 상품인 경우에만 캐싱 로직 적용
        if (activeSaleItem != null) {
            // 캐시에서 상품 정보 조회
            String key = RedisKeys.getProductCacheKey(productId);
            ProductResponseDto cachedProduct = (ProductResponseDto) redisTemplate.opsForValue().get(key);

            if (cachedProduct != null) {
                return cachedProduct;
            }

            // Cache Miss 처리 - DB 조회 후 캐시 저장
            Product product = getProductById(productId);
            ProductResponseDto productInfo = new ProductResponseDto(product, activeSaleItem);

            // 행사 종료 시간까지 캐싱
            Duration ttl = Duration.between(LocalDateTime.now(), activeSaleItem.getFlashSale().getEndTime());
            redisTemplate.opsForValue().set(key, productInfo, ttl);

            return productInfo;
        }

        // 일반 상품은 DB에서 직접 조회
        Product product = getProductById(productId);
        return new ProductResponseDto(product);
    }

    /*public ProductResponseDto getProductDB(Long productId) {
        Product product = getProductById(productId);
        return new ProductResponseDto(product);
    }*/

    // Active 상품 목록 조회
    @Transactional(readOnly = true)
    public ProductListResponseDto getActiveProductList(Long cursor, int size) {
        // 페이징된 상품 목록 조회
        PageRequest pageRequest = PageRequest.of(0, size);
        Slice<Product> productList = getProductsByCursor(cursor, pageRequest);

        // 전체 상품 수 조회
        Long totalProducts = productRepository.countActiveProducts();

        List<ProductListDto> productInfoList = new ArrayList<>();
        for (Product product : productList) {
            BigDecimal price = product.getPrice();

            FlashSaleItem activeSaleItem = flashSaleItemRepository.findByProductIdAndFlashSaleStatus(product.getId(), FlashSaleStatus.ACTIVE)
                    .orElse(null);

            if (activeSaleItem != null) {
                price = activeSaleItem.getDiscountedPrice();
            }
            productInfoList.add(getProductListDto(product, price));
        }
        // 마지막 상품의 id를 cursor로 사용
        Long nextCursor = productList.hasNext() ? productInfoList.get(productInfoList.size() - 1).getId() : null;

        return new ProductListResponseDto(productInfoList, totalProducts, nextCursor);
    }

    // 상품 목록 조회 - 관리자용 (deleted 여부에 따라 필터링)
    @Transactional(readOnly = true)
    public ProductListWithStockResponseDto getAllProductList(boolean deleted, User user, Long cursor, int size) {
        //관리자 권한 확인
        checkAdminRole(user);

        PageRequest pageRequest = PageRequest.of(0, size);
        Slice<Product> productList;
        Long totalProducts;

        if (deleted) {
            productList = getDeletedProductsByCursor(cursor, pageRequest);
            totalProducts = productRepository.countDeletedProducts();
        } else {
            productList = getProductsByCursor(cursor, pageRequest);
            totalProducts = productRepository.countActiveProducts();
        }
        List<ProductResponseDto> ProductInfoList = new ArrayList<>();
        for (Product product : productList) {
            ProductInfoList.add(new ProductResponseDto(product));
        }

        // 마지막 상품의 id를 cursor로 사용
        Long nextCursor = productList.hasNext() ? ProductInfoList.get(ProductInfoList.size() - 1).getId() : null;

        return new ProductListWithStockResponseDto(ProductInfoList, totalProducts, nextCursor);
    }

    // 상품 생성
    public ProductResponseDto createProduct(ProductRequestDto requestDto, int stock, User user) {
        //관리자 권한 확인
        checkAdminRole(user);

        Product product = new Product(requestDto, stock);
        productRepository.save(product);

        // Redis 재고 정보 업데이트
        inventoryService.updateStockInRedis(product.getId(), stock);
        return new ProductResponseDto(product);
    }

    // 상품 수정
    @Transactional
    public ProductResponseDto updateProduct(Long productId, ProductRequestDto requestDto, int stock, User user) {
        //관리자 권한 확인
        checkAdminRole(user);

        Product product = getProductById(productId);
        product.update(requestDto);
        product.getInventory().updateStock(stock, product);

        try {
            // 재고 정보 캐시 업데이트
            inventoryService.updateStockInRedis(productId, stock);
        } catch (Exception e) {
            log.error("Cache update failed for productId: {}", productId, e);
        }
        return new ProductResponseDto(product);
    }

    // 상품 삭제
    public void deleteProduct(Long productId, User user) {
        //관리자 권한 확인
        checkAdminRole(user);

        Product product = getProductById(productId);
        product.updateToDelete(true); // soft delete 상태로 변경

        // Redis 재고 정보 삭제
        redisTemplate.delete(RedisKeys.getInventoryCacheKey(productId));
        productRepository.save(product);
    }

    private Slice<Product> getProductsByCursor(Long cursor, PageRequest pageRequest) {
        return productRepository.findActiveProductsAndCursor(cursor, pageRequest);
    }

    private Slice<Product> getDeletedProductsByCursor(Long cursor, PageRequest pageRequest) {
        return productRepository.findDeletedProductsAndCursor(cursor, pageRequest);
    }

    private Product getProductById(Long productId) {
        return productRepository.findByIdWithInventory(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private void checkAdminRole(User user) {
        if (user == null && !user.getRole().equals(UserRoleEnum.ADMIN)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    private ProductListDto getProductListDto(Product product, BigDecimal price) {
        return ProductListDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(price)
                .createdAt(product.getCreatedAt().toString())
                .modifiedAt(product.getModifiedAt().toString())
                .build();
    }

}
