package com.kmkbe.core.exception;

public class ApiBusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final Integer code;

    public ApiBusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
