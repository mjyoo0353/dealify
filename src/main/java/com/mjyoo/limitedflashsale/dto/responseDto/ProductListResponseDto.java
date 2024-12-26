package com.mjyoo.limitedflashsale.dto.responseDto;

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
