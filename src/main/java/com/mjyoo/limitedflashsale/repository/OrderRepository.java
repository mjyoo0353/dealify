package com.mjyoo.limitedflashsale.repository;

import com.mjyoo.limitedflashsale.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}