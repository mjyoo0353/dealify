package com.mjyoo.limitedflashsale.flashsale.dto;

import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleProduct;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class FlashSaleProductResponseDto {
    private Long productId;
    private BigDecimal originalPrice;
    private BigDecimal discountRate;
    private BigDecimal discountedPrice;
    private int initialStock; // 행사 시점의 재고

    public FlashSaleProductResponseDto(FlashSaleProduct flashSaleProduct) {
        this.productId = flashSaleProduct.getProduct().getId();
        this.originalPrice = flashSaleProduct.getOriginalPrice();
        this.discountRate = flashSaleProduct.getDiscountRate();
        this.discountedPrice = flashSaleProduct.getDiscountedPrice();
        this.initialStock = flashSaleProduct.getInitialStock();
    }
}
