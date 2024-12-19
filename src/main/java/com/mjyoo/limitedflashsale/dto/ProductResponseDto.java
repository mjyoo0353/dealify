package com.mjyoo.limitedflashsale.dto;


import com.mjyoo.limitedflashsale.entity.Product;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class ProductResponseDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private int stock;

    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.stock = product.getStock();
    }
}