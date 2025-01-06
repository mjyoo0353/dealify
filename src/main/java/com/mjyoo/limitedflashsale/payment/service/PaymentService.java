package com.mjyoo.limitedflashsale.payment.service;

import com.mjyoo.limitedflashsale.order.entity.Order;
import com.mjyoo.limitedflashsale.order.entity.OrderStatus;
import com.mjyoo.limitedflashsale.order.repository.OrderRepository;
import com.mjyoo.limitedflashsale.payment.dto.PaymentRequestDto;
import com.mjyoo.limitedflashsale.payment.dto.PaymentResponseDto;
import com.mjyoo.limitedflashsale.payment.entity.Payment;
import com.mjyoo.limitedflashsale.payment.entity.PaymentStatus;
import com.mjyoo.limitedflashsale.payment.repository.PaymentRepository;
import com.mjyoo.limitedflashsale.product.entity.Product;
import com.mjyoo.limitedflashsale.product.repository.ProductRepository;
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
    private final ProductRepository productRepository;

    // 결제 생성
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto requestDto, User user) {

        // 결제 검증
        validatePayment(requestDto.getOrderId(), user);

        Order order = orderRepository.findById(requestDto.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문 정보가 없습니다."));

        if (!order.getStatus().equals(OrderStatus.PAYMENT_PROCESSING)) {
            throw new IllegalArgumentException("결제할 수 없는 상태입니다.");
        }

        // 결제 시뮬레이션
        boolean success = simulatePayment();

        if (!success) {
            // 결제 실패 시 재고 복원
            restoreStock(order);
            order.updateStatus(OrderStatus.PAYMENT_FAILED);
        }

        PaymentStatus paymentStatus = success ? PaymentStatus.SUCCESS : PaymentStatus.CANCELLED;
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
    private void validatePayment(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 정보가 없습니다."));

        if (!isStockAvailable(order)) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }

        order.updateStatus(OrderStatus.PAYMENT_PROCESSING);
        orderRepository.save(order);
    }

    private void restoreStock(Order order) {
        log.warn("재고 복원 대상 주문 ID: " + order.getId());

        order.getOrderProductList().forEach(orderProduct -> {
            log.warn("재고 복원 중 상품 ID: " + orderProduct.getProduct().getId());
            Product product = productRepository.findById(orderProduct.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException("상품 정보가 없습니다."));
            log.warn("재고 복원: " + product.getName() + ", 수량: " + orderProduct.getQuantity());

            product.getInventory().restoreStock(orderProduct.getQuantity());
            productRepository.save(product);

            log.warn("복원된 재고 수량: " + product.getInventory().getStock());
        });
    }

    private boolean simulatePayment() {
        // 20% 확률로 결제 실패 시뮬레이션
        return Math.random() >= 0.2;
    }

    private boolean isStockAvailable(Order order) {
        return order.getOrderProductList().stream()
                .allMatch(orderProduct -> {
                    Product product = orderProduct.getProduct();
                    return product.getInventory().getStock() >= orderProduct.getQuantity();
                });
    }
}
