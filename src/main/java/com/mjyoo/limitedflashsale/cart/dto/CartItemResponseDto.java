package com.mjyoo.limitedflashsale.cart.dto;

import com.mjyoo.limitedflashsale.cart.entity.CartItem;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CartItemResponseDto {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal totalPrice;

    public CartItemResponseDto(CartItem cartItem) {
        this.productId = cartItem.getProduct().getId();
        this.productName = cartItem.getProduct().getName();
        this.quantity = cartItem.getQuantity();
        this.totalPrice = cartItem.getTotalPrice();
    }
}
