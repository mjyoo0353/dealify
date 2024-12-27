package com.mjyoo.limitedflashsale.order.repository;

import com.mjyoo.limitedflashsale.order.entity.Order;
import com.mjyoo.limitedflashsale.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    //주문 취소
    @Modifying
    @Query("update Order o set o.status = :status where o.id = :orderId")
    void updateOrderStatusToCanceled(@Param("orderId") Long orderId, @Param("status") OrderStatus status);

    //사용자별 주문 조회
    List<Order> findByUserId(Long id);
}