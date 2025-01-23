package com.mjyoo.limitedflashsale.order.repository;

import com.mjyoo.limitedflashsale.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}