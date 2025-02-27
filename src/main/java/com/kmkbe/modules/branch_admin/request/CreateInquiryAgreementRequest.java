package com.kmkbe.modules.branch_admin.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CreateInquiryAgreementRequest {
    @NotNull(message = "CWR Code is required, key: cwrCode")
    @NotEmpty(message = "CWR Code is required, key: cwrCode")
    String cwrCode;

    @NotNull(message = "Financing No is required, key: financingNo")
    @NotEmpty(message = "Financing No is required, key: financingNo")
    String financingHdrCode;

    @NotNull(message = "Agreement No is required, key: agreementNo")
    @NotEmpty(message = "Agreement No is required, key: agreementNo")
    private String agreementNo;

    /*@NotNull(message = "Payment Date is required, key: paymentDate")
    @NotEmpty(message = "Payment Date is required, key: paymentDate")*/
    private String paymentDate;

    /*@NotNull(message = "Batch is required, key: batch")
    @NotEmpty(message = "Batch is required, key: batch")*/
    private String batch;
}
