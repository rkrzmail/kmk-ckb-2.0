package com.kmkbe.core.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PencarianPayload {
    private String customer_name;
    private String bank_name;
    private String account_number;
    private String disbursement_date;
    private String total_disbursement;


    private List<InvoiceEmailPayload> invoices;
}
