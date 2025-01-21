package com.mjyoo.limitedflashsale.flashsale.controller;

import com.mjyoo.limitedflashsale.auth.security.UserDetailsImpl;
import com.mjyoo.limitedflashsale.common.dto.ApiResponse;
import com.mjyoo.limitedflashsale.flashsale.dto.*;
import com.mjyoo.limitedflashsale.flashsale.service.FlashSaleService;
import com.mjyoo.limitedflashsale.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    // 행사 조회
    @GetMapping("/flash-sale/{flashSaleId}")
    public ResponseEntity<ApiResponse<?>> getFlashSaleDetail(@PathVariable Long flashSaleId) {
        FlashSaleResponseDto flashSaleDetail = flashSaleService.getFlashSaleDetail(flashSaleId);
        return ResponseEntity.ok(ApiResponse.success(flashSaleDetail));
    }

    // 행사 목록 조회
    @GetMapping("/flash-sales")
    public ResponseEntity<ApiResponse<?>> getFlashSaleList() {
        FlashSaleListResponseDto flashSaleList = flashSaleService.getFlashSaleList();
        return ResponseEntity.ok(ApiResponse.success(flashSaleList));
    }

    // 행사 생성
    @PostMapping("/flash-sale")
    public ResponseEntity<ApiResponse<?>> createFlashSale(@Valid @RequestBody FlashSaleRequestDto requestDto,
                                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        Long flashSaleId = flashSaleService.createFlashSale(requestDto, user);
        return ResponseEntity.ok(ApiResponse.success("행사가 생성되었습니다.", flashSaleId));
    }

    // 행사 상품 추가
    @PostMapping("/flash-sale/{flashSaleId}/products")
    public ResponseEntity<ApiResponse<?>> addFlashSaleProduct(@PathVariable Long flashSaleId,
                                                              @Valid @RequestBody FlashSaleProductRequestDto requestDto,
                                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        flashSaleService.addFlashSaleProduct(flashSaleId, requestDto, user);
        return ResponseEntity.ok(ApiResponse.success("행사 상품이 추가되었습니다."));
    }

    // 행사 수정
    @PutMapping("/flash-sale/{flashSaleId}")
    public ResponseEntity<ApiResponse<?>> updateFlashSale(@PathVariable Long flashSaleId,
                                                          @Valid @RequestBody FlashSaleRequestDto requestDto,
                                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        FlashSaleResponseDto flashSaleResponseDto = flashSaleService.updateFlashSale(flashSaleId, requestDto, user);
        return ResponseEntity.ok(ApiResponse.success("행사가 수정되었습니다.", flashSaleResponseDto));
    }

    // 행사 삭제
    @DeleteMapping("/flash-sale/{flashSaleId}")
    public ResponseEntity<ApiResponse<?>> deleteFlashSale(@PathVariable Long flashSaleId,
                                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        flashSaleService.deleteFlashSale(flashSaleId, user);
        return ResponseEntity.ok(ApiResponse.success("행사가 삭제되었습니다."));
    }

    // 행사 오픈
    @PutMapping("/flash-sale/open/{flashSaleId}")
    public ResponseEntity<ApiResponse<?>> openFlashSale(@PathVariable Long flashSaleId,
                                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        flashSaleService.openFlashSale(flashSaleId, user);
        return ResponseEntity.ok(ApiResponse.success("행사가 오픈되었습니다."));
    }

    // 행사 종료
    @PutMapping("/flash-sale/close/{flashSaleId}")
    public ResponseEntity<ApiResponse<?>> closeFlashSale(@PathVariable Long flashSaleId,
                                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        flashSaleService.closeFlashSale(flashSaleId, user);
        return ResponseEntity.ok(ApiResponse.success("행사가 종료되었습니다."));
    }
}
