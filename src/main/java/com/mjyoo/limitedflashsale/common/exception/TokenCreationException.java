package com.mjyoo.limitedflashsale.common.exception;

public class TokenCreationException extends RuntimeException {

    public TokenCreationException(String message) {
        super(message);
    }

    public TokenCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
