package com.mjyoo.limitedflashsale.payment.dto;

import com.mjyoo.limitedflashsale.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;


@Getter
public class PaymentResponseDto {
    private Long orderId;
    private PaymentStatus status;

    @Builder
    public PaymentResponseDto(Long orderId, PaymentStatus status) {
        this.orderId = orderId;
        this.status = status;
    }
}
