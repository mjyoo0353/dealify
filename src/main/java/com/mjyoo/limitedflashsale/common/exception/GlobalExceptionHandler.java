package com.mjyoo.limitedflashsale.common.exception;

import com.mjyoo.limitedflashsale.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Business Logic 수행 중 발생시킬 커스텀 에러
     */
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ApiResponse<?>> handleCustomException(CustomException e) {
        log.error("handleCustomException = {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(e.getMessage()));
    }

    /**
     * 지원하지 않은 HTTP method 호출 할 경우 발생
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ApiResponse<?>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("handleHttpRequestMethodNotSupportedException = {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.fail(e.getMessage()));
    }

    /**
     * Authentication 객체가 필요한 권한을 보유하지 않은 경우 발생
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException e) {
        log.error("handleAccessDeniedException = {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(AuthenticationException e) {
        log.error("handleAuthenticationException = {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(e.getMessage()));
    }

    /**
     * 메서드에 전달된 인자가 유효하지 않거나 잘못된 값을 가진 경우 발생
     */
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("handleIllegalArgumentException = {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<String> errorMessages = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage())  // 필드별 검증 메시지 추출
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(String.join(", ", errorMessages)));  // 메시지 합쳐서 반환
    }

    /**
     * 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error("handleException = {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(e.getMessage()));
    }

}
