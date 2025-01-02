package com.mjyoo.limitedflashsale.cart.entity;

import com.mjyoo.limitedflashsale.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartProduct> cartProductList = new ArrayList<>();

    public Cart(User user) {
        this.user = user;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartProduct cartProduct : cartProductList) {
            totalAmount = totalAmount.add(cartProduct.getTotalPrice());
        }
        return totalAmount;
    }
}
