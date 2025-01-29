package com.kmkbe.core.domain.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisburseInvoiceDto {
    private String agreementNo;
    private String custName;
    private String bouwheerName;
    private Date disburseDate;
    private Date paidDate;
    private BigDecimal retentionRefund;
    private BigDecimal paidAmount;
    private BigDecimal disburseAmount;
    private Date retentionRefundDate;
    private StatusLabelDto status;
}
