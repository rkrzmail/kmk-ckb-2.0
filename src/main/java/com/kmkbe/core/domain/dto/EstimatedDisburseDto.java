package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Builder
public class EstimatedDisburseDto {
    private Long productId;
    private BigDecimal financingAmount;
    private BigDecimal serviceFeeAmount;
    private BigDecimal estimatedDisburseAmount;
}
