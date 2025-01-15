package com.mjyoo.limitedflashsale.product.service;

import com.mjyoo.limitedflashsale.common.exception.CustomException;
import com.mjyoo.limitedflashsale.common.exception.ErrorCode;
import com.mjyoo.limitedflashsale.product.entity.Inventory;
import com.mjyoo.limitedflashsale.product.repository.InventoryRepository;
import com.mjyoo.limitedflashsale.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class InventoryService {

    public final ProductRepository productRepository;
    public final InventoryRepository inventoryRepository;
    private final RedisTemplate<String, Integer> inventoryRedisTemplate;
    private static final String INVENTORY_CACHE_KEY = "inventory:";

    public int getStockFromCache(Long productId) {
        String key = INVENTORY_CACHE_KEY + productId;
        Integer cachedStock = inventoryRedisTemplate.opsForValue().get(key);

        if (cachedStock != null) {
            return cachedStock;
        }

        // Cache Miss 처리 - DB 조회 후 캐시 저장
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        int stock = inventory.getStock();

        int ttl = calculateTTL(stock);
        inventoryRedisTemplate.opsForValue().setIfAbsent(key, stock, ttl, TimeUnit.MINUTES);
        return stock;
    }

    public void updateStock(Long productId, int newStock) {
        String key = INVENTORY_CACHE_KEY + productId;
        int ttl = calculateTTL(newStock);
        inventoryRedisTemplate.opsForValue().set(key, newStock, ttl, TimeUnit.MINUTES);
    }

    private int calculateTTL(int stock) {
        if(stock < 5) return 1; // 1분
        if(stock < 20) return 3; // 3분
        if(stock < 50) return 5; // 5분
        return 10; // 10분 - 충분한 상태
    }
}
