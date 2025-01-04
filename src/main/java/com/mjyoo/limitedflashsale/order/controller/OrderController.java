package com.mjyoo.limitedflashsale.order.controller;

import com.mjyoo.limitedflashsale.cart.dto.CartRequestDto;
import com.mjyoo.limitedflashsale.common.dto.ApiResponse;
import com.mjyoo.limitedflashsale.order.dto.OrderRequestDto;
import com.mjyoo.limitedflashsale.order.dto.OrderListResponseDto;
import com.mjyoo.limitedflashsale.order.dto.OrderResponseDto;
import com.mjyoo.limitedflashsale.auth.security.UserDetailsImpl;
import com.mjyoo.limitedflashsale.order.service.OrderService;
import com.mjyoo.limitedflashsale.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    //주문 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<?>> getOrder(@PathVariable Long orderId,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        OrderResponseDto order = orderService.getOrder(orderId, user);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    //주문 리스트 조회
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<?>> getOrderList(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                       @RequestParam(value = "cursor", required = false, defaultValue = "0") Long cursor,
                                                       @RequestParam(value = "size", defaultValue = "10") int size) {
        User user = userDetails.getUser();
        OrderListResponseDto orderList = orderService.getOrderList(user, cursor, size);
        return ResponseEntity.ok(ApiResponse.success(orderList));
    }

    //단일 상품 주문 생성
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createOrder(@Valid @RequestBody OrderRequestDto requestDto,
                                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        Long orderId = orderService.createOrder(requestDto, user);
        return ResponseEntity.ok(ApiResponse.success("주문이 완료되었습니다.", orderId));
    }

    @PostMapping("/create-from-cart")
    public ResponseEntity<ApiResponse<?>> createOrderFromCart(@RequestBody List<CartRequestDto> cartRequestDtos,
                                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        Long orderId = orderService.createOrderFromCart(cartRequestDtos, user);
        return ResponseEntity.ok(ApiResponse.success("주문이 생성되었습니다.", orderId));
    }

    //주문 취소
    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        orderService.cancelOrder(orderId, user);
        return ResponseEntity.ok("주문이 취소되었습니다.");
    }
}
