package com.mjyoo.limitedflashsale.order.scheduler;

import com.mjyoo.limitedflashsale.order.entity.Order;
import com.mjyoo.limitedflashsale.order.entity.OrderStatus;
import com.mjyoo.limitedflashsale.order.repository.OrderRepository;
import com.mjyoo.limitedflashsale.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    /**
     * 만료된 주문 처리 스케줄러
     * 실행 주기: 1분마다
     */
    @Scheduled(cron = "${scheduler.order.sync-time}")
    @Transactional
    public void processExpiredOrders() {
        log.info("Starting to process expired orders...");
        // 만료된 주문 조회
        List<Order> expiredOrders = orderRepository.findByStatusAndExpiryTimeBefore(OrderStatus.ORDER_PROCESSING, LocalDateTime.now());
        for (Order order : expiredOrders) {
            try {
                orderService.processSingleOrder(order);
                log.info("Expired order processed - orderId: {}", order.getId());
            } catch (Exception e) {
                log.error("Error processing expired order - orderId: {}", order.getId(), e);
            }
        }
    }

}
