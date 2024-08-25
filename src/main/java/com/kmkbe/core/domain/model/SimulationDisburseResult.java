package com.kmkbe.core.domain.model;


import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Builder
public class SimulationDisburseResult {
    private BigDecimal financingAmount;
    private BigDecimal estimatedDisburseAmount;
    private Date maxInvoiceDate;
    private Double totalInvoiceAmount;
}
