package com.kmkbe.core.exception;

import java.util.Map;

public class DebtorCreationException extends RuntimeException {
    private final Map<String, Object> errorDetails;

    public DebtorCreationException(String message, Map<String, Object> errorDetails) {
        super(message);
        this.errorDetails = errorDetails;
    }

}
