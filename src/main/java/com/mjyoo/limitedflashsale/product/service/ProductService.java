package com.mjyoo.limitedflashsale.product.service;

import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.product.dto.ProductRequestDto;
import com.mjyoo.limitedflashsale.product.dto.ProductListResponseDto;
import com.mjyoo.limitedflashsale.product.dto.ProductResponseDto;
import com.mjyoo.limitedflashsale.product.entity.Product;
import com.mjyoo.limitedflashsale.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
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

    // 상품 목록 조회 (deleted 여부 필터링)
    public ProductListResponseDto getActiveProductList(boolean deleted) {
        List<Product> productList;
        Long totalProducts;

        if(deleted) {
            productList = productRepository.findDeletedProducts();
            totalProducts = productRepository.countDeletedProducts();
        } else {
            productList = productRepository.findAllActive();
            totalProducts = productRepository.countActiveProducts();
        }
        List<ProductResponseDto> ProductInfoList = new ArrayList<>();
        for (Product product : productList) {
            ProductInfoList.add(new ProductResponseDto(product));
        }
        return new ProductListResponseDto(ProductInfoList, totalProducts);
    }

    // 상품 생성
    public ProductResponseDto createProduct(ProductRequestDto requestDto, int stock) {
        Product product = new Product(requestDto, stock);
        productRepository.save(product);
        return new ProductResponseDto(product);
    }

    // 상품 수정
    @Transactional
    public ProductResponseDto updateProduct(Long productId, ProductRequestDto requestDto, int stock) {
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
    public void deleteProduct(Long productId) {
        Product product = getProductById(productId);
        productRepository.delete(product); // @SQLDelete가 실행됨
    }

    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .filter(product -> !product.isDeleted())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

}
