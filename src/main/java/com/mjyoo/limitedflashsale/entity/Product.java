package com.mjyoo.limitedflashsale.entity;

import com.mjyoo.limitedflashsale.dto.ProductRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; //상품 가격

    @Column(nullable = false)
    private int stock; //상품 재고

    @OneToOne(mappedBy = "product")
    private SaleEvent saleEvent;

    @OneToMany(mappedBy = "product")
    private List<OrderProduct> orderProductList = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<CartItem> cartItemList = new ArrayList<>();

    public Product(ProductRequestDto requestDto) {
        this.name = requestDto.getName();
        this.price = requestDto.getPrice();
        this.stock = requestDto.getStock();
    }

    public void update(ProductRequestDto requestDto) {
        this.name = requestDto.getName();
        this.price = requestDto.getPrice();
        this.stock = requestDto.getStock();
    }
}
