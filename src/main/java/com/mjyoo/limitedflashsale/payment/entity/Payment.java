package com.mjyoo.limitedflashsale.payment.entity;

import com.mjyoo.limitedflashsale.common.entity.Timestamped;
import com.mjyoo.limitedflashsale.order.entity.Order;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "payments")
public class Payment extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Builder
    public Payment(BigDecimal totalAmount, PaymentStatus status, Order order) {
        this.totalAmount = totalAmount;
        this.status = status;
        this.order = order;
    }

    public void setStatus(PaymentStatus paymentStatus) {
        this.status = paymentStatus;
    }
}

