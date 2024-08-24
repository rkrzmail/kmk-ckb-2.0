package com.kmkbe.modules.loan_submission.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CreateSessionLoanSubmissionRequest {
    @NotNull(message = "lastStep is required")
    private Integer lastStep;

    @NotNull(message = "session is required")
    private Object session;
}
