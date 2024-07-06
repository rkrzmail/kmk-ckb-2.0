package com.kmkbe.modules.internal.request;

public record InternalAuthRequest(
        String loginID,
        Long requestTimestamp
) {
}
