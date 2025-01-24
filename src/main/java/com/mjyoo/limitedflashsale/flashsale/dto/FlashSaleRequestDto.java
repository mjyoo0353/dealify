package com.mjyoo.limitedflashsale.flashsale.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class FlashSaleRequestDto {

    @NotNull(message = "Please enter the name.")
    private String name;

    @NotNull(message = "Please enter the start time.")
    private LocalDateTime startTime;

    @NotNull(message = "Please enter the end time.")
    private LocalDateTime endTime;

}
