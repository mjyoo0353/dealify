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

    // Inventory
    CONFLICT_UPDATE_STOCK("재고 처리 중 충돌이 발생했습니다. 다시 시도해주세요."),

    // Order
    ORDER_NOT_FOUND("해당 주문 정보를 찾을 수 없습니다."),
    OUT_OF_STOCK("재고가 부족합니다."),
    LOCK_ACQUISITION_FAILURE("재고 업데이트 락 획득 실패"),
    INVALID_ORDER_STATUS("주문 상태가 유효하지 않습니다."),
    ORDER_EXPIRED("주문이 만료되었습니다."),

    // Cart
    CART_NOT_FOUND("해당 장바구니 정보를 찾을 수 없습니다."),
    CART_PRODUCT_NOT_FOUND("장바구니에 상품이 없습니다."),
    INVALID_QUANTITY("수량이 유효하지 않습니다."),

    // FlashSale
    FLASH_SALE_NOT_FOUND("해당 이벤트 정보를 찾을 수 없습니다."),
    FLASH_SALE_NOT_STARTED("행사 시작 시간이 아닙니다."),
    FLASH_SALE_NOT_ENDED("행사 종료 시간이 아닙니다."),
    FLASH_SALE_NOT_ONGOING("현재 행사가 진행 중이 아닙니다."),
    INVALID_UPDATE_FLASH_SALE("종료된 행사는 수정이 불가합니다."),
    INVALID_DELETE_FLASH_SALE("진행 중이거나 종료된 행사는 삭제가 불가합니다."),
    FLASH_SALE_NOT_SCHEDULED("예정된 행사가 없습니다."),
    PRODUCT_ALREADY_EXISTS("이미 등록된 상품입니다."),

    // Payment
    PAYMENT_FAILED("결제에 실패하였습니다."),

    // Auth
    INVALID_EMAIL_VERIFICATION("이메일 인증이 필요합니다."),
    TOKEN_CREATION_ERROR("토큰 생성에 실패하였습니다."),
    MISSING_REFRESH_TOKEN("리프레시 토큰이 없습니다."),

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
