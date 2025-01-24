package com.mjyoo.limitedflashsale.flashsale.dto;

import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FlashSaleUpdateRequestDto {

    @NotNull(message = "Please enter the name.")
    private String name;

    @NotNull(message = "Please enter the start time.")
    private LocalDateTime startTime;

    @NotNull(message = "Please enter the end time.")
    private LocalDateTime endTime;

    private FlashSaleStatus status;

}
