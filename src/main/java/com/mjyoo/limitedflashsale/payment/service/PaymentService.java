package com.mjyoo.limitedflashsale.payment.service;

import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.order.entity.Order;
import com.mjyoo.limitedflashsale.order.entity.OrderStatus;
import com.mjyoo.limitedflashsale.order.repository.OrderRepository;
import com.mjyoo.limitedflashsale.payment.dto.PaymentRequestDto;
import com.mjyoo.limitedflashsale.payment.dto.PaymentResponseDto;
import com.mjyoo.limitedflashsale.payment.entity.Payment;
import com.mjyoo.limitedflashsale.payment.entity.PaymentStatus;
import com.mjyoo.limitedflashsale.payment.repository.PaymentRepository;
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

    // 결제 생성
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto requestDto, User user) {
        // 결제 검증
        Order order = validatePayment(requestDto.getOrderId(), user);

        // 결제 시뮬레이션
        boolean success = simulatePayment();

        PaymentStatus paymentStatus = success ? PaymentStatus.SUCCESS : PaymentStatus.PAYMENT_FAILED;
        OrderStatus orderStatus = success ? OrderStatus.ORDERED : OrderStatus.PAYMENT_FAILED;

        // 결제 정보 생성
        Payment payment = Payment.builder()
                .order(order)
                .totalAmount(requestDto.getTotalAmount())
                .status(paymentStatus)
                .build();
        paymentRepository.save(payment);

        order.setPayment(payment); // 주문에 결제 정보 설정
        order.updateStatus(orderStatus);
        orderRepository.save(order);

        return PaymentResponseDto.builder()
                .orderId(order.getId())
                .status(order.getStatus().name())
                .build();
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
        
        if(!order.getStatus().equals(OrderStatus.ORDER_PROCESSING)){
            throw new CustomException(ErrorCode.INVALID_ORDER_STATUS);
        }

        order.updateStatus(OrderStatus.PAYMENT_PROCESSING);
        return orderRepository.save(order);
    }

    private boolean simulatePayment() {
        // 20% 확률로 결제 실패 시뮬레이션
        return Math.random() >= 0.2;
    }

}
