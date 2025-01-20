package com.mjyoo.limitedflashsale.payment.controller;

import com.mjyoo.limitedflashsale.auth.security.UserDetailsImpl;
import com.mjyoo.limitedflashsale.common.dto.ApiResponse;
import com.mjyoo.limitedflashsale.payment.dto.PaymentResponseDto;
import com.mjyoo.limitedflashsale.payment.entity.PaymentStatus;
import com.mjyoo.limitedflashsale.payment.service.PaymentService;
import com.mjyoo.limitedflashsale.user.entity.User;
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
    public ApiResponse<?> processPayment(@PathVariable Long orderId,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        PaymentResponseDto response = paymentService.processPayment(orderId, user);
        String message = (response.getStatus() == PaymentStatus.SUCCESS) ? "결제가 완료되었습니다." : "결제에 실패했습니다.";
        return ApiResponse.success(message, response);
    }
}
