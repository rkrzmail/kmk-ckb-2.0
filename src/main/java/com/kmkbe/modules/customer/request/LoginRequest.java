package com.kmkbe.modules.customer.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema
public record LoginRequest(
        @Schema(description = "Email User yg sudah terdaftar", type = "string", example = "khesaalvandik123@gmail.com")
        @NotNull
        String email,

        @Schema(description = "Pin", type = "integer", example = "12345")
        @NotNull
        String pin
) {
}
