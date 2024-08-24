package com.kmkbe.modules.remote.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FinancingSubmissionRequest {
    @JsonProperty("vendor_code")
    public String vendorCode;

    @JsonProperty("account_no")
    public String accountNo;

    @JsonProperty("account_name")
    public String accountName;

    @JsonProperty("bank_name")
    public String bankName;

    @JsonProperty("bank_key")
    public String bankKey;

    @JsonProperty("financing_code")
    public String financingCode;

    @JsonProperty("financing_amount")
    public Double financingAmount;

    @JsonProperty("data")
    public List<FinancingInvoice> financingInvoices;

    @Getter
    @Builder
    public static class FinancingInvoice{
        @JsonProperty("po_number")
        public String poNumber;

        @JsonProperty("accounting_document")
        public String accountingDocument;

        @JsonProperty("reference")
        public String reference;

        @JsonProperty("invoice_amount")
        public Double invoiceAmount;
    }
}
