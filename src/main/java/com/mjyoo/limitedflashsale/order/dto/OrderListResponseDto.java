package com.mjyoo.limitedflashsale.order.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class OrderListResponseDto {
    private List<OrderResponseDto> orderList;
    private Long totalOrderCount;

    public OrderListResponseDto(List<OrderResponseDto> orderInfoList, Long totalOrderCount) {
        this.orderList = orderInfoList;
        this.totalOrderCount = totalOrderCount;
    }
}
