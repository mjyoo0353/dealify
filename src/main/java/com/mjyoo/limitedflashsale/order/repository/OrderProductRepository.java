package com.mjyoo.limitedflashsale.order.repository;

import com.mjyoo.limitedflashsale.order.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
}