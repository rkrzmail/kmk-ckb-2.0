package com.kmkbe.core.domain.dto;

import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link FinancingHdr}
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinancingHdrDto implements Serializable {
    private UUID financingHdrCode;
    private LocalDateTime financingDate;
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
    private LocalDateTime disburseDate;
    private LocalDateTime financingDueDate;
    private String financingStatus;
    private LocalDateTime dtmCrt;
    private Customer customer;
    private Bouwheer bouwheer;
    private List<FinancingDtlDto> details;
    private SimulationHistDto simulationHist;//optional
}
