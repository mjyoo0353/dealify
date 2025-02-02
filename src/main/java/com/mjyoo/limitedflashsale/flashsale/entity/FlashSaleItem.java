package com.mjyoo.limitedflashsale.flashsale.entity;

import com.mjyoo.limitedflashsale.common.entity.Timestamped;
import com.mjyoo.limitedflashsale.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "flash_sale_items")
public class FlashSaleItem extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal originalPrice; // 정가

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountRate; // 할인율

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountedPrice; // 할인된 가격

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlashSaleItemStatus status; // 행사 상품 상태

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flash_sale_id", nullable = false)
    private FlashSale flashSale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Builder
    public FlashSaleItem(BigDecimal originalPrice, BigDecimal discountRate, BigDecimal discountedPrice, FlashSaleItemStatus status, FlashSale flashSale, Product product) {
        this.originalPrice = originalPrice;
        this.discountRate = discountRate;
        this.discountedPrice = discountedPrice;
        this.status = status;
        this.flashSale = flashSale;
        this.product = product;
    }

    public void setStatus(FlashSaleItemStatus status) {
        this.status = status;
    }
}
