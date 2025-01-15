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

        inventoryRedisTemplate.opsForValue().setIfAbsent(key, stock, 5, TimeUnit.MINUTES);
        return stock;
    }

    public void updateStock(Long productId, int newStock) {
        String key = INVENTORY_CACHE_KEY + productId;
        inventoryRedisTemplate.opsForValue().set(key, newStock, 5, TimeUnit.MINUTES);
    }
}
