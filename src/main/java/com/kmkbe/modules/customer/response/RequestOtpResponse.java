package com.kmkbe.modules.customer.response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record RequestOtpResponse(
        String email
) {
}
