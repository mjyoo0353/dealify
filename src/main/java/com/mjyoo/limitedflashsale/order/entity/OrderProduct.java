package com.mjyoo.limitedflashsale.order.entity;

import com.mjyoo.limitedflashsale.product.entity.Product;
import com.mjyoo.limitedflashsale.common.Timestamped;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProduct extends Timestamped { //주문 이력 정보 저장

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; //주문 당시 가격 저장

    @Column(nullable = false)
    private int quantity; //주문한 상품의 수량

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount; //주문 당시 가격 * 수량

    @Column(nullable = false)
    private boolean isEventProduct; // 행사 상품 여부

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Builder
    public OrderProduct(String name, BigDecimal price, int quantity, BigDecimal totalAmount, boolean isEventProduct, Product product, Order order) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.isEventProduct = isEventProduct;
        this.product = product;
        this.order = order;
    }
}
