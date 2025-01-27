package com.mjyoo.limitedflashsale.flashsale.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class FlashSaleListResponseDto {
    private List<FlashSaleResponseDto> flashSaleInfoList;
    private Long nextCursor;

    public FlashSaleListResponseDto(List<FlashSaleResponseDto> flashSaleInfoList, Long nextCursor) {
        this.flashSaleInfoList = flashSaleInfoList;
        this.nextCursor = nextCursor;
    }
}
