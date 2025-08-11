package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class FinancialDataResponse {
    @JsonProperty("AgrmntFinDataObj")
    private FinancialData financialData;

    @JsonProperty("AgrmntFeeObjs")
    private List<AgreementFee> feeList;

    @JsonProperty("HeaderObj")
    private Header header;

    @Data
    public static class FinancialData {
        @JsonProperty("NtfAmt")
        private String ntfAmount;

        @JsonProperty("DiskontoAmt")
        private String diskontoAmount;

        @JsonProperty("MaxAllocatedRefundAmt")
        private String maxRefundAmount;

        @JsonProperty("TotalRetentionAmt")
        private String totalRetention;

        @JsonProperty("TotalInvcAmt")
        private String totalInvoiceAmount;

        @JsonProperty("EffectiveRatePrcnt")
        private String effectiveRate;

        @JsonProperty("InstAmt")
        private String installmentAmount;

        @JsonProperty("TotalFeeAmt")
        private String totalFeeAmount;

        @JsonProperty("GracePeriod")
        private String gracePeriod;

    }

    @Data
    public static class AgreementFee {
        @JsonProperty("FeeTypeName")
        private String feeTypeName;

        @JsonProperty("AppFeeAmt")
        private String feeAmount;
    }

    @Data
    public static class Header {
        @JsonProperty("StatusCode")
        private String statusCode;

        @JsonProperty("Message")
        private String message;
    }
}

