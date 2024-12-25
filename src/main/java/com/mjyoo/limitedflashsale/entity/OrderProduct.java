package com.mjyoo.limitedflashsale.entity;

import com.mjyoo.limitedflashsale.dto.requestDto.OrderRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

}
