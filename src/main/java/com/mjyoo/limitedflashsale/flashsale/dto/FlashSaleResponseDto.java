package com.mjyoo.limitedflashsale.flashsale.dto;

import com.mjyoo.limitedflashsale.flashsale.entity.FlashSale;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlashSaleResponseDto {
    private Long flashSaleId;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private FlashSaleStatus status;
    private List<FlashSaleProductResponseDto> flashSaleProductList;

    public FlashSaleResponseDto(FlashSale flashSale) {
        this.flashSaleId = flashSale.getId();
        this.name = flashSale.getName();
        this.startTime = flashSale.getStartTime();
        this.endTime = flashSale.getEndTime();
        this.status = flashSale.getStatus();
        this.flashSaleProductList = flashSale.getFlashSaleProductList().stream()
                .map(FlashSaleProductResponseDto::new)
                .collect(Collectors.toList());
    }
}
