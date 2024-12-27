package com.mjyoo.limitedflashsale.product.controller;

import com.mjyoo.limitedflashsale.common.dto.ApiResponse;
import com.mjyoo.limitedflashsale.product.dto.ProductRequestDto;
import com.mjyoo.limitedflashsale.product.dto.ProductListResponseDto;
import com.mjyoo.limitedflashsale.product.dto.ProductResponseDto;
import com.mjyoo.limitedflashsale.product.service.ProductService;
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
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProduct(@PathVariable Long productId) {
        ProductResponseDto product = productService.getProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(product));

    }

    //상품 목록 조회 (deleted 여부에 따라 필터링)
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<ProductListResponseDto>> getActiveProductList(@RequestParam(value = "deleted", required = false, defaultValue = "false") boolean deleted) {
        ProductListResponseDto productList = productService.getActiveProductList(deleted);
        return ResponseEntity.ok(ApiResponse.success(productList));
    }

    //상품 생성
    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequestDto requestDto) {
        ProductResponseDto product = productService.createProduct(requestDto, requestDto.getStock());
        return ResponseEntity.ok(ApiResponse.success("상품이 생성되었습니다.", product));
    }

    //상품 수정
    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId,
                                            @RequestBody ProductRequestDto requestDto) {
        ProductResponseDto productResponseDto = productService.updateProduct(productId, requestDto, requestDto.getStock());
        return ResponseEntity.ok(ApiResponse.success("상품이 수정되었습니다.", productResponseDto));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<?>> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId); // 논리적 삭제
        return ResponseEntity.ok(ApiResponse.success("상품이 삭제되었습니다."));
    }

}
