package com.kmkbe.modules.loan_submission.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CreateFinancingRequest {
    @JsonProperty(required = true)
    @NotNull(message = "Please provide a valid custCode")
    private UUID custCode;

    @JsonProperty(required = true)
    @NotNull(message = "Please provide a valid bouwheerCode")
    private UUID bouwheerCode;

    private Instant financingDate;
    private String currencyCode;
    private Long invoiceQty;
    private String interestType;
    private Long tenor;
    private Double effectiveRate;
    private Double interestAmt;
    private Long gracePeriod;
    private Double retention;
    private Double totalInvoiceAmt;
    private Double provisionFeePercentage;
    private Double provisionFeeAmt;
    private Double surveyFeeAmt;
    private Double surveyFeeAmtNett;
    private Double legalFeeAmt;
    private Double legalFeeAmtNett;
    private Double adminLimitAmt;
    private Double adminFeePercentage;
    private Double adminFeeAmt;
    private Double insuranceFeePercentage;
    private Double insuranceFeeAmt;
    private Double othersFeeAmt;
    private Double financingAmt;
    private Double disburseAmt;
    private Double disburseDate;
    private Instant financingDueDate;

    @JsonProperty(required = true)
    @NotNull(message = "Please provide attach details, at least one")
    private List<CreateFinancingDetailRequest> details;

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class CreateFinancingDetailRequest {
        private String invoiceNumber;
        private Long invoiceSeqNumber;
    }
}
