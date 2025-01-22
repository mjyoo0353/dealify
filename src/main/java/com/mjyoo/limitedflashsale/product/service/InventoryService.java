package com.mjyoo.limitedflashsale.product.service;

import com.mjyoo.limitedflashsale.common.util.RedisKeys;
import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.order.entity.Order;
import com.mjyoo.limitedflashsale.order.entity.OrderProduct;
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

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    public final ProductRepository productRepository;
    public final InventoryRepository inventoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;

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

    // 캐시 재고 조회 전용
    public Integer getStockFromCache(Long productId) {
        String key = RedisKeys.getInventoryCacheKey(productId);
        return (Integer) redisTemplate.opsForValue().get(key);
    }

    // 재고 감소 로직 (비관적 락 사용)
    @Transactional
    public void validateAndDecreaseStock(Long productId, int quantity) {
        // 캐시에서 먼저 재고 조회
        Integer cachedStock = getStockFromCache(productId);
        if (cachedStock != null && cachedStock < quantity) {
            throw new CustomException(ErrorCode.OUT_OF_STOCK);
        }

        // DB 재고 조회 및 락 획득
        Inventory inventory = inventoryRepository.findByProductIdWithPessimisticLock(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // 락 획득 후 실제 재고 검증
        if (inventory.getStock() < quantity) {
            throw new CustomException(ErrorCode.OUT_OF_STOCK);
        }
        // 재고 감소
        inventory.decreaseStock(quantity);

        // Redis 재고 캐시 업데이트
        updateStockInRedis(productId, inventory.getStock());
    }

    // 재고 복원
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    @Transactional
    public void restoreStock(Order order) {
        log.warn("재고 복원 대상 주문 ID: " + order.getId());

        try {
            for (OrderProduct orderProduct : order.getOrderProductList()) {
                // 주문 상품 조회
                Product product = productRepository.findByIdWithInventory(orderProduct.getProduct().getId())
                        .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

                log.warn("재고 복원: " + product.getName() + ", 수량: " + orderProduct.getQuantity());
                // DB 재고 복원
                product.getInventory().restoreStock(orderProduct.getQuantity());
                productRepository.save(product);

                // Redis 재고 업데이트
                String key = RedisKeys.getInventoryCacheKey(product.getId());
                int ttl = calculateTTL(product.getInventory().getStock());
                redisTemplate.opsForValue().set(key, product.getInventory().getStock(), ttl, TimeUnit.MINUTES);

                log.warn("복원된 재고 수량: " + product.getInventory().getStock());
            }
        } catch (Exception e) {
            log.error("재고 복원 실패 - 주문 ID: " + order.getId(), e);
            throw e;
        }
    }

    // 재고 업데이트
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    @Transactional
    public void updateStockInRedis(Long productId, int newStock) {
        String key = RedisKeys.getInventoryCacheKey(productId);
        try {
            int ttl = calculateTTL(newStock);
            redisTemplate.opsForValue().set(key, newStock, ttl, TimeUnit.MINUTES);
            log.info("재고 캐시 업데이트 - 상품 ID: {}, 재고: {}, TTL: {}분", productId, newStock, ttl);
        } catch (Exception e) {
            redisTemplate.delete(key);
            log.error("재고 캐시 업데이트 실패 - 상품 ID: {}, 재고: {}", productId, newStock);
            throw e;
        }
    }

    private int calculateTTL(int stock) {
        if (stock < 5) return 1; // 1분
        if (stock < 20) return 3; // 3분
        if (stock < 50) return 5; // 5분
        return 10; // 10분 - 충분한 상태
    }
}
