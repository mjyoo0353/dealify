package com.mjyoo.limitedflashsale.order.repository;

import com.mjyoo.limitedflashsale.order.entity.Order;
import com.mjyoo.limitedflashsale.order.entity.OrderStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = "SELECT o FROM Order o JOIN FETCH o.orderProductList op WHERE o.user.id = :id ORDER BY o.id DESC",
            countQuery = "SELECT COUNT(o) FROM Order o WHERE o.user.id = :id")
    Slice<Order> findByUserId(Long id, PageRequest pageRequest);

    @Query(value = "SELECT o FROM Order o JOIN FETCH o.orderProductList op WHERE o.user.id = :id AND o.id < :cursor ORDER BY o.id DESC",
            countQuery = "SELECT COUNT(o) FROM Order o WHERE o.user.id = :id")
    Slice<Order> findByUserIdAndIdLessThan(Long id, Long cursor, PageRequest pageRequest);

    @Query("SELECT count(o) FROM Order o WHERE o.user.id = :userId")
    Long countAllByUserId(Long userId);

    @Query("SELECT o FROM Order o JOIN o.orderProductList op WHERE op.product.id = :productId")
    List<Order> findAllByProductId(Long productId);

    List<Order> findByStatusAndExpiryTimeBefore(OrderStatus status, LocalDateTime time);
}