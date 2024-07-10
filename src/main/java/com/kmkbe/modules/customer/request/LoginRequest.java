package com.kmkbe.modules.customer.request;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public record LoginRequest(
        @Schema(description = "Email User yg sudah terdaftar", type = "string", example = "khesaalvandik123@gmail.com")
        String email,

        @Schema(description = "Pin", type = "integer", example = "12345")
        String pin
) {
}
