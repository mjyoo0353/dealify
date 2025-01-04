package com.mjyoo.limitedflashsale.flashsale.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class FlashSaleListResponseDto {
    private List<FlashSaleResponseDto> flashSaleInfoList;

    public FlashSaleListResponseDto(List<FlashSaleResponseDto> flashSaleInfoList) {
        this.flashSaleInfoList = flashSaleInfoList;
    }
}
