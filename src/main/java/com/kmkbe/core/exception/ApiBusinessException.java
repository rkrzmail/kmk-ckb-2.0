package com.kmkbe.core.exception;

import java.util.Map;

public class ApiBusinessException extends RuntimeException {
    private final Integer code;
    private final Map<String, Object> errorDetails;
    private final Map<String, Object> apiResponse;

    public ApiBusinessException(Integer code, String message,
                                Map<String, Object> errorDetails,
                                Map<String, Object> apiResponse) {
        super(message);
        this.code = code;
        this.errorDetails = errorDetails;
        this.apiResponse = apiResponse;
    }

    public Integer getCode() {
        return code;
    }
}