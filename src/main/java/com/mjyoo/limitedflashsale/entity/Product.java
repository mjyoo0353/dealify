package com.mjyoo.limitedflashsale.entity;

import com.mjyoo.limitedflashsale.dto.requestDto.ProductRequestDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE product SET is_deleted = true WHERE id = ?") // 삭제 시 is_deleted를 true로 변경
public class Product extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; //상품 가격

    @Column(nullable = false)
    private boolean isDeleted = false;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "inventory_id")
    private Inventory inventory;

    @OneToMany(mappedBy = "product")
    private List<OrderProduct> orderProductList = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<CartProduct> cartProductList = new ArrayList<>();

    public Product(ProductRequestDto requestDto, int stock) {
        this.name = requestDto.getName();
        this.price = requestDto.getPrice();
        this.inventory = new Inventory(stock, this);
    }

    public void update(ProductRequestDto requestDto) {
        this.name = requestDto.getName();
        this.price = requestDto.getPrice();
    }

}
