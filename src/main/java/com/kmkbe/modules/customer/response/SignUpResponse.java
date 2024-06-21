package com.kmkbe.modules.customer.response;

import lombok.*;

import java.util.UUID;

public record SignUpResponse(
        UUID custCode
) {
}
