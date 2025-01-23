package com.mjyoo.limitedflashsale.order.dto;

import com.mjyoo.limitedflashsale.order.entity.OrderItem;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderItemResponseDto {
    private String name;
    private int quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;

    public OrderItemResponseDto(OrderItem orderItem) {
        this.name = orderItem.getProduct().getName();
        this.quantity = orderItem.getQuantity();
        this.price = orderItem.getPrice();
        this.totalAmount = orderItem.getTotalAmount();
    }
}
