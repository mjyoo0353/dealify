package com.mjyoo.limitedflashsale.product.dto;


import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleItem;
import com.mjyoo.limitedflashsale.product.entity.Product;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private int stock;
    private boolean isOnSale;
    private String endTime;
    private String createdAt;
    private String modifiedAt;

    //일반 상품용
    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.isOnSale = false;
        this.stock = product.getInventory().getStock();
        this.createdAt = String.valueOf(product.getCreatedAt());
        this.modifiedAt = String.valueOf(product.getModifiedAt());
    }

    //행사 상품용
    public ProductResponseDto(Product product, FlashSaleItem saleItem) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = saleItem.getDiscountedPrice();
        this.isOnSale = true;
        this.stock = product.getInventory().getStock();
        this.endTime = String.valueOf(saleItem.getFlashSale().getEndTime());
        this.createdAt = String.valueOf(product.getCreatedAt());
        this.modifiedAt = String.valueOf(product.getModifiedAt());
    }

}