package com.kmkbe.core.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PencarianPayload {
//    private String customer_name;
//    private String bank_name;
//    private String account_number;
//    private String disbursement_date;
//    private String total_disbursement;
//    private String loan_number;//aggerNo

    private String financingCode;
    private String companyName;
    private String phoneNumber;
    private String applicationDate;
    private String invoiceAmt;
    private String retention;
    private String financingAmt;
    private String totalFeeAmt;
    private String disburseAmt;
    private Long tenor;
    private String financingDueDate;

    private List<InvoiceEmailPayload> invoices;
}
