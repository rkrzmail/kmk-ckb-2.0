package com.kmkbe.feign.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsulInquiryInvoiceRemoteDto {
    @JsonProperty("blacklist_status")
    private Boolean blacklistStatus;

    @JsonProperty("identity_number")
    private String identityNumber;

    @JsonProperty("identity_type")
    private String identityType;

    @JsonProperty("document_status")
    private String documentStatus; // 01/02/03

    @JsonProperty("data_vendor_eproc")
    private Boolean dataVendorEproc;

    @JsonProperty("count")
    private Integer count;

    @JsonProperty("row")
    private List<InvoiceRemoteDto> row;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceRemoteDto {
        @JsonProperty("vendor_no")
        public String vendorNo;

        @JsonProperty("reference")
        public String reference;

        @JsonProperty("accounting_document")
        public String accountingDocument;

        @JsonProperty("po_number")
        public String poNumber;

        @JsonProperty("amount")
        public String amount;

        @JsonProperty("currency")
        public String currency;

        @JsonProperty("net_due_date")
        public String netDueDate;

        @JsonProperty("posting_date")
        public String postingDate;

        @JsonProperty("description")
        public String description;
    }
}
