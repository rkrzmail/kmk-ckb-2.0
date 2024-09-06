package com.kmkbe.modules.loan_submission.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CreateLoanApplicationRequest {
    @NotNull(message = "Financing header code is required, this from created simulation at first step")
    private UUID financingHdrCode;

    @NotNull(message = "Pin is required")
    @NotEmpty(message = "Pin is required")
    private String pin;

    @NotNull(message = "Uploaded documents is required")
    @NotEmpty(message = "Uploaded documents is required")
    private List<SubmitLoanDocumentRequest> documents;

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class SubmitLoanDocumentRequest {
        @NotNull(message = "Try to map the uploaded document id")
        private Long fileId;

        @NotNull(message = "Try to map the uploaded document typeCode")
        @NotEmpty(message = "Try to map the uploaded document typeCode")
        private String fileTypeCode;

        @NotNull(message = "Try to map the uploaded document fileName")
        @NotEmpty(message = "Try to map the uploaded document fileName")
        private String fileName;
    }
}
