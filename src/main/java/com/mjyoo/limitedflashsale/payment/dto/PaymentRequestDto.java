package com.mjyoo.limitedflashsale.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PaymentRequestDto {
    private Long orderId;
    private BigDecimal totalAmount;

    @Builder
    public PaymentRequestDto(Long orderId, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
    }
}
