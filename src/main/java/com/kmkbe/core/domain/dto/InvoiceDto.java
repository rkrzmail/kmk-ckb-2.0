package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
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
    private LocalDateTime invoiceDate;
    private LocalDateTime invoiceDueDate;
    private BigDecimal invoiceAmt;
    private String poNumber;
    private Date postingDate;
    private String test;

    //private BouwheerDto bouwheer;
}
