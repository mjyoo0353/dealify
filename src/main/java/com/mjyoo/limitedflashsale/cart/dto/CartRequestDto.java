package com.mjyoo.limitedflashsale.cart.dto;

import lombok.Getter;

@Getter
public class CartRequestDto {
    private Long productId;
    private int quantity;

    public CartRequestDto(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}
