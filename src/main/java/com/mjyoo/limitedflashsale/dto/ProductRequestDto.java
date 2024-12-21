package com.mjyoo.limitedflashsale.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductRequestDto {

    @NotBlank(message = "상품명을 입력해주세요.")
    private String name;

    @NotNull(message = "가격을 입력해주세요.")
    @Positive(message = "가격은 $1 이상이어야 합니다.")
    private BigDecimal price;

    @NotNull(message = "재고를 입력해주세요.")
    @Positive(message = "재고는 0보다 커야 합니다.")
    private int stock;

}