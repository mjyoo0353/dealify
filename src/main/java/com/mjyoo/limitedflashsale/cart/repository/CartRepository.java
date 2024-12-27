package com.mjyoo.limitedflashsale.cart.repository;

import com.mjyoo.limitedflashsale.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long id);

}