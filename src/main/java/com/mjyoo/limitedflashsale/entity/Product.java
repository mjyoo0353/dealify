package com.mjyoo.limitedflashsale.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "product")
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    @Column(name = "sale_start_time", nullable = false)
    private LocalDateTime saleStartTime;

    @Column(name = "sale_end_time", nullable = false)
    private LocalDateTime saleEndTime;

    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItemList;

    @OneToMany(mappedBy = "product")
    private List<CartItem> cartItemList;

}
