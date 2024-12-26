package com.mjyoo.limitedflashsale.payment;

import com.mjyoo.limitedflashsale.common.Timestamped;
import com.mjyoo.limitedflashsale.order.entity.Order;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
public class Payment extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status;

    @OneToOne(mappedBy = "payment")
    private Order order;


}

