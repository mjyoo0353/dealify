package com.mjyoo.limitedflashsale.controller;

import com.mjyoo.limitedflashsale.dto.ProductRequestDto;
import com.mjyoo.limitedflashsale.dto.ProductResponseDto;
import com.mjyoo.limitedflashsale.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
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
    public ProductResponseDto createProduct(@RequestBody ProductRequestDto requestDto) {
        return productService.createProduct(requestDto);
    }

    //상품 수정
    @PutMapping("/{productId}")
    public ProductResponseDto updateProduct(@PathVariable Long productId,
                                            @RequestBody ProductRequestDto requestDto) {
        return productService.updateProduct(productId, requestDto);
    }

    @DeleteMapping("/{productId}")
    public void deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
    }

}
