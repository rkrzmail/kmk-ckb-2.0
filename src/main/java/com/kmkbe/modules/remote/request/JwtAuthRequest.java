package com.kmkbe.modules.remote.request;

public record JwtAuthRequest(
        String loginID,
        Long requestTimestamp
) {
}
