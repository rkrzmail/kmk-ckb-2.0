package com.kmkbe.modules.common.request;

public record CsulLoginRequest(
        String loginID,
        Long requestTimestamp
) {
}
