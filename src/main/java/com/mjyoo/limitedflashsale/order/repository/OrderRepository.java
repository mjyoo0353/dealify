package com.mjyoo.limitedflashsale.order.repository;

import com.mjyoo.limitedflashsale.order.entity.Order;
import com.mjyoo.limitedflashsale.order.entity.OrderStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface OrderRepository extends JpaRepository<Order, Long> {

    // 주문 목록 조회 - 페이징 no offset
    @Query(value = "SELECT o FROM Order o WHERE o.user.id = :id AND (:cursor = 0 OR o.id < :cursor) AND o.status = 'ORDERED' ORDER BY o.id DESC")
    Slice<Order> findByUserIdAndCursor(@Param("id") Long id,@Param("cursor") Long cursor, Pageable pageable);

    // 전체 주문 수 조회, 단순 카운트 쿼리로 성능 최적화
    @Query("SELECT count(o) FROM Order o WHERE o.user.id = :userId AND o.status = 'ORDERED'")
    Long countAllByUserId(Long userId);

    // 만료된 주문 조회 - 오더스케줄러에 의해 삭제됨
    @Query(value = "SELECT o FROM Order o WHERE o.status = :status AND o.expiryTime < :now")
    List<Order> findByStatusAndExpiryTimeBefore(OrderStatus status, LocalDateTime now);

    // 단건 주문 조회
    @Query("SELECT o FROM Order o JOIN FETCH o.orderItemList WHERE o.id = :orderId AND o.status = 'ORDERED'")
    Optional<Order> findByIdWithCheck(Long orderId);
}