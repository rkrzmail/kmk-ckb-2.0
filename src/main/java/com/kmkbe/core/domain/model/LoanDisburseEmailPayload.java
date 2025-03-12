package com.kmkbe.core.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LoanDisburseEmailPayload {
    private String financingCode;
    private String companyName;
    private String phoneNumber;

    private String email;

    private String toEmail;
    private String ccEmail;

    /**
     * date format with dd/MM/yyyy see DateSerializer
     */
    private String applicationDate;

    /**
     * Start Should be used BigDecimal, but with performance issue. used direct string
     */
    private String invoiceAmt;

    /**
     * Start Should be used BigDecimal, but with performance issue. used direct string
     */
    private String retention;

    /**
     * Start Should be used BigDecimal, but with performance issue. used direct string
     */
    private String financingAmt;

    /**
     * Start Should be used BigDecimal, but with performance issue. used direct string
     */
    private String totalFeeAmt;

    /**
     * Start Should be used BigDecimal, but with performance issue. used direct string
     */
    private String disburseAmt;

    private Long tenor;

    /**
     * date format with dd/MM/yyyy see DateSerializer
     */
    private String financingDueDate;

    private List<InvoiceEmailPayload> invoices;
}
