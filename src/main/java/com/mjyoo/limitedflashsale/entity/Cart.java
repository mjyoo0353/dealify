package com.mjyoo.limitedflashsale.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "cart")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "cart")
    private List<CartItem> cartItemList;
}
