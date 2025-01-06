package com.mjyoo.limitedflashsale.cart.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
public class CartListResponseDto {
    private List<CartProductResponseDto> cartProductList = new ArrayList<>();
    private long totalCartProducts;
    private BigDecimal totalAmount;

    @Builder
    public CartListResponseDto(List<CartProductResponseDto> cartProductList, long totalCartProducts, BigDecimal totalAmount) {
        this.cartProductList = cartProductList;
        this.totalCartProducts = totalCartProducts;
        this.totalAmount = totalAmount;
    }
}
