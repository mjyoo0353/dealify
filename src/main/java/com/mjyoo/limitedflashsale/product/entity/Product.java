package com.mjyoo.limitedflashsale.product.entity;

import com.mjyoo.limitedflashsale.cart.entity.CartItem;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleItem;
import com.mjyoo.limitedflashsale.product.dto.ProductRequestDto;
import com.mjyoo.limitedflashsale.common.entity.Timestamped;
import com.mjyoo.limitedflashsale.order.entity.OrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "products")
public class Product extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; //상품 가격

    @Column(nullable = false)
    private boolean isDeleted = false;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)
    private Inventory inventory;

    @OneToMany(mappedBy = "product")
    private List<FlashSaleItem> flashSaleItemList = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItemList = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<CartItem> cartItemList = new ArrayList<>();

    @Builder
    public Product(ProductRequestDto requestDto, int stock) {
        this.name = requestDto.getName();
        this.price = requestDto.getPrice();
        this.inventory = new Inventory(stock, this);
    }

    public void update(ProductRequestDto requestDto) {
        this.name = requestDto.getName();
        this.price = requestDto.getPrice();
    }

    public void updateToDelete(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

}
