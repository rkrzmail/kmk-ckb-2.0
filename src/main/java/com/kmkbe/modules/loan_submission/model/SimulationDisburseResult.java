package com.kmkbe.modules.loan_submission.model;


import com.kmkbe.modules.loan_submission.dto.InvoiceDto;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Builder
public class SimulationDisburseResult {
    private BigDecimal financingAmount;
    private BigDecimal estimatedDisburseAmount;
    private Date maxInvoiceDate;
    private Double totalInvoiceAmount;
    private List<InvoiceDto> createdInvoices;
}
