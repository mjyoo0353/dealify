package com.mjyoo.limitedflashsale.event.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class FlashSaleRequestDto {
    private Long productId;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal discountRate; //20%는 0.20으로 입력

}
