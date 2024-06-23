package com.kmkbe.modules.customer.response;

public record LoginResponse(
        String token,
        Long expiresIn
) {
}
