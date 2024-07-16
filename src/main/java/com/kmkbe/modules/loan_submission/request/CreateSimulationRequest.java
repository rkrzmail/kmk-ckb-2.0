package com.kmkbe.modules.loan_submission.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CreateSimulationRequest {
    private Long productId;
    private Double disbursePercentage;
    private List<CreatePostedInvoice> invoices;

    @JsonIgnore
    private SimulationDisburse disburse;

    public record SimulationDisburse(
            BigDecimal financingAmount,
            BigDecimal estimatedDisburseAmount,
            Date maxInvoiceDate,
            Double totalInvoiceAmount
    ) {
    }

    public record CreatePostedInvoice(
            String bouwheerCode,
            String bouwheerName,
            String customerInvoiceNo,
            String bouwheerInvoiceNo,
            String invoiceDescription,
            String currencyCode,
            Date invoiceDate,
            Date invoiceDueDate,
            Double invoiceAmount
    ) {
    }
}
