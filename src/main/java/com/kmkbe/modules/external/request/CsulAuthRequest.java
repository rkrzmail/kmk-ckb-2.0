package com.kmkbe.modules.external.request;

public record CsulAuthRequest(
        String loginID,
        Long requestTimestamp
) {
}
