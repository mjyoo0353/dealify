package com.mjyoo.limitedflashsale.product.dto;


import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductRequestDto {

    @NotBlank(message = "Please enter the product name.")
    private String name;

    @NotNull(message = "Please enter the price.")
    @DecimalMin(value = "1.00", message = "The price must be at least $1.")
    private BigDecimal price;

    @NotNull(message = "Please enter the stock.")
    @Min(value = 1, message = "The stock must be at least 1.")
    private int stock;

    @Builder
    public ProductRequestDto(String name, BigDecimal price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
}