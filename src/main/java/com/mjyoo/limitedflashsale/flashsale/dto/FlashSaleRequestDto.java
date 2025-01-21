package com.mjyoo.limitedflashsale.flashsale.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class FlashSaleRequestDto {

    @NotNull(message = "행사 이름을 입력해주세요.")
    private String name;

    @NotNull(message = "시작 시간을 입력해주세요.")
    private LocalDateTime startTime;

    @NotNull(message = "종료 시간을 입력해주세요.")
    private LocalDateTime endTime;

}
