package com.kmkbe.modules.customer.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record LoginDto(
        String token,
        Long expiresIn
) {
}
