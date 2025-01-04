package com.mjyoo.limitedflashsale.product.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ProductListResponseDto {
    private List<ProductResponseDto> productInfoList;
    private Long totalProductCount;
    private Long lastCursor; // 위치

    public ProductListResponseDto(List<ProductResponseDto> productInfoList, Long totalProductCount, Long cursor) {
        this.productInfoList = productInfoList;
        this.totalProductCount = totalProductCount;
        this.lastCursor = cursor;
    }
}
