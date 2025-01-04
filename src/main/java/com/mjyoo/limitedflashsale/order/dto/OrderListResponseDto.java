package com.mjyoo.limitedflashsale.order.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class OrderListResponseDto {
    private List<OrderResponseDto> orderList;
    private Long totalOrderCount;
    private Long LastCursor;

    public OrderListResponseDto(List<OrderResponseDto> orderInfoList, Long totalOrderCount, Long LastCursor) {
        this.orderList = orderInfoList;
        this.totalOrderCount = totalOrderCount;
        this.LastCursor =LastCursor;
    }
}
