package com.mjyoo.limitedflashsale.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderRequestDto {
    @NotNull(message = "Please enter the product ID.")
    private Long productId;

    @NotNull(message = "Please enter the quantity.")
    @Min(value = 1, message = "The quantity must be at least 1.")
    private Integer quantity;
}
