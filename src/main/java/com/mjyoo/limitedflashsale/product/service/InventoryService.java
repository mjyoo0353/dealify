package com.mjyoo.limitedflashsale.product.service;

import com.mjyoo.limitedflashsale.common.util.RedisKeys;
import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.order.entity.Order;
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
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    public final ProductRepository productRepository;
    public final InventoryRepository inventoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    // 재고 조회용 - DB 조회 및 캐시 갱신
    @Transactional(readOnly = true)
    public int getStock(Long productId) {
        // Redis 캐시 조회
        String cacheKey = RedisKeys.getInventoryCacheKey(productId);
        Integer cachedStock = (Integer) redisTemplate.opsForValue().get(cacheKey);

        // Cache Miss 처리
        if (cachedStock == null) {
            // DB 조회 (읽기만 하므로 일반 조회)
            int stock = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND))
                    .getStock();
            // 캐시 업데이트
            redisTemplate.opsForValue().set(cacheKey, stock, calculateTTL(stock), TimeUnit.MINUTES);
            return stock;
        }
        return cachedStock;
    }

    // 레디스에서 재고 감소(선점/예약) - 주문 생성 시 사용
    public boolean reserveStockToRedis(Product product, int quantity) {
        // Redis에서 재고 확인
        String key = RedisKeys.getInventoryCacheKey(product.getId());
        Integer stock = (Integer) redisTemplate.opsForValue().get(key);
        int dbStock = product.getInventory().getStock();

        // Cache Miss 처리
        if (stock == null) {
            redisTemplate.opsForValue().set(key, dbStock);
        }

        try {
            // Lua Script를 이용한 원자적 감소
            String luaScript =
                    "local stock = redis.call('GET', KEYS[1]) " +
                            "if (not stock or tonumber(stock) < tonumber(ARGV[1])) then return -1 " +
                            "else return redis.call('DECRBY', KEYS[1], ARGV[1]) end";

            Long result = redisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class),
                    Collections.singletonList(key), quantity);

            return result != null && result >= 0;
        } catch (Exception e) {
            // Redis 예외 발생 시 DB fallback
            return checkAndReserveStockInDB(product.getId(), quantity);
        }
    }

    // 결제 성공 시 DB 재고 감소
    @Transactional
    public void decreaseStockToDB(Order order) {
        for (OrderItem item : order.getOrderItemList()) {
            Long productId = item.getProduct().getId();
            int quantity = item.getQuantity();

            int updatedRows = inventoryRepository.decrementStock(productId, quantity);

            if (updatedRows == 0) {
                throw new CustomException(ErrorCode.OUT_OF_STOCK);
            }
        }
    }

    // 레디스 재고 복원 - 주문 중 이탈 시 사용
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void restoreRedisStock(Order order) {
        for (OrderItem item : order.getOrderItemList()) {
            String key = RedisKeys.getInventoryCacheKey(item.getProduct().getId());
            redisTemplate.opsForValue().increment(key, item.getQuantity());

            log.info("Restored stock for product: {}. Quantity: {}",
                    item.getProduct().getId(), item.getQuantity());
        }
    }

    // DB & Redis 재고 복원 - 주문결제 건 취소 시 사용
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Transactional
    public void restoreStockOnCancel(Order order) {
        for (OrderItem item : order.getOrderItemList()) {
            Long productId = item.getProduct().getId();
            int quantity = item.getQuantity();

            // Redis 재고 복원
            String key = RedisKeys.getInventoryCacheKey(productId);
            redisTemplate.opsForValue().increment(key, quantity);

            // DB 재고 복원
            inventoryRepository.incrementStock(productId, quantity);

            log.info("Restored stock for product: {}. Quantity: {}", productId, quantity);
        }
    }

    // 재고 업데이트 - 주문 생성, 상품 재고 수정에서 사용
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void updateStockInRedis(Long productId, int newStock) {
        String key = RedisKeys.getInventoryCacheKey(productId);
        redisTemplate.opsForValue().set(key, newStock);
    }

    // Redis 예외 발생 시 DB fallback - DB에서 재고 확인/선점
    private boolean checkAndReserveStockInDB(Long id, int quantity) {
        // DB에서 재고 확인/선점
        String lockKey = RedisKeys.getInventoryLockKey(id);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                try {
                    Inventory inventory = inventoryRepository.findByProductId(id)
                            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

                    if (inventory.getStock() < quantity) {
                        return false;
                    }

                    // DB 재고 임시 차감 (실제 차감은 주문 완료 시)
                    inventoryRepository.decrementStock(id, quantity);

                    // Redis 복구 시도
                    try {
                        redisTemplate.opsForValue().set(lockKey, inventory.getStock());
                    } catch (Exception e) {
                        log.error("Failed to update Redis stock for productId: {}", id, e);
                    }
                    return true;

                } finally {
                    lock.unlock();
                }
            }
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock acquisition interrupted", e);
        }
    }

    private int calculateTTL(int stock) {
        if (stock < 10) return 1; // 1분
        if (stock < 50) return 5; // 5분
        return 10; // 10분 - 충분한 상태
    }

}
