package com.mjyoo.limitedflashsale.common.dto;


import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ApiResponse<T> {
    private final LocalDateTime timestamp;
    private final String status; // "SUCCESS" or "FAIL"
    private final String message;
    private final T data;

    public ApiResponse(String status, String message, T data) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
        this.data = data;

    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", null, data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }

    // 실패 응답 생성 메서드
    public static ApiResponse<?> error(String message) {
        return new ApiResponse<>("ERROR", message, null);
    }



}
