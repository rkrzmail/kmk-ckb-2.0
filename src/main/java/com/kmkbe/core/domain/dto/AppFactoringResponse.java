package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AppFactoringResponse {
    @JsonProperty("TotalInvcAmt")
    private String totalInvoiceAmount;

    @JsonProperty("TotalRetentionAmt")
    private String totalRetentionAmount;

    @JsonProperty("DiskontoAmt")
    private String diskontoAmount;

    @JsonProperty("HeaderObj")
    private Header header;

    @Data
    public static class Header {
        @JsonProperty("StatusCode")
        private String statusCode;
        @JsonProperty("Message")
        private String message;
    }
}