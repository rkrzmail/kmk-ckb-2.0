package com.kmkbe.modules.customer.request;

public record LoginRequest(
        String email,
        String pin
) {
}
