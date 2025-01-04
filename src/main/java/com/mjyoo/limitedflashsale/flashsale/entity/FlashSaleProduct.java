package com.mjyoo.limitedflashsale.flashsale.entity;

import com.mjyoo.limitedflashsale.common.Timestamped;
import com.mjyoo.limitedflashsale.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FlashSaleProduct extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal originalPrice; // 정가

    @Column(nullable = false)
    private BigDecimal discountRate; // 할인율

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountedPrice; // 할인된 가격

    @Column(nullable = false)
    private int initialStock; // 행사 시점의 재고

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlashSaleProductStatus status; // 행사 상품 상태

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private FlashSale flashSale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Builder
    public FlashSaleProduct(BigDecimal originalPrice, BigDecimal discountRate, BigDecimal discountedPrice, int initialStock, FlashSaleProductStatus status, FlashSale flashSale, Product product) {
        this.originalPrice = originalPrice;
        this.discountRate = discountRate;
        this.discountedPrice = discountedPrice;
        this.initialStock = initialStock;
        this.status = status;
        this.flashSale = flashSale;
        this.product = product;
    }

    public void updateDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
        this.discountedPrice = this.originalPrice.subtract(this.originalPrice.multiply(discountRate));
    }


    public void setStatus(FlashSaleProductStatus status) {
        this.status = status;
    }
}
