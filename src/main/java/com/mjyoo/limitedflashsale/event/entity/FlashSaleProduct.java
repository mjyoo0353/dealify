package com.mjyoo.limitedflashsale.event.entity;

import com.mjyoo.limitedflashsale.common.Timestamped;
import com.mjyoo.limitedflashsale.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
}
