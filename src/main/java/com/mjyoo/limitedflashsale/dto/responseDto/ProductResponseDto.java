package com.mjyoo.limitedflashsale.dto.responseDto;


import com.mjyoo.limitedflashsale.entity.Product;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Getter
public class ProductResponseDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private int stock;
    private String createdAt;
    private String modifiedAt;

    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.stock = product.getInventory().getStock();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.createdAt = product.getCreatedAt().format(formatter);
        this.modifiedAt = product.getModifiedAt().format(formatter);
    }
}