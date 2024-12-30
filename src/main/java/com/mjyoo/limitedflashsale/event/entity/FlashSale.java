package com.mjyoo.limitedflashsale.event.entity;

import com.mjyoo.limitedflashsale.common.Timestamped;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlashSale extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FlashSaleStatus status;

    @OneToMany(mappedBy = "flashSale")
    private List<FlashSaleProduct> flashSaleProductList = new ArrayList<>();

}
