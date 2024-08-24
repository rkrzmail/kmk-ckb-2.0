package com.kmkbe.modules.loan_submission.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class FinancingInvoicePaidRequest {
    @NotNull(message = "BouwheerCode is required")
    @NotEmpty(message = "BouwheerCode shouldn't be empty")
    @JsonProperty("BouwheerCode")
    @JsonAlias("bouwheerCode")
    private String bouwheerCode;

    @NotNull(message = "VendorCode is required")
    @NotEmpty(message = "VendorCode shouldn't be empty")
    @JsonProperty("vendorCode")
    @JsonAlias("VendorCode")
    private String vendorCode;

    @NotNull(message = "FinancingCode is required")
    @NotEmpty(message = "FinancingCode shouldn't be empty")
    @JsonProperty("financingCode")
    @JsonAlias("FinancingCode")
    private String financingCode;

    @Valid
    @NotNull(message = "InvoicePaid is required")
    @NotEmpty(message = "InvoicePaid shouldn't be empty")
    @JsonProperty("invoicePaid")
    @JsonAlias("InvoicePaid")
    private List<InvoicePaid> invoicePaid;


    @Getter
    @Setter
    public static class InvoicePaid {
        @NotNull(message = "InvoiceNo in InvoicePaid is required")
        @NotEmpty(message = "InvoiceNo in InvoicePaid shouldn't be empty")
        @JsonProperty("invoiceNo")
        @JsonAlias("InvoiceNo")
        private String invoiceNo;

        @NotNull(message = "PoNo in InvoicePaid is required")
        @NotEmpty(message = "PoNo in InvoicePaid shouldn't be empty")
        @JsonProperty("poNo")
        @JsonAlias("PoNo")
        private String poNo;

        @NotNull(message = "ClearingNo in InvoicePaid is required")
        @NotEmpty(message = "ClearingNo in InvoicePaid shouldn't be empty")
        @JsonProperty("clearingNo")
        @JsonAlias("ClearingNo")
        private String clearingNo;

        @NotNull(message = "ClearingDate in InvoicePaid is required")
        @JsonProperty("clearingDate")
        @JsonAlias("ClearingDate")
        private Date clearingDate;

        @NotNull(message = "PostingDate in InvoicePaid is required")
        @JsonProperty("postingDate")
        @JsonAlias("PostingDate")
        private Date postingDate;

        @NotNull(message = "InvoiceAmount in InvoicePaid is required")
        @JsonProperty("invoiceAmount")
        @JsonAlias("InvoiceAmount")
        private Integer invoiceAmount;
    }

}
