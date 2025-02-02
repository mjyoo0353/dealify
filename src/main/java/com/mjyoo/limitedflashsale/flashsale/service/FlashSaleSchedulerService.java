package com.mjyoo.limitedflashsale.flashsale.service;

import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.common.util.RedisKeys;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSale;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleItem;
import com.mjyoo.limitedflashsale.flashsale.entity.FlashSaleStatus;
import com.mjyoo.limitedflashsale.flashsale.repository.FlashSaleRepository;
import com.mjyoo.limitedflashsale.product.dto.ProductResponseDto;
import io.lettuce.core.RedisConnectionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlashSaleSchedulerService {

    private final FlashSaleRepository flashSaleRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    //행사 시작 (자동 스케줄링용)
    @Retryable(value = {RedisConnectionException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Transactional
    public void openFlashSale(Long flashSaleId) {
        // 상태 검증
        FlashSale flashSale = findFlashSale(flashSaleId);

        // 행사 시작 시간이 아닌 경우 예외 처리
        if (flashSale.getStatus() != FlashSaleStatus.SCHEDULED && !flashSale.getStartTime().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.FLASH_SALE_NOT_STARTED);
        }
        // DB 상태 업데이트
        updateFlashSaleStatus(flashSale, FlashSaleStatus.ACTIVE);

        // 상품 조회 시, 할인된 price 적용을 위해 캐시 갱신
        for(FlashSaleItem flashSaleItem : flashSale.getFlashSaleItemList()) {
            updateFlashSaleProductCache(flashSaleItem);
        }
    }

    //행사 종료 (자동 스케줄링용)
    @Retryable(value = {RedisConnectionException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Transactional
    public void closeFlashSale(Long flashSaleId) {
        // 상태 검증
        FlashSale flashSale = findFlashSale(flashSaleId);

        if (flashSale.getStatus() != FlashSaleStatus.ACTIVE && !flashSale.getEndTime().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.FLASH_SALE_NOT_ENDED);
        }
        // DB 상태 업데이트
        updateFlashSaleStatus(flashSale, FlashSaleStatus.ENDED);

        // 상품 조회 시, original price 적용을 위해 캐시 갱신
        for(FlashSaleItem flashSaleItem : flashSale.getFlashSaleItemList()) {
            updateFlashSaleProductCache(flashSaleItem);
        }
    }

    // 현재 시점에 오픈해야할 할인행사 목록 조회
    @Transactional(readOnly = true)
    public List<Long> getScheduledFlashSales() {
        LocalDateTime now = LocalDateTime.now();
        List<FlashSale> scheduledSales = flashSaleRepository.findByStatusAndStartTimeLessThanEqual(FlashSaleStatus.SCHEDULED, now);

        List<Long> scheduledSaleIds = new ArrayList<>();
        for (FlashSale flashSale : scheduledSales) {
            scheduledSaleIds.add(flashSale.getId());
        }
        return scheduledSaleIds;
    }

    // 현재 시점에 종료해야할 할인행사 목록 조회
    @Transactional(readOnly = true)
    public List<Long> getClosedFlashSales() {
        LocalDateTime now = LocalDateTime.now();
        List<FlashSale> closedSales = flashSaleRepository.findByStatusAndEndTimeLessThanEqual(FlashSaleStatus.ACTIVE, now);

        List<Long> scheduledSaleIds = new ArrayList<>();
        for (FlashSale flashSale : closedSales) {
            scheduledSaleIds.add(flashSale.getId());
        }
        return scheduledSaleIds;
    }

    /// -------------------------------------------- private method -------------------------------------------- ///

    // DB 행사 상태 업데이트
    private void updateFlashSaleStatus(FlashSale flashSale, FlashSaleStatus status) {
        // DB 상태 변경
        flashSale.updateStatus(status);
        flashSaleRepository.save(flashSale);
    }

    // 행사 조회
    private FlashSale findFlashSale(Long flashSaleId) {
        return flashSaleRepository.findByIdWithProducts(flashSaleId)
                .orElseThrow(() -> new CustomException(ErrorCode.FLASH_SALE_NOT_FOUND));
    }

    private void updateFlashSaleProductCache(FlashSaleItem flashSaleItem) {
        String productKey = RedisKeys.getProductCacheKey(flashSaleItem.getProduct().getId());
        ProductResponseDto productInfo = new ProductResponseDto(flashSaleItem.getProduct(), flashSaleItem);
        Duration ttl = Duration.between(LocalDateTime.now(), flashSaleItem.getFlashSale().getEndTime());
        redisTemplate.opsForValue().set(productKey, productInfo, ttl);
    }

}
