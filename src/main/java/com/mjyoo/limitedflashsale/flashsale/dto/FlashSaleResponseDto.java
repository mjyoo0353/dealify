package com.mjyoo.limitedflashsale.flashsale.dto;

import com.mjyoo.limitedflashsale.flashsale.entity.FlashSale;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlashSaleResponseDto {
    private Long id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<FlashSaleProductResponseDto> flashSaleProductList;

    public FlashSaleResponseDto(FlashSale flashSale) {
        this.id = flashSale.getId();
        this.name = flashSale.getName();
        this.startTime = flashSale.getStartTime();
        this.endTime = flashSale.getEndTime();
        this.flashSaleProductList = flashSale.getFlashSaleProductList().stream()
                .map(FlashSaleProductResponseDto::new)
                .collect(Collectors.toList());
    }
}
