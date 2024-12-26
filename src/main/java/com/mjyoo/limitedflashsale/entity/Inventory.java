package com.mjyoo.limitedflashsale.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@Entity
@NoArgsConstructor
public class Inventory extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int stock;

    @OneToOne(mappedBy = "inventory")
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

    public void decreaseStock(Integer quantity) {
        if (quantity > this.stock) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.stock -= quantity;
    }
}
