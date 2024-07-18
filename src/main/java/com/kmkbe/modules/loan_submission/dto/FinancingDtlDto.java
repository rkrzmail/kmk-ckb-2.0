package com.kmkbe.modules.loan_submission.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link com.kmkbe.modules.loan_submission.entity.FinancingDtl}
 */
@Getter
@Setter
@NoArgsConstructor
public class FinancingDtlDto implements Serializable {
    private UUID financingDtlCode;
    private Long financingDtlId;
    private String bouwheerInvNo;
    private Long invoiceSeqno;
    private Instant paidToCustDate;
    private Instant bouwheerPaidDate;
    private InvoiceDto invoice;
}
