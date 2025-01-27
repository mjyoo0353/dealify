package com.mjyoo.limitedflashsale.common.util;

public class RedisKeys {
    private static final String INVENTORY_CACHE_KEY = "inventory:";
    private static final String PRODUCT_CACHE_KEY = "product:";
    private static final String REFRESH_TOKEN = "refresh_token:";
    private static final String SIGNUP_CODE = "signup_code:";
    private static final String SIGNUP_CODE_CHECK = ":verified";
    private static final String INVENTORY_LOCK = "inventory_lock:";
    private static final String STOCK_RESTORE = "stock_restore:";


    public static String getInventoryCacheKey(Long productId) {
        return PRODUCT_CACHE_KEY + productId + "_" + INVENTORY_CACHE_KEY;
    }

    public static String getProductCacheKey(Long productId) {
        return PRODUCT_CACHE_KEY + productId;
    }

    public static String getRefreshToken(String token) {
        return REFRESH_TOKEN + token;
    }

    public static String getSignupCode(String email) {
        return SIGNUP_CODE + email;
    }

    public static String getSignupCodeCheck(String email) {
        return SIGNUP_CODE + email + SIGNUP_CODE_CHECK;
    }

    public static String getInventoryLockKey(Long productId) {
        return INVENTORY_LOCK + productId;
    }

    public static String getStockRestoreKey(Long productId) {
        return STOCK_RESTORE + productId;
    }

}
