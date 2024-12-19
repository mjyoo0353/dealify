package com.mjyoo.limitedflashsale.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductRequestDto {

    @NotBlank(message = "상품명은 필수 항목입니다.")
    String name;

    @NotNull(message = "가격은 필수 항목입니다.")
    @Positive(message = "가격은 1000원 이상이어야 합니다.")
    BigDecimal price;

    @NotNull(message = "재고는 필수 항목입니다.")
    @Positive(message = "재고는 0보다 커야 합니다.")
    int stock;
}