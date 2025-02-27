package com.kmkbe.core.domain.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaidInvoiceDto {
    private String invoiceNo;
    private String custName;
    private String bouwheerName;
    private Date paidDate;

    private Date dueDate;
    private BigDecimal paidAmount;
    private StatusLabelDto status;
}
