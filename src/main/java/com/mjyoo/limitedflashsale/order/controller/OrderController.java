package com.mjyoo.limitedflashsale.order.controller;

import com.mjyoo.limitedflashsale.order.dto.OrderRequestDto;
import com.mjyoo.limitedflashsale.order.dto.OrderListResponseDto;
import com.mjyoo.limitedflashsale.order.dto.OrderResponseDto;
import com.mjyoo.limitedflashsale.auth.security.UserDetailsImpl;
import com.mjyoo.limitedflashsale.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long orderId,
                                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        OrderResponseDto order = orderService.getOrder(orderId, userDetails);
        return ResponseEntity.ok(order);
    }

    //주문 리스트 조회
    @GetMapping("/list")
    public ResponseEntity<OrderListResponseDto> getOrderList(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        OrderListResponseDto orderList = orderService.getOrderList(userDetails);
        return ResponseEntity.ok(orderList);
    }

    //주문 생성
    @PostMapping()
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto requestDto,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        OrderResponseDto order = orderService.createOrder(requestDto, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    //주문 취소
    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        orderService.cancelOrder(orderId, userDetails);
        return ResponseEntity.ok("주문이 취소되었습니다.");
    }
}
