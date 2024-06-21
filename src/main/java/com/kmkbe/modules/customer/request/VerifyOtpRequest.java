package com.kmkbe.modules.customer.request;

public record VerifyOtpRequest(
        String custCode,
        String otpCode
) {
}
