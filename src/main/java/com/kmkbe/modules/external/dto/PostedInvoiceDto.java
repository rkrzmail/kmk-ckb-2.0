package com.kmkbe.modules.external.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Data
@Builder
public class PostedInvoiceDto {
    private final String bouwheerCode;
    private final String bouwheerName;
    private final String customerInvoiceNo;
    private final String bouwheerInvoiceNo;
    private final String invoiceDescription;
    private final String currencyCode;
    private final Date invoiceDate;
    private final Date invoiceDueDate;
    private final Double invoiceAmount;
}
