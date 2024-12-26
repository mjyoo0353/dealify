package com.mjyoo.limitedflashsale.product.repository;

import com.mjyoo.limitedflashsale.order.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
}