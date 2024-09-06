package com.kmkbe.modules.loan_submission.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadRequirementDocumentRequest {
    @NotNull(message = "File Type Code cannot be null")
    @NotEmpty(message = "File Type Code cannot be null")
    private String fileTypeCode;
}
