package com.mjyoo.limitedflashsale.product.entity;

import com.mjyoo.limitedflashsale.cart.entity.CartProduct;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleProduct;
import com.mjyoo.limitedflashsale.product.dto.ProductRequestDto;
import com.mjyoo.limitedflashsale.common.Timestamped;
import com.mjyoo.limitedflashsale.order.entity.OrderProduct;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@Table(indexes = {@Index(name = "idx_product_deleted_id", columnList = "isDeleted,id")})
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
    private List<FlashSaleProduct> flashSaleProductList = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<OrderProduct> orderProductList = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<CartProduct> cartProductList = new ArrayList<>();

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
