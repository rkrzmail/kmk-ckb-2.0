package com.kmkbe.modules.loan_submission.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link com.kmkbe.modules.loan_submission.entity.SimulationHist}
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

    private Instant dtmCrt;

    private FinancingHdrDto financingHdr;
}
