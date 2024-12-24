package com.mjyoo.limitedflashsale.service;

import com.mjyoo.limitedflashsale.dto.requestDto.ProductRequestDto;
import com.mjyoo.limitedflashsale.dto.responseDto.ProductResponseDto;
import com.mjyoo.limitedflashsale.entity.Product;
import com.mjyoo.limitedflashsale.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private static final BigDecimal MIN_PRICE = new BigDecimal("1");

    // 상품 조회
    public ProductResponseDto getProduct(Long productId) {
        Product product = getProductOrThrow(productId);
        return new ProductResponseDto(product);
    }

    // 상품 목록 조회
    public List<ProductResponseDto> getProductList() {
        List<Product> productList = productRepository.findAll();
        return productList.stream().map(ProductResponseDto::new).toList();
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
            throw new IllegalArgumentException("상품의 가격은 $1 이상이어야 합니다.");
        }
        Product product = getProductOrThrow(productId);
        product.update(requestDto);
        product.getInventory().updateStock(stock, product);
        return new ProductResponseDto(product);
    }

    // 상품 삭제
    public void deleteProduct(Long productId) {
        Product product = getProductOrThrow(productId);
        productRepository.delete(product);
    }

    private Product getProductOrThrow(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다."));
        return product;
    }
}
