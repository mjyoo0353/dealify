package com.mjyoo.limitedflashsale.product.entity;

import com.mjyoo.limitedflashsale.common.entity.Timestamped;
import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "inventories")
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
            throw new CustomException(ErrorCode.INVALID_STOCK);
        }
        this.stock = stock;
        this.product = product;
    }

    public void decreaseStock(int quantity) {
        if (quantity > this.stock) {
            throw new CustomException(ErrorCode.INSUFFICIENT_STOCK);
        }
        this.stock -= quantity;
    }

    public void restoreStock(int quantity) {
        this.stock += quantity;
    }

}
