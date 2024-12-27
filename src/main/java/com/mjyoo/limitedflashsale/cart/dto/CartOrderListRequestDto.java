package com.mjyoo.limitedflashsale.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class CartOrderListRequestDto {

    @NotNull(message = "상품을 선택해주세요.")
    private List<CartRequestDto> cartProductList = new ArrayList<>();
}
