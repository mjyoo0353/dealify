package com.mjyoo.limitedflashsale.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ProductListDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private String createdAt;
    private String modifiedAt;
}
