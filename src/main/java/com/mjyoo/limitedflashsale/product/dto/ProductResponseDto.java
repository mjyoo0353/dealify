package com.mjyoo.limitedflashsale.product.dto;


import com.mjyoo.limitedflashsale.product.entity.Product;
import lombok.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private int stock;
    private boolean isDeleted;
    private String createdAt;
    private String modifiedAt;

    @Builder
    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.stock = product.getInventory().getStock();
        this.isDeleted = product.isDeleted();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.createdAt = product.getCreatedAt().format(formatter);
        this.modifiedAt = product.getModifiedAt().format(formatter);
    }

}