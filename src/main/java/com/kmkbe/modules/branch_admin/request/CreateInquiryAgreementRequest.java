package com.kmkbe.modules.branch_admin.request;

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
public class CreateInquiryAgreementRequest {
    @NotNull(message = "Agreement No is required, key: agreementNo")
    @NotEmpty(message = "Agreement No is required, key: agreementNo")
    private String agreementNo;
}
