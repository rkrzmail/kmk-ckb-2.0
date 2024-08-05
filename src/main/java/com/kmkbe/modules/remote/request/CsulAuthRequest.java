package com.kmkbe.modules.remote.request;

public record CsulAuthRequest(
        String loginID,
        Long requestTimestamp
) {
}
