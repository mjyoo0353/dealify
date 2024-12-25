package com.mjyoo.limitedflashsale.repository;

import com.mjyoo.limitedflashsale.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
}