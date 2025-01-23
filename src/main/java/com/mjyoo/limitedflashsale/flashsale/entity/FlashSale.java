package com.mjyoo.limitedflashsale.flashsale.entity;

import com.mjyoo.limitedflashsale.common.entity.Timestamped;
import com.mjyoo.limitedflashsale.flashsale.dto.FlashSaleRequestDto;
import com.mjyoo.limitedflashsale.flashsale.dto.FlashSaleUpdateRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "flash_sales")
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
    private List<FlashSaleItem> flashSaleItemList = new ArrayList<>();

    @Builder
    public FlashSale(String name, LocalDateTime startTime, LocalDateTime endTime, FlashSaleStatus status, List<FlashSaleItem> flashSaleItemList) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.flashSaleItemList = flashSaleItemList;
    }

    public void update(FlashSaleUpdateRequestDto requestDto) {
        this.name = requestDto.getName();
        this.startTime = requestDto.getStartTime();
        this.endTime = requestDto.getEndTime();
        this.status = requestDto.getStatus();
    }

    public void updateStatus(FlashSaleStatus status) {
        this.status = status;
    }
}
