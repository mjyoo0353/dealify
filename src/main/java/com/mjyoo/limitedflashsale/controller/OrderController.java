package com.mjyoo.limitedflashsale.controller;

import com.mjyoo.limitedflashsale.dto.requestDto.OrderRequestDto;
import com.mjyoo.limitedflashsale.dto.responseDto.OrderResponseDto;
import com.mjyoo.limitedflashsale.security.UserDetailsImpl;
import com.mjyoo.limitedflashsale.service.OrderService;
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

    //주문 생성
    @PostMapping()
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto requestDto,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        OrderResponseDto order = orderService.createOrder(requestDto, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    //주문 취소
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        orderService.cancelOrder(orderId, userDetails);
        return ResponseEntity.ok("주문이 취소되었습니다.");

    }
}
