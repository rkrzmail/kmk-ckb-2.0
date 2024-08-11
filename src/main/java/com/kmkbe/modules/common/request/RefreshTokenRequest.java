package com.kmkbe.modules.common.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record RefreshTokenRequest(
        @NotNull(message = "Refresh Token is required, key: refreshToken")
        @NotEmpty(message = "Refresh Token is required, key: refreshToken")
        String refreshToken
) {
}
