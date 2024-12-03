package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.kmkbe.core.domain.entity.SimulationHist;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link SimulationHist}
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationHistDto implements Serializable {
    private UUID simulationHistCode;

    private Double totalInvoiceAmt;

    private Double retention;

    private Double adminAmt;

    private Double financingAmt;

    private Boolean isUsed;

    private LocalDateTime dtmCrt;

    private FinancingHdrDto financingHdr;
}
