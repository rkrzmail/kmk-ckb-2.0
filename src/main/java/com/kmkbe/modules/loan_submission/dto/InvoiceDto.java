package com.kmkbe.modules.loan_submission.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@NoArgsConstructor
public class InvoiceDto {
    private UUID invoiceCode;
    private String bouwheerInvNo;
    private String custInvNo;
    private String invoiceDescription;
    private Instant invoiceDate;
    private Instant invoiceDueDate;
    private Double invoiceAmt;
}
