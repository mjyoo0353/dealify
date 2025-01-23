package com.mjyoo.limitedflashsale.flashsale.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FlashSaleItemRequestDto {

    @NotNull
    private Long productId;

    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    private BigDecimal discountRate;

    @NotNull
    @Min(1)
    private Integer initialStock;
}
