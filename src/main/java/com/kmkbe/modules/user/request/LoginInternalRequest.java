package com.kmkbe.modules.user.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class LoginInternalRequest {
    @NotNull(message = "Email or Username cannot be null")
    @NotEmpty(message = "Email or Username cannot be empty")
    private String emailOrUsername;

    @NotNull(message = "Password cannot be null")
    @NotEmpty(message = "Password cannot be empty")
    private String password;
}
