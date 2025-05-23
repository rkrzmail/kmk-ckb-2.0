package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
@NoArgsConstructor
public class SummaryByAODto implements Serializable {

    private double totalDisbursement;
    private double totalUtilizationAmount;
    private String customerName;
    private String bouwheerName;
    private double plafondAmount;
    private double retentionAmount;
    private String branch;
    private String aoName;

    // Constructor, Getters, and Setters
    public SummaryByAODto(
            double totalDisbursement,
            double totalUtilizationAmount,
            String customerName,
            String bouwheerName,
            double plafondAmount,
            double retentionAmount,
            String branch,
            String aoName

    ) {

        this.totalDisbursement = totalDisbursement;
        this.totalUtilizationAmount = totalUtilizationAmount;
        this.customerName = customerName;
        this.bouwheerName = bouwheerName;
        this.plafondAmount = plafondAmount;
        this.retentionAmount = retentionAmount;
        this.branch = branch;
        this.aoName = aoName;
    }

}
