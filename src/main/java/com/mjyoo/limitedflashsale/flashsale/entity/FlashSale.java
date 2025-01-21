package com.mjyoo.limitedflashsale.flashsale.entity;

import com.mjyoo.limitedflashsale.common.entity.Timestamped;
import com.mjyoo.limitedflashsale.flashsale.dto.FlashSaleRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FlashSale extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FlashSaleStatus status;

    @OneToMany(mappedBy = "flashSale", cascade = CascadeType.ALL)
    private List<FlashSaleProduct> flashSaleProductList = new ArrayList<>();

    @Builder
    public FlashSale(String name, LocalDateTime startTime, LocalDateTime endTime, FlashSaleStatus status, List<FlashSaleProduct> flashSaleProductList) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.flashSaleProductList = flashSaleProductList;
    }

    public void update(FlashSaleRequestDto requestDto) {
        this.name = requestDto.getName();
        this.startTime = requestDto.getStartTime();
        this.endTime = requestDto.getEndTime();
    }

    public void updateStatus(FlashSaleStatus status) {
        this.status = status;
    }
}
