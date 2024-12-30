package com.mjyoo.limitedflashsale.order.dto;

import com.mjyoo.limitedflashsale.order.entity.Order;
import com.mjyoo.limitedflashsale.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {

    private Long id;
    private OrderStatus status; //주문완료, 주문취소
    private BigDecimal totalAmount;
    private List<OrderProductResponseDto> orderProductList; // 주문 상품 리스트

    public OrderResponseDto(Order order) {
        this.id = order.getId();
        this.status = order.getStatus();
        this.totalAmount = order.getTotalAmount();
        this.orderProductList = order.getOrderProductList().stream()
                .map(OrderProductResponseDto::new)
                .collect(Collectors.toList());
    }
}
