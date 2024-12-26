package com.mjyoo.limitedflashsale.order.entity;

import com.mjyoo.limitedflashsale.payment.Payment;
import com.mjyoo.limitedflashsale.common.Timestamped;
import com.mjyoo.limitedflashsale.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status; //주문완료, 주문취소

    /*@Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;*/

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "payment_id", nullable = true)
    private Payment payment;

    @OneToMany(mappedBy = "order")
    private List<OrderProduct> orderProductList = new ArrayList<>();

}
