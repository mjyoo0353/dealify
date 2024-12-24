package com.mjyoo.limitedflashsale.controller;

import com.mjyoo.limitedflashsale.dto.requestDto.ProductRequestDto;
import com.mjyoo.limitedflashsale.dto.responseDto.ProductResponseDto;
import com.mjyoo.limitedflashsale.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    //상품 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDto> getProduct(@PathVariable Long productId) {
        ProductResponseDto product = productService.getProduct(productId);
        return ResponseEntity.ok(product);

    }

    //상품 목록 조회
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getProductList() {
        List<ProductResponseDto> productList = productService.getProductList();
        return ResponseEntity.ok(productList);
    }

    //상품 생성
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto requestDto) {
        ProductResponseDto product = productService.createProduct(requestDto, requestDto.getStock());
        return ResponseEntity.ok(product);
    }

    //상품 수정
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponseDto> updateProduct(@PathVariable Long productId,
                                            @RequestBody ProductRequestDto requestDto) {
        ProductResponseDto product = productService.updateProduct(productId, requestDto, requestDto.getStock());
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok("상품 삭제 완료");
    }

}
