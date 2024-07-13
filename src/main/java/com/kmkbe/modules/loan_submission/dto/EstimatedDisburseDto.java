package com.kmkbe.modules.loan_submission.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@NoArgsConstructor
public class EstimatedDisburseDto {
    private Long productId;
    private BigDecimal financingAmount;
    private BigDecimal serviceFeeAmount;
    private BigDecimal estimatedDisburseAmount;
}
