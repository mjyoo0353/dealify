package com.mjyoo.limitedflashsale.order.entity;

public enum OrderStatus {
    PENDING, // 초기 상태
    ORDER_PROCESSING, // 주문 처리 중
    ORDERED, // 주문 완료
    CANCELED, // 주문 취소
    PAYMENT_PROCESSING, // 결제 진행 중
    PAYMENT_FAILED, // 결제 실패
    REFUNDED, // 환불 완료
    PREPARING_FOR_SHIPMENT, // 배송 준비 중
    SHIPPED, // 배송 시작
    DELIVERED, PAYMENT_TIMEOUT; // 배송 완료
}
