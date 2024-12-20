package com.mjyoo.limitedflashsale.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
public class OrderProduct extends Timestamped { //주문 시점의 세부 정보를 저장하는 엔티티

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal subtotal; //주문한 상품의 소계 (주문 당시 가격 저장)

    @Column(nullable = false)
    private int quantity; //주문한 상품의 수량

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}
