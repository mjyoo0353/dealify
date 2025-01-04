package com.mjyoo.limitedflashsale.product.service;

import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.product.dto.ProductRequestDto;
import com.mjyoo.limitedflashsale.product.dto.ProductListResponseDto;
import com.mjyoo.limitedflashsale.product.dto.ProductResponseDto;
import com.mjyoo.limitedflashsale.product.entity.Product;
import com.mjyoo.limitedflashsale.product.repository.ProductRepository;
import com.mjyoo.limitedflashsale.user.entity.User;
import com.mjyoo.limitedflashsale.user.entity.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private static final BigDecimal MIN_PRICE = new BigDecimal("1");

    // 단일 상품 조회 (삭제되지 않은 데이터)
    public ProductResponseDto getProduct(Long productId) {
        Product product = getProductById(productId);
        return new ProductResponseDto(product);
    }

    // 상품 목록 조회 (Active 상품만 조회)
    public ProductListResponseDto getActiveProductList(Long cursor, int size) {

        PageRequest pageRequest = PageRequest.of(0, size);
        Slice<Product> productList = getProductsByCursor(cursor, pageRequest);

        // 전체 상품 수 조회
        Long totalProducts = productRepository.countActiveProducts();

        List<ProductResponseDto> productInfoList = new ArrayList<>();
        for (Product product : productList) {
            productInfoList.add(new ProductResponseDto(product));
        }
        // 마지막 상품의 id를 cursor로 사용
        Long nextCursor = productList.hasNext() ? productInfoList.get(productInfoList.size() - 1).getId() : null;

        return new ProductListResponseDto(productInfoList, totalProducts, nextCursor);
    }

    // 상품 목록 조회 - 관리자용 (deleted 여부에 따라 필터링)
    public ProductListResponseDto getAllProductList(boolean deleted, User user, Long cursor, int size) {
        //관리자 권한 확인
        checkAdminRole(user);

        PageRequest pageRequest = PageRequest.of(0, size);
        Slice<Product> productList;
        Long totalProducts;

        if(deleted) {
            productList = productRepository.findDeletedProducts(cursor, pageRequest);
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

        return new ProductListResponseDto(ProductInfoList, totalProducts, nextCursor);
    }

    // 상품 생성
    public ProductResponseDto createProduct(ProductRequestDto requestDto, int stock, User user) {
        //관리자 권한 확인
        checkAdminRole(user);

        Product product = new Product(requestDto, stock);
        productRepository.save(product);
        return new ProductResponseDto(product);
    }

    // 상품 수정
    @Transactional
    public ProductResponseDto updateProduct(Long productId, ProductRequestDto requestDto, int stock, User user) {
        //관리자 권한 확인
        checkAdminRole(user);

        BigDecimal price = requestDto.getPrice();
        if (price.compareTo(MIN_PRICE) < 0) {
            throw new CustomException(ErrorCode.INVALID_PRICE);
        }
        Product product = getProductById(productId);
        product.update(requestDto);
        product.getInventory().updateStock(stock, product);
        return new ProductResponseDto(product);
    }

    // 상품 삭제
    public void deleteProduct(Long productId, User user) {
        //관리자 권한 확인
        checkAdminRole(user);

        Product product = getProductById(productId);
        productRepository.delete(product); // @SQLDelete가 실행됨
    }

    private Slice<Product> getProductsByCursor(Long cursor, PageRequest pageRequest) {
        Slice<Product> productList;
        if (cursor == null || cursor == 0) { // cursor가 없을 경우, 최신 상품 조회
            productList = productRepository.findAllActiveProducts(pageRequest);
        } else { // cursor가 있을 경우, cursor 기준으로 이전 상품 조회
            productList = productRepository.findAllActiveProductsAndIdLessThan(cursor, pageRequest);
        }
        return productList;
    }

    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .filter(product -> !product.isDeleted())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private void checkAdminRole(User user) {
        if (user == null && !user.getRole().equals(UserRoleEnum.ADMIN)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

}
