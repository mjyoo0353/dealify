package com.mjyoo.limitedflashsale.dto.responseDto;

import com.mjyoo.limitedflashsale.entity.OrderProduct;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderProductResponseDto {
    private String name;
    private int quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;

    public OrderProductResponseDto(OrderProduct orderProduct) {
        this.name = orderProduct.getProduct().getName();
        this.quantity = orderProduct.getQuantity();
        this.price = orderProduct.getPrice();
        this.totalAmount = orderProduct.getTotalAmount();
    }
}
