package com.mjyoo.limitedflashsale.repository;

import com.mjyoo.limitedflashsale.entity.Order;
import com.mjyoo.limitedflashsale.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Modifying
    @Query("update Order o set o.status = :status where o.id = :orderId")
    void updateOrderStatusToCanceled(@Param("orderId") Long orderId, @Param("status") OrderStatus status);

}