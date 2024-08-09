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
public class SaveImportantNotesRequest {
    @NotNull(message = "token cannot be null")
    @NotEmpty(message = "token cannot be empty")
    private String token;
}
