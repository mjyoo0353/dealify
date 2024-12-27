package com.mjyoo.limitedflashsale.cart.repository;

import com.mjyoo.limitedflashsale.cart.entity.Cart;
import com.mjyoo.limitedflashsale.cart.entity.CartProduct;
import com.mjyoo.limitedflashsale.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartProductRepository extends JpaRepository<CartProduct, Long> {
    Optional<CartProduct> findByCartAndProduct(Cart cart, Product product);

    Optional<CartProduct> findByCartIdAndProductId(Long id, Long productId);
}