package com.kmkbe.modules.loan_submission.request;

import com.kmkbe.modules.loan_submission.dto.InvoiceDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CreateSimulationRequest {
    private Long productId;
    private BigDecimal financingAmount;
    private BigDecimal serviceFeeAmount;
    private BigDecimal estimatedDisburseAmount;
    private List<InvoiceDto> invoices;
}
