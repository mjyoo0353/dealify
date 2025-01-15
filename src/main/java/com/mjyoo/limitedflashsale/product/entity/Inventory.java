package com.mjyoo.limitedflashsale.product.entity;

import com.mjyoo.limitedflashsale.common.Timestamped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int stock;

    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public Inventory(int stock, Product product) {
        this.stock = stock;
        this.product = product;
    }

    public void updateStock(int stock, Product product) {
        if(stock == 0){
            throw new IllegalArgumentException("상품의 재고는 1개 이상이어야 합니다.");
        }
        this.stock = stock;
        this.product = product;
    }

    public void decreaseStock(int quantity) {
        if (quantity > this.stock) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.stock -= quantity;
    }

    public void restoreStock(int quantity) {
        this.stock += quantity;
    }

}
