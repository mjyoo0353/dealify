package com.mjyoo.limitedflashsale.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_time", nullable = false)
    private LocalDateTime paymentTime;

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;


}

