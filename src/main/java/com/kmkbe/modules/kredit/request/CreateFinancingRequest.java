package com.kmkbe.modules.kredit.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
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
    private BigDecimal effectiveRate;
    private BigDecimal interestAmt;
    private Long gracePeriod;
    private BigDecimal retention;
    private BigDecimal totalInvoiceAmt;
    private BigDecimal provisionFeePercentage;
    private BigDecimal provisionFeeAmt;
    private BigDecimal surveyFeeAmt;
    private BigDecimal surveyFeeAmtNett;
    private BigDecimal legalFeeAmt;
    private BigDecimal legalFeeAmtNett;
    private BigDecimal adminLimitAmt;
    private BigDecimal adminFeePercentage;
    private BigDecimal adminFeeAmt;
    private BigDecimal insuranceFeePercentage;
    private BigDecimal insuranceFeeAmt;
    private BigDecimal othersFeeAmt;
    private BigDecimal financingAmt;
    private BigDecimal disburseAmt;
    private BigDecimal disburseDate;
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
