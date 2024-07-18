package com.kmkbe.modules.loan_submission.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Builder
public class CreatedSimulationDto {
    private Long productId;
    private UUID financingHdrCode;
    private List<InvoiceDto> invoices;
}
