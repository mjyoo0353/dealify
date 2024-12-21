package com.mjyoo.limitedflashsale.controller;

import com.mjyoo.limitedflashsale.dto.ProductRequestDto;
import com.mjyoo.limitedflashsale.dto.ProductResponseDto;
import com.mjyoo.limitedflashsale.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    //상품 조회
    @GetMapping("/{productId}")
    public ProductResponseDto getProduct(@PathVariable Long productId) {
        return productService.getProduct(productId);
    }

    //상품 목록 조회
    @GetMapping
    public List<ProductResponseDto> getProductList() {
        return productService.getProductList();
    }

    //상품 생성
    @PostMapping
    public ProductResponseDto createProduct(@Valid @RequestBody ProductRequestDto requestDto) {
        return productService.createProduct(requestDto, requestDto.getStock());
    }

    //상품 수정
    @PutMapping("/{productId}")
    public ProductResponseDto updateProduct(@PathVariable Long productId,
                                            @RequestBody ProductRequestDto requestDto) {
        return productService.updateProduct(productId, requestDto, requestDto.getStock());
    }

    @DeleteMapping("/{productId}")
    public void deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
    }

}
