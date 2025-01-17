package com.mjyoo.limitedflashsale.product.service;

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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    public final ProductRepository productRepository;
    public final InventoryRepository inventoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String INVENTORY_CACHE_KEY = "inventory:";

    public int getStockFromCache(Long productId) {
        String key = INVENTORY_CACHE_KEY + productId;
        Integer cachedStock = (Integer) redisTemplate.opsForValue().get(key);

        if (cachedStock != null) {
            return cachedStock;
        }

        // Cache Miss 처리 - DB 조회 후 캐시 저장
        Inventory inventory = inventoryRepository.findByProductIdWithPessimisticLock(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        int stock = inventory.getStock();

        int ttl = calculateTTL(stock);
        redisTemplate.opsForValue().setIfAbsent(key, stock, ttl, TimeUnit.MINUTES);
        return stock;
    }

    // 재고 차감
    public void decreaseStock(Long productId, int quantity) {
        // 재고 조회
        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        // DB 재고 차감
        inventory.decreaseStock(quantity);
        inventoryRepository.save(inventory);

        // Redis 재고 업데이트
        updateStockInRedis(productId, inventory.getStock());
    }

    // 재고 업데이트
    public void updateStockInRedis(Long productId, int newStock) {
        String key = INVENTORY_CACHE_KEY + productId;
        int ttl = calculateTTL(newStock);
        redisTemplate.opsForValue().set(key, newStock, ttl, TimeUnit.MINUTES);
    }

    // 재고 복원
    public void restoreStock(Order order) {
        log.warn("재고 복원 대상 주문 ID: " + order.getId());

        for (OrderProduct orderProduct : order.getOrderProductList()) {
            // 주문 상품 조회
            Product product = productRepository.findByIdWithInventory(orderProduct.getProduct().getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

            log.warn("재고 복원: " + product.getName() + ", 수량: " + orderProduct.getQuantity());
            // DB 재고 복원
            product.getInventory().restoreStock(orderProduct.getQuantity());
            productRepository.save(product);

            log.warn("복원된 재고 수량: " + product.getInventory().getStock());
        }
    }

    private int calculateTTL(int stock) {
        if (stock < 5) return 1; // 1분
        if (stock < 20) return 3; // 3분
        if (stock < 50) return 5; // 5분
        return 10; // 10분 - 충분한 상태
    }
}
