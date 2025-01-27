package com.mjyoo.limitedflashsale.common.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // User
    USER_NOT_FOUND("User not found"),
    DUPLICATED_USERNAME("Username already exists"),
    DUPLICATED_EMAIL("Email already in use"),
    INVALID_PASSWORD("Password does not match"),
    INVALID_ADMIN_TOKEN("Invalid admin token"),

    // Product
    PRODUCT_NOT_FOUND("Product not found"),
    INSUFFICIENT_STOCK("Insufficient stock"),
    INVALID_STOCK("Invalid stock"),

    // Inventory
    CONFLICT_UPDATE_STOCK("Stock update conflict. Please try again."),

    // Order
    ORDER_NOT_FOUND("Order not found"),
    OUT_OF_STOCK("Item out of stock"),
    LOCK_ACQUISITION_FAILURE("Failed to acquire stock lock"),
    INVALID_ORDER_STATUS("Invalid order status"),
    ORDER_EXPIRED("Order has expired"),

    // Cart
    CART_NOT_FOUND("Cart not found"),
    CART_PRODUCT_NOT_FOUND("No items in cart"),
    INVALID_QUANTITY("Invalid quantity"),

    // FlashSale
    FLASH_SALE_NOT_FOUND("Sale event not found"),
    FLASH_SALE_NOT_STARTED("Sale has not started yet."),
    FLASH_SALE_NOT_ENDED("Sale has not ended yet."),
    FLASH_SALE_NOT_ACTIVE("No active sale event."),
    INVALID_UPDATE_FLASH_SALE("Cannot update active or ended sale event."),
    INVALID_DELETE_FLASH_SALE("Cannot delete active or ended sale event."),
    FLASH_SALE_NOT_SCHEDULED("No upcoming sale event."),
    PRODUCT_ALREADY_EXISTS("Product already added."),

    // Payment
    PAYMENT_FAILED("Payment failed"),

    // Auth
    INVALID_EMAIL_VERIFICATION("Email verification required."),
    TOKEN_CREATION_ERROR("Failed to create token."),
    MISSING_REFRESH_TOKEN("Missing refresh token."),

    // Common
    BAD_REQUEST("Invalid request. Please try again."), //400
    INVALID_INPUT_VALUE("Invalid input"), //400
    UNAUTHORIZED("Login required"), //401
    FORBIDDEN("Access denied"), //403
    RESOURCE_NOT_FOUND("Resource not found"); //404


    private final String message;

}
