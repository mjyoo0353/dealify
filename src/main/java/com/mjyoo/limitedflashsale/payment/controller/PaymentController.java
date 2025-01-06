package com.mjyoo.limitedflashsale.payment.controller;

import com.mjyoo.limitedflashsale.auth.security.UserDetailsImpl;
import com.mjyoo.limitedflashsale.common.dto.ApiResponse;
import com.mjyoo.limitedflashsale.payment.dto.PaymentRequestDto;
import com.mjyoo.limitedflashsale.payment.dto.PaymentResponseDto;
import com.mjyoo.limitedflashsale.payment.service.PaymentService;
import com.mjyoo.limitedflashsale.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 처리
    @PostMapping("/process/{orderId}")
    public ApiResponse<?> processPayment(@Valid @RequestBody PaymentRequestDto requestDto,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        PaymentResponseDto paymentResponseDto = paymentService.processPayment(requestDto, user);
        return ApiResponse.success("결제가 완료되었습니다.", paymentResponseDto);
    }
}
