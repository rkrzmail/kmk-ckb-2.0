package com.kmkbe.modules.loan_submission.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class CalculateSimulationRequest {
    @NotEmpty(message = "Please select at least 1 invoice to get calculated invoice amount")
    private BigDecimal totalInvoiceAmount;

    @NotEmpty(message = "Please swipe the indicator to get calculated percentage")
    private Double disbursePercentage;
}
