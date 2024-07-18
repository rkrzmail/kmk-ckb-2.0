package com.kmkbe.modules.loan_submission.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class PostedInvoicePayload {


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
