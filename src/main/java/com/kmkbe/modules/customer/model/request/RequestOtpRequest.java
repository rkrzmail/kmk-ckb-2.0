package com.kmkbe.modules.customer.model.request;

import jakarta.validation.constraints.NotNull;

public record RequestOtpRequest(
        @NotNull
        String email
) {
}
