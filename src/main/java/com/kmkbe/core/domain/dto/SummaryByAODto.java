package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

import java.io.Serializable;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
@NoArgsConstructor
public class SummaryByAODto implements Serializable {
    private String aoName;
    private String branch;
    private String customerName;
    private String bouwheerName;
    private double totalDisbursement;
    private double plafondAmount;
    private double totalUtilizationAmount;
    private double retentionAmount;

    // Constructor, Getters, and Setters
    public SummaryByAODto(String aoName,
                          String branch,
                          String customerName,
                          String bouwheerNameName,
                          double totalDisbursement,
                          double plafondAmount,
                          double totalUtilizationAmount,
                          double retentionAmount) {

        this.aoName = aoName;
        this.customerName = customerName;
        this.bouwheerName = bouwheerNameName;
        this.branch = branch;
        this.totalDisbursement = totalDisbursement;
        this.plafondAmount = plafondAmount;
        this.totalUtilizationAmount = totalUtilizationAmount;
        this.retentionAmount = retentionAmount;
    }

}
