package com.kmkbe.modules.loan_submission.dto;

import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.loan_submission.entity.Bouwheer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link com.kmkbe.modules.loan_submission.entity.FinancingHdr}
 */
@Getter
@Setter
@NoArgsConstructor
public class FinancingHdrDto implements Serializable {
    private UUID financingHdrCode;
    private Long financingHdrId;
    private Instant financingDate;
    private String currencyCode;
    private Long invoiceQty;
    private String interestType;
    private Long tenor;
    private Double effectiveRate;
    private Double interestAmt;
    private Long termOfPayment;
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
    private Instant disburseDate;
    private Instant financingDueDate;
    private String financingStatus;
    private String usrCrt;
    private Instant dtmCrt;
    private String usrUpd;
    private Instant dtmUpd;
    private Customer customer;
    private Bouwheer bouwheer;
    private List<FinancingDtlDto> details;
}
