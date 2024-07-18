package com.kmkbe.modules.customer.request;


import jakarta.validation.constraints.NotNull;

public record VerifyOtpRequest(
        String requestId,
        @NotNull
        String email,

        @NotNull
        String otpCode
) {
}
