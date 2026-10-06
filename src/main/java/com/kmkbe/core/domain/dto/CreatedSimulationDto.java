package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Builder
public class CreatedSimulationDto {
    private Long productId;
    private UUID financingHdrCode;
    private BigDecimal totalInvoiceAmount;
    private List<InvoiceDto> invoices;
}
