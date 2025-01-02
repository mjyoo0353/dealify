package com.mjyoo.limitedflashsale.common.dto;


import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ApiResponse<T> {
    private String timestamp;
    private boolean success;
    private String message;
    private T data;

    public ApiResponse(boolean success, String message, T data, String timestamp) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    // 성공 응답 생성 메서드
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "요청이 성공적으로 처리되었습니다.", data, LocalDateTime.now().toString());
    }

    // 데이터만 있는 성공 응답 생성 메서드
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now().toString());
    }

    // 실패 응답 생성 메서드
    public static ApiResponse<?> fail(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now().toString());
    }



}
