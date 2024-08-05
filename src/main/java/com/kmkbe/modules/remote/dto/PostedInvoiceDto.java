package com.kmkbe.modules.remote.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Builder
public class PostedInvoiceDto {
    private String bouwheerCode;
    private String bouwheerName;
    private String customerInvoiceNo;
    private String bouwheerInvoiceNo;
    private String invoiceDescription;
    private String currencyCode;
    private Date invoiceDate;
    private Date invoiceDueDate;
    private BigDecimal invoiceAmount;
}
