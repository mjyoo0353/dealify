package com.mjyoo.limitedflashsale.cart.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class CartUpdateRequestDto {

    @Min(value = 1, message = "Quantity must be greater than 1.")
    private int quantity;

    public CartUpdateRequestDto(int quantity) {
        this.quantity = quantity;
    }
}
