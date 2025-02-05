package com.mjyoo.limitedflashsale.order.entity;

import com.mjyoo.limitedflashsale.payment.entity.Payment;
import com.mjyoo.limitedflashsale.common.entity.Timestamped;
import com.mjyoo.limitedflashsale.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Column(nullable = false)
    private LocalDateTime expiryTime;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Payment payment;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItemList = new ArrayList<>();

    @Builder
    public Order(OrderStatus status, LocalDateTime expiryTime, User user, Payment payment, List<OrderItem> orderItemList) {
        this.status = status;
        this.expiryTime = expiryTime;
        this.user = user;
        this.payment = payment;
        this.orderItemList = orderItemList;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem orderItem : orderItemList) {
            totalAmount = totalAmount.add(orderItem.getTotalAmount());
        }
        return totalAmount;
    }

    public void updatePayment(Payment payment) {
        this.payment = payment;
    }

    public void updateStatus(OrderStatus orderStatus) {
        this.status = orderStatus;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }

}
