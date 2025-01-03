package com.mjyoo.limitedflashsale.common.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // User
    USER_NOT_FOUND("해당 유저 정보를 찾을 수 없습니다."),
    DUPLICATED_USERNAME("해당 유저네임은 이미 사용중입니다."),
    DUPLICATED_EMAIL("해당 이메일은 이미 사용중입니다."),
    INVALID_PASSWORD("비밀번호가 일치하지 않습니다."),
    INVALID_ADMIN_TOKEN("관리자 토큰이 유효하지 않습니다. 올바른 토큰을 입력해주세요."),

    // Product
    PRODUCT_NOT_FOUND("해당 상품 정보를 찾을 수 없습니다."),
    INVALID_PRICE("가격이 유효하지 않습니다. 최소 $1 이상의 가격을 입력해주세요."),

    // Order
    ORDER_NOT_FOUND("해당 주문 정보를 찾을 수 없습니다."),
    OUT_OF_STOCK("재고가 부족합니다."),

    // Cart
    CART_NOT_FOUND("해당 장바구니 정보를 찾을 수 없습니다."),
    CART_PRODUCT_NOT_FOUND("해당 장바구니 상품 정보를 찾을 수 없습니다."),


    // Common
    BAD_REQUEST("잘못된 요청입니다. 다시 시도해 주세요."), //400
    INVALID_INPUT_VALUE("입력값이 올바르지 않습니다."), //400
    METHOD_ARGUMENT_NOT_VALID("유효성 검사 실패"), //400
    METHOD_TYPE_MISMATCH("파라미터 타입 불일치"), //400
    UNAUTHORIZED("로그인이 필요합니다."), //401
    FORBIDDEN("접근 권한이 없습니다."), //403
    RESOURCE_NOT_FOUND("해당 정보를 찾을 수 없습니다."), //404
    METHOD_NOT_ALLOWED("지원되지 않는 HTTP 메서드입니다."), //405
    INTERNAL_SERVER_ERROR("예기치 못한 오류가 발생하였습니다."), //500
    DUPLICATE_RESOURCE("데이터가 이미 존재합니다."); //409


    private final String message;

}
