package com.kmkbe.modules.customer.model.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema
public record LoginRequest(
        @Schema(description = "Email User yg sudah terdaftar", type = "string", example = "khesaalvandik123@gmail.com")
        @NotNull(message = "Email is required")
        @NotEmpty(message = "Email is required")
        String email,

        @Schema(description = "Pin", type = "integer", example = "12345")
        @NotNull(message = "Pin is required")
        @NotEmpty(message = "Pin is required")
        String pin
) {
}
