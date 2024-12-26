package com.mjyoo.limitedflashsale.controller;

import com.mjyoo.limitedflashsale.dto.requestDto.ProductRequestDto;
import com.mjyoo.limitedflashsale.dto.responseDto.ProductListResponseDto;
import com.mjyoo.limitedflashsale.dto.responseDto.ProductResponseDto;
import com.mjyoo.limitedflashsale.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    //상품 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDto> getProduct(@PathVariable Long productId) {
        ProductResponseDto product = productService.getProduct(productId);
        return ResponseEntity.ok(product);

    }

    //상품 목록 조회 (deleted 여부에 따라 필터링)
    @GetMapping("/list")
    public ResponseEntity<ProductListResponseDto> getActiveProductList(@RequestParam(value = "deleted", required = false, defaultValue = "false") boolean deleted) {
        ProductListResponseDto productList = productService.getActiveProductList(deleted);
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
        productService.deleteProduct(productId); // 논리적 삭제
        return ResponseEntity.ok("상품이 삭제되었습니다.");
    }

}
