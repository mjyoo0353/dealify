package com.mjyoo.limitedflashsale.product.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ProductListResponseDto {
    private List<ProductResponseDto> productInfoList;
    private Long totalProductCount;

    public ProductListResponseDto(List<ProductResponseDto> productInfoList, Long totalProductCount) {
        this.productInfoList = productInfoList;
        this.totalProductCount = totalProductCount;
    }
}
