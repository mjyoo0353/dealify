package com.mjyoo.limitedflashsale.flashsale.dto;

import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleItem;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class FlashSaleItemResponseDto {
    private Long productId;
    private BigDecimal originalPrice;
    private BigDecimal discountRate;
    private BigDecimal discountedPrice;

    public FlashSaleItemResponseDto(FlashSaleItem flashSaleItem) {
        this.productId = flashSaleItem.getProduct().getId();
        this.originalPrice = flashSaleItem.getOriginalPrice();
        this.discountRate = flashSaleItem.getDiscountRate();
        this.discountedPrice = flashSaleItem.getDiscountedPrice();
    }
}
