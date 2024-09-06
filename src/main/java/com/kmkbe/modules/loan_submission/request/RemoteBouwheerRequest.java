package com.kmkbe.modules.loan_submission.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class RemoteBouwheerRequest {
    @NotNull(message = "Token is required, null present")
    @NotEmpty(message = "Token is required, empty present")
    private String token;
}
