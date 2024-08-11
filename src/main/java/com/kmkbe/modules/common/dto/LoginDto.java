package com.kmkbe.modules.common.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record LoginDto(
        String token,
        String refreshToken,
        Long expiresIn
) {
}
