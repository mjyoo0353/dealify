package com.mjyoo.limitedflashsale.product.controller;

import com.mjyoo.limitedflashsale.auth.security.UserDetailsImpl;
import com.mjyoo.limitedflashsale.common.dto.ApiResponse;
import com.mjyoo.limitedflashsale.product.dto.ProductRequestDto;
import com.mjyoo.limitedflashsale.product.dto.ProductListResponseDto;
import com.mjyoo.limitedflashsale.product.dto.ProductResponseDto;
import com.mjyoo.limitedflashsale.product.service.InventoryService;
import com.mjyoo.limitedflashsale.product.service.ProductService;
import com.mjyoo.limitedflashsale.user.entity.User;
import com.mjyoo.limitedflashsale.user.entity.UserRoleEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final InventoryService inventoryService;

    //재고 조회
    @GetMapping("/product/{productId}/stock")
    public ResponseEntity<ApiResponse<?>> getStock(@PathVariable Long productId) {
        int stockFromCache = inventoryService.getStockFromCache(productId);
        return ResponseEntity.ok(ApiResponse.success(stockFromCache));
    }

    //상품 조회
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProduct(@PathVariable Long productId) {
        ProductResponseDto product = productService.getProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    //상품 목록 조회
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<?>> getProducts(@RequestParam(value = "deleted", required = false, defaultValue = "false") boolean deleted,
                                                      @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                      @RequestParam(value = "cursor", required = false, defaultValue = "0") Long cursor,
                                                      @RequestParam(value = "size", defaultValue = "10") int size) {
        ProductListResponseDto productList;

        if (userDetails == null) {
            productList = productService.getActiveProductList(cursor, size);
        } else {
            User user = userDetails.getUser();
            // 관리자인 경우, 삭제된 상품도 조회 가능
            if (user.getRole() == UserRoleEnum.ADMIN) {
                productList = productService.getAllProductList(deleted, user, cursor, size);
            } else {
                productList = productService.getActiveProductList(cursor, size);
            }
        }
        return ResponseEntity.ok(ApiResponse.success(productList));
    }

    //상품 생성
    @PostMapping("/product")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequestDto requestDto,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        ProductResponseDto product = productService.createProduct(requestDto, requestDto.getStock(), user);
        return ResponseEntity.ok(ApiResponse.success("상품이 생성되었습니다.", product));
    }

    //상품 수정
    @PutMapping("/product/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId,
                                           @RequestBody ProductRequestDto requestDto,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        ProductResponseDto productResponseDto = productService.updateProduct(productId, requestDto, requestDto.getStock(), user);
        return ResponseEntity.ok(ApiResponse.success("상품이 수정되었습니다.", productResponseDto));
    }

    //상품 삭제
    @PatchMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<?>> deleteProduct(@PathVariable Long productId,
                                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        productService.deleteProduct(productId, user); // soft delete
        return ResponseEntity.ok(ApiResponse.success("상품이 삭제되었습니다."));
    }

}
