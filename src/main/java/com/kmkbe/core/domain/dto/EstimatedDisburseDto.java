package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.kmkbe.modules.product.model.entity.Product;
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
    private BigDecimal provisionFeeAmount;
    private BigDecimal interestFeeAmount;
    private BigDecimal adminFeeAmount;
    private BigDecimal othersFeeAmount;
    private BigDecimal legalFeeAmount;
    private BigDecimal surveyFeeAmount;
    private BigDecimal totalInvoiceAmount;

    private Double provisionRate;
    private Double effectiveRate;
    private Double adminRate;
    private Product product;
}
