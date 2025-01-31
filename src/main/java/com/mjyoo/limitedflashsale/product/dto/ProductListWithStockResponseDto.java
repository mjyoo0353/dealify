package com.mjyoo.limitedflashsale.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProductListWithStockResponseDto {
    private List<ProductResponseDto> productInfoList;
    private Long totalProductCount;
    private Long lastCursor; // 위치
}
