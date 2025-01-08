package com.mjyoo.limitedflashsale.order.entity;

import com.mjyoo.limitedflashsale.payment.entity.Payment;
import com.mjyoo.limitedflashsale.common.Timestamped;
import com.mjyoo.limitedflashsale.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
public class Order extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status; //주문완료, 주문취소

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<OrderProduct> orderProductList = new ArrayList<>();

    @Builder
    public Order(OrderStatus status, User user, Payment payment, List<OrderProduct> orderProductList) {
        this.status = status;
        this.user = user;
        this.payment = payment;
        this.orderProductList = orderProductList;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderProduct orderProduct : orderProductList) {
            totalAmount = totalAmount.add(orderProduct.getTotalAmount());
        }
        return totalAmount;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public void updateStatus(OrderStatus orderStatus) {
        this.status = orderStatus;
    }
}
