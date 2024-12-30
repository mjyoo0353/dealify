package com.mjyoo.limitedflashsale.event.controller;

import com.mjyoo.limitedflashsale.common.dto.ApiResponse;
import com.mjyoo.limitedflashsale.event.dto.FlashSaleRequestDto;
import com.mjyoo.limitedflashsale.event.service.FlashSaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/event")
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    // TODO 행사 조회

    // TODO 행사 목록 조회

    // 행사 생성
    @PostMapping("/admin/create")
    public ResponseEntity<ApiResponse<?>> createEvent(@Valid @RequestBody FlashSaleRequestDto requestDto) {
        Long eventId = flashSaleService.createEvent(requestDto);
        return ResponseEntity.ok(ApiResponse.success("행사가 생성되었습니다.", eventId));
    }

    // TODO 행사 수정

    // 행사 종료
    @PutMapping("/admin/end/{eventId}")
    public ResponseEntity<ApiResponse<?>> deleteEvent(@PathVariable Long eventId) {
        flashSaleService.deleteEvent(eventId);
        return ResponseEntity.ok(ApiResponse.success("행사가 종료되었습니다."));
    }
}
