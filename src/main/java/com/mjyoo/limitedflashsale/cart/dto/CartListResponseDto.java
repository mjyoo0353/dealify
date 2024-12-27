package com.mjyoo.limitedflashsale.cart.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class CartListResponseDto {
    private List<CartProductResponseDto> cartProductList = new ArrayList<>();
    private long totalCartProducts;
    //private BigDecimal totalAmount;

}
