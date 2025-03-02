package com.mjyoo.limitedflashsale.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProductListResponseDto {
    private List<ProductListDto> productInfoList;
    private Long totalProductCount;
    private Long lastCursor; // 위치

}
