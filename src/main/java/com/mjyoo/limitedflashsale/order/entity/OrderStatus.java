package com.mjyoo.limitedflashsale.order.entity;

public enum OrderStatus {
    ORDER_PROCESSING, // 주문 처리 중
    PAYMENT_PROCESSING, // 결제 진행 중
    ORDERED, // 주문 완료
    CANCELED, // 주문 취소
    PAYMENT_FAILED, // 결제 실패
}
