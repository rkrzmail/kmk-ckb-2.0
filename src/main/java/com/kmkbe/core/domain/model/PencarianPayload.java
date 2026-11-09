package com.kmkbe.core.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PencarianPayload {
    private String financingCode;
    private String agreementCode;
    private String companyName;
    private String phoneNumber;
    private String applicationDate;
    private String invoiceAmt;
    private String retention;
    private String financingAmt;
    private String totalFeeAmt;
    private String disburseAmt;
    private String bankName;
    private String accountNo;
    private String disbursementDate;
    private Long tenor;
    private String financingDueDate;

    private List<InvoiceEmailPayload> invoices;
}
