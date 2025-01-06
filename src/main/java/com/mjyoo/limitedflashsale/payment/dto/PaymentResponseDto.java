package com.mjyoo.limitedflashsale.payment.dto;

import lombok.Builder;
import lombok.Getter;


@Getter
public class PaymentResponseDto {
    private Long orderId;
    private String status;

    @Builder
    public PaymentResponseDto(Long orderId, String status) {
        this.orderId = orderId;
        this.status = status;
    }
}
