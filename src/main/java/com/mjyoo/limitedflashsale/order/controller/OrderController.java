package com.mjyoo.limitedflashsale.order.controller;

import com.mjyoo.limitedflashsale.common.dto.ApiResponse;
import com.mjyoo.limitedflashsale.order.dto.OrderRequestDto;
import com.mjyoo.limitedflashsale.order.dto.OrderListResponseDto;
import com.mjyoo.limitedflashsale.order.dto.OrderResponseDto;
import com.mjyoo.limitedflashsale.auth.security.UserDetailsImpl;
import com.mjyoo.limitedflashsale.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    //주문 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<?>> getOrder(@PathVariable Long orderId,
                                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        OrderResponseDto order = orderService.getOrder(orderId, userDetails);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    //주문 리스트 조회
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<?>> getOrderList(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        OrderListResponseDto orderList = orderService.getOrderList(userDetails);
        return ResponseEntity.ok(ApiResponse.success(orderList));
    }

    //주문 생성
    @PostMapping()
    public ResponseEntity<ApiResponse<?>> createOrder(@Valid @RequestBody OrderRequestDto requestDto,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        OrderResponseDto order = orderService.createOrder(requestDto, userDetails);
        return ResponseEntity.ok(ApiResponse.success("주문이 생성되었습니다.", order));
    }

    //주문 취소
    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        orderService.cancelOrder(orderId, userDetails);
        return ResponseEntity.ok("주문이 취소되었습니다.");
    }
}
