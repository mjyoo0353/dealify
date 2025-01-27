package com.mjyoo.limitedflashsale.product.service;

import com.mjyoo.limitedflashsale.common.util.RedisKeys;
import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.order.entity.OrderItem;
import com.mjyoo.limitedflashsale.product.entity.Inventory;
import com.mjyoo.limitedflashsale.product.entity.Product;
import com.mjyoo.limitedflashsale.product.repository.InventoryRepository;
import com.mjyoo.limitedflashsale.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    public final ProductRepository productRepository;
    public final InventoryRepository inventoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;


    // 초기 재고 설정 메서드
    public void setStock(Long productId, int stock) {
        // Redis에 초기 재고 설정
        redisTemplate.opsForValue().set(RedisKeys.getInventoryCacheKey(productId), stock);
    }

    // 재고 조회용 - DB 조회 및 캐시 갱신
    @Transactional(readOnly = true)
    public int getStock(Long productId) {
        // 캐시 조회
        Integer cachedStock = getStockFromCache(productId);
        if (cachedStock != null) {
            return cachedStock;
        }

        // Cache Miss 처리 - DB 조회 (읽기만 하므로 일반 조회)
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        int stock = inventory.getStock();

        // 캐시 업데이트
        int ttl = calculateTTL(stock);
        redisTemplate.opsForValue().setIfAbsent(RedisKeys.getInventoryCacheKey(productId), stock, ttl, TimeUnit.MINUTES);
        return stock;
    }

    // 캐시 재고 조회
    public Integer getStockFromCache(Long productId) {
        String key = RedisKeys.getInventoryCacheKey(productId);
        return (Integer) redisTemplate.opsForValue().get(key);
    }

    @Transactional
    public void decreaseStock(Product product, int quantity) {
        // Redisson으로 분산 락 획득 시도
        RLock lock = redissonClient.getLock(RedisKeys.getInventoryLockKey(product.getId()));

        try {
            // 락 획득 시도 (최대 3초 대기, 10초 점유) - 타임아웃으로 무한 대기 방지
            boolean isLocked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILURE);
            }

            // DB 재고 감소 처리
            product.getInventory().decreaseStock(quantity);

            // 재고 감소 처리 캐시에도 업데이트
            String key = RedisKeys.getInventoryCacheKey(product.getId());
            Long currentStock = redisTemplate.opsForValue().decrement(key, quantity);

            // 재고 부족 시 롤백
            if (currentStock < 0) {
                redisTemplate.opsForValue().increment(key, quantity);
                throw new CustomException(ErrorCode.INSUFFICIENT_STOCK);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock acquisition interrupted", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 재고 복원 - 시간 만료 주문 처리, 주문 취소, PaymentService 결제 실패/오류 시 사용
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    @Transactional
    public void restoreStock(List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            restoreStockWithLock(orderItem);
        }
    }

    private void restoreStockWithLock(OrderItem orderItem) {
        // 분산 락 획득
        String lockKey = RedisKeys.getStockRestoreKey(orderItem.getProduct().getId());
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILURE);
            }

            // 주문 상품 조회
            Product product = productRepository.findByIdWithInventory(orderItem.getProduct().getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

            // DB 재고 복원
            int restoredQuantity = orderItem.getQuantity();
            product.getInventory().restoreStock(restoredQuantity);
            productRepository.save(product);

            // Redis 재고 업데이트
            String key = RedisKeys.getInventoryCacheKey(product.getId());
            redisTemplate.opsForValue().increment(key, restoredQuantity);

            log.info("Restored stock for product: {}. Quantity: {}, Current stock: {}",
                    product.getId(), restoredQuantity, product.getInventory().getStock());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILURE);
        }
    }

    // 재고 업데이트 - 주문 생성, 상품 수정에서 사용
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    @Transactional
    public void updateStockInRedis(Long productId, int newStock) {
        String key = RedisKeys.getInventoryCacheKey(productId);
        try {
            redisTemplate.opsForValue().setIfPresent(key, newStock);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.REDIS_UPDATE_FAILED);
        }
    }

    private int calculateTTL(int stock) {
        if (stock < 5) return 1; // 1분
        if (stock < 20) return 3; // 3분
        if (stock < 50) return 5; // 5분
        return 10; // 10분 - 충분한 상태
    }
}
