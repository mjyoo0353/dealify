package com.mjyoo.limitedflashsale.payment.service;

import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.order.entity.Order;
import com.mjyoo.limitedflashsale.order.entity.OrderStatus;
import com.mjyoo.limitedflashsale.order.repository.OrderRepository;
import com.mjyoo.limitedflashsale.payment.dto.PaymentResponseDto;
import com.mjyoo.limitedflashsale.payment.entity.Payment;
import com.mjyoo.limitedflashsale.payment.entity.PaymentStatus;
import com.mjyoo.limitedflashsale.payment.repository.PaymentRepository;
import com.mjyoo.limitedflashsale.product.service.InventoryService;
import com.mjyoo.limitedflashsale.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    // 결제 생성
    @Transactional
    public PaymentResponseDto processPayment(Long orderId, User user) {
        // 결제 검증
        Order order = validatePayment(orderId, user);

        try {
            // 결제 시뮬레이션
            boolean success = simulatePayment();

            PaymentStatus paymentStatus = success ? PaymentStatus.SUCCESS : PaymentStatus.PAYMENT_FAILED;
            OrderStatus orderStatus = success ? OrderStatus.ORDERED : OrderStatus.PAYMENT_FAILED;

            // 결제 정보 생성
            Payment payment = createPayment(order, paymentStatus);

            order.updatePayment(payment); // 주문에 결제 정보 설정
            order.updateStatus(orderStatus);
            orderRepository.save(order);

            if (success) { // 결제 성공 시 Redis -> DB 재고 감소
                inventoryService.decreaseStockToDB(order);
            } else { // 결제 실패 시 Redis 재고 복원
                inventoryService.restoreRedisStock(order);
            }

            return PaymentResponseDto.builder()
                    .orderId(order.getId())
                    .status(paymentStatus)
                    .build();

        } catch (Exception e) {
            // 기타 예외
            log.error("Unexpected error during payment - orderId: {}", orderId, e);
            handlePaymentFailure(orderId);
            throw e;
        }
    }

    // Redis에서 주문 정보 못찾을 경우 - DB 롤백, Redis 재고 복원
    private void handlePaymentFailure(Long orderId) {
        // DB에서 주문 정보 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // Redis 재고 복원
        inventoryService.restoreRedisStock(order);

        order.updateStatus(OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);

        log.info("Payment failure handled. Restored stock - orderId: {}", orderId);
    }

    // 결제 검증
    private Order validatePayment(Long orderId, User user) {
        // 주문 정보 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // 주문 정보와 사용자 정보 일치 여부 확인
        if (!order.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (!order.getStatus().equals(OrderStatus.ORDER_PROCESSING)) {
            throw new CustomException(ErrorCode.INVALID_ORDER_STATUS);
        }

        if (order.isExpired()) {
            throw new CustomException(ErrorCode.ORDER_EXPIRED);
        }

        order.updateStatus(OrderStatus.PAYMENT_PROCESSING);
        return orderRepository.save(order);
    }

    private Payment createPayment(Order order, PaymentStatus paymentStatus) {
        Payment payment = Payment.builder()
                .order(order)
                .totalAmount(order.getTotalAmount())
                .status(paymentStatus)
                .build();
        paymentRepository.save(payment);
        return payment;
    }

    private boolean simulatePayment() {
        // 20% 확률로 결제 실패 시뮬레이션
        return Math.random() >= 0.2;
        //return true;
    }

}
