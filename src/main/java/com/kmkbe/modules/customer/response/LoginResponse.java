package com.kmkbe.modules.customer.response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record LoginResponse(
        String token,
        Long expiresIn
) {
}
