package com.kmkbe.modules.customer.request;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record VerifyOtpRequest(
        String requestId,
        @NotNull(message = "email is required")
        @NotEmpty(message = "email is empty")
        String email,

        @NotNull(message = "otp is required")
        @NotEmpty(message = "otp is empty")
        String otp,

        String pin
) {
}
