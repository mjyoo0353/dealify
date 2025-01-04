package com.mjyoo.limitedflashsale.order.repository;

import com.mjyoo.limitedflashsale.order.entity.Order;
import com.mjyoo.limitedflashsale.order.entity.OrderStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface OrderRepository extends JpaRepository<Order, Long> {
    //주문 취소
    @Modifying
    @Query("update Order o set o.status = :status where o.id = :orderId")
    void updateOrderStatusToCanceled(@Param("orderId") Long orderId, @Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.user.id = :id ORDER BY o.createdAt DESC")
    Slice<Order> findByUserId(Long id, PageRequest pageRequest);

    @Query("SELECT o FROM Order o WHERE o.user.id = :id AND o.id < :cursor ORDER BY o.createdAt DESC")
    Slice<Order> findByUserIdAndIdLessThan(Long id, Long cursor, PageRequest pageRequest);

    @Query("SELECT count(o) FROM Order o WHERE o.user.id = :userId")
    Long countAllByUserId(Long userId);
}