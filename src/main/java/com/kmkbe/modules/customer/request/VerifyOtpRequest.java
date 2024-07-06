package com.kmkbe.modules.customer.request;

public record VerifyOtpRequest(
        String requestId,
        String email,
        String otpCode
) {
}
