package com.mjyoo.limitedflashsale.cart.controller;

import com.mjyoo.limitedflashsale.cart.dto.CartRequestDto;
import com.mjyoo.limitedflashsale.cart.dto.CartListResponseDto;
import com.mjyoo.limitedflashsale.cart.dto.CartItemResponseDto;
import com.mjyoo.limitedflashsale.auth.security.UserDetailsImpl;
import com.mjyoo.limitedflashsale.cart.dto.CartUpdateRequestDto;
import com.mjyoo.limitedflashsale.cart.service.CartService;
import com.mjyoo.limitedflashsale.common.dto.ApiResponse;
import com.mjyoo.limitedflashsale.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CartController {

    private final CartService cartService;

    // 장바구니 목록 조회
    @GetMapping("/carts")
    public ResponseEntity<ApiResponse<?>> getCartList(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        CartListResponseDto cartList = cartService.getCartList(user);
        return ResponseEntity.ok(ApiResponse.success(cartList));
    }

    // 장바구니에 상품 추가
    @PostMapping("/cart")
    public ResponseEntity<ApiResponse<?>> addToCart(@RequestBody CartRequestDto requestDto,
                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        CartItemResponseDto cartProduct = cartService.addToCart(requestDto, user);
        return ResponseEntity.ok(ApiResponse.success(cartProduct));
    }

    // 장바구니 상품 수량 변경
    @PutMapping("/cart/{productId}")
    public ResponseEntity<ApiResponse<?>> updateCart(@PathVariable Long productId, @RequestBody CartUpdateRequestDto requestDto,
                                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        cartService.updateCart(productId, requestDto.getQuantity(), user);
        return ResponseEntity.ok(ApiResponse.success("상품 수량이 수정되었습니다."));
    }

    // 장바구니에서 상품 삭제
    @DeleteMapping("/cart/{productId}")
    public ResponseEntity<ApiResponse<?>> deleteFromCart(@PathVariable Long productId,
                                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        cartService.deleteFromCart(productId, user);
        return ResponseEntity.ok(ApiResponse.success("장바구니에서 상품이 삭제되었습니다."));
    }

}
