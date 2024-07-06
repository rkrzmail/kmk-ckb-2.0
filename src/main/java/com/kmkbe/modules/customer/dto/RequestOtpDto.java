package com.kmkbe.modules.customer.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record RequestOtpDto(
        String requestId,
        String email,
        OffsetDateTime expiredTime
) {
}
