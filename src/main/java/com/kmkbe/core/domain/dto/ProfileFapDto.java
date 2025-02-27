package com.kmkbe.core.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Builder
public class ProfileFapDto {
    private String bouwheerCode;
    private String bouwheerName;
    private String customerInvoiceNo;
    private String bouwheerInvoiceNo;
    private String poNumber;
    private String invoiceDescription;
    private String currencyCode;



    @Getter
    @Builder
    public static class AmountConverter {
        private BigDecimal base;
        private String fromCurrencyCode;
        private String toCurrencyCode;
        private BigDecimal amount;
    }
}
