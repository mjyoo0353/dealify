package com.mjyoo.limitedflashsale.cart.dto;

import com.mjyoo.limitedflashsale.cart.entity.CartProduct;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CartProductResponseDto {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal totalPrice;

    public CartProductResponseDto(CartProduct cartProduct) {
        this.productId = cartProduct.getProduct().getId();
        this.productName = cartProduct.getProduct().getName();
        this.quantity = cartProduct.getQuantity();
        this.totalPrice = cartProduct.getTotalPrice();
    }
}
