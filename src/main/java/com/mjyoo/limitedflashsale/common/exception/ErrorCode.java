package com.mjyoo.limitedflashsale.common.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    DUPLICATED_USERNAME(HttpStatus.CONFLICT, "Username already exists"),
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "Email already in use"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "Password does not match"),
    INVALID_ADMIN_TOKEN(HttpStatus.FORBIDDEN, "Invalid admin token"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Login required"), // 401

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Product not found"),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "Insufficient stock"),
    INVALID_STOCK(HttpStatus.BAD_REQUEST, "Invalid stock"),

    // Inventory
    REDIS_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update Redis cache"),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Order not found"),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "Item out of stock"),
    LOCK_ACQUISITION_FAILURE(HttpStatus.CONFLICT, "Failed to acquire stock lock"),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "Invalid order status"),
    ORDER_EXPIRED(HttpStatus.GONE, "Order has expired"),

    // Cart
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "Cart not found"),
    CART_PRODUCT_NOT_FOUND(HttpStatus.BAD_REQUEST, "No items in cart"),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "Invalid quantity"),


    // FlashSale
    FLASH_SALE_NOT_FOUND(HttpStatus.NOT_FOUND, "Sale event not found"),
    FLASH_SALE_NOT_STARTED(HttpStatus.BAD_REQUEST, "Sale has not started yet."),
    FLASH_SALE_NOT_ENDED(HttpStatus.BAD_REQUEST, "Sale has not ended yet."),
    FLASH_SALE_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "No active sale event."),
    INVALID_UPDATE_FLASH_SALE(HttpStatus.CONFLICT, "Cannot update active or ended sale event."),
    INVALID_DELETE_FLASH_SALE(HttpStatus.CONFLICT, "Cannot delete active or ended sale event."),
    FLASH_SALE_NOT_SCHEDULED(HttpStatus.BAD_REQUEST, "No upcoming sale event."),
    PRODUCT_ALREADY_EXISTS(HttpStatus.CONFLICT, "Product already added."),

    // Payment
    PAYMENT_FAILED(HttpStatus.PAYMENT_REQUIRED, "Payment failed"),

    // Auth
    INVALID_EMAIL_VERIFICATION(HttpStatus.UNAUTHORIZED, "Email verification required."),
    TOKEN_CREATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create token."),
    MISSING_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Missing refresh token."),

    // Common
    FORBIDDEN(HttpStatus.FORBIDDEN, "Access denied"); // 403

    private final HttpStatus status;
    private final String message;

}
