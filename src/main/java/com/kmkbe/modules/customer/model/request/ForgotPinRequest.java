package com.kmkbe.modules.customer.model.request;

public record ForgotPinRequest(
        String email,
        String pin,
        String token
) {

}
