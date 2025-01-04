package com.mjyoo.limitedflashsale.product.controller;

import com.mjyoo.limitedflashsale.auth.security.UserDetailsImpl;
import com.mjyoo.limitedflashsale.common.dto.ApiResponse;
import com.mjyoo.limitedflashsale.product.dto.ProductRequestDto;
import com.mjyoo.limitedflashsale.product.dto.ProductListResponseDto;
import com.mjyoo.limitedflashsale.product.dto.ProductResponseDto;
import com.mjyoo.limitedflashsale.product.service.ProductService;
import com.mjyoo.limitedflashsale.user.entity.User;
import com.mjyoo.limitedflashsale.user.entity.UserRoleEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    //상품 목록 조회 (Active 상품만 조회)
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<ProductListResponseDto>> getActiveProductList(@RequestParam(value = "cursor", required = false, defaultValue = "0") Long cursor,
                                                                                    @RequestParam(value = "size", defaultValue = "10") int size) {
        ProductListResponseDto productList = productService.getActiveProductList(cursor, size);
        return ResponseEntity.ok(ApiResponse.success(productList));
    }

    /**
     * 아래로는 관리자용 API
     */

    //상품 목록 조회 - 관리자용 (deleted 여부에 따라 필터링)
    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Secured(UserRoleEnum.Authority.ADMIN)
    public ResponseEntity<ApiResponse<?>> getAllProductList(@RequestParam(value = "deleted", required = false, defaultValue = "false") boolean deleted,
                                                                                    @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                                    @RequestParam(value = "cursor", required = false, defaultValue = "0") Long cursor,
                                                                                    @RequestParam(value = "size", defaultValue = "10") int size) {
        User user = userDetails.getUser();
        ProductListResponseDto productList = productService.getAllProductList(deleted, user, cursor, size);
        return ResponseEntity.ok(ApiResponse.success(productList));
    }

    //상품 생성
    @PostMapping("/admin/create")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequestDto requestDto,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        ProductResponseDto product = productService.createProduct(requestDto, requestDto.getStock(), user);
        return ResponseEntity.ok(ApiResponse.success("상품이 생성되었습니다.", product));
    }

    //상품 수정
    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId,
                                           @RequestBody ProductRequestDto requestDto,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        ProductResponseDto productResponseDto = productService.updateProduct(productId, requestDto, requestDto.getStock(), user);
        return ResponseEntity.ok(ApiResponse.success("상품이 수정되었습니다.", productResponseDto));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<?>> deleteProduct(@PathVariable Long productId,
                                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        productService.deleteProduct(productId, user); // 논리적 삭제
        return ResponseEntity.ok(ApiResponse.success("상품이 삭제되었습니다."));
    }

}
