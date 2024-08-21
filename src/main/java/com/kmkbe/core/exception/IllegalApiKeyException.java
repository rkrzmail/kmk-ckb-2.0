package com.kmkbe.core.exception;

public class IllegalApiKeyException extends RuntimeException {

    public IllegalApiKeyException(String message) {
        super(message);
    }

    public IllegalApiKeyException() {
        super("Cannot set trust request with client, ApiKey doesn't provide or invalid");
    }
}
