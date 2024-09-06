package com.kmkbe.modules.customer.request;

public record ForgotPinRequest(
        String email,
        String pin
) {

}
