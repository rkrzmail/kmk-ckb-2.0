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
//@AllArgsConstructor
public class SummaryByBranchDto implements Serializable {
    private String debtorName;
    private String branchCode;
    private String npwp;
    private String bouwheerName;
    private Double totalPencairan;
    private Double plafondAmount;
    private Double utilizationAmount;
    private Double retentionAmt;

    // Constructor with the correct parameters (custom constructor)
    public SummaryByBranchDto(String debtorName, String branchCode, String npwp,
                              String bouwheerName, Double totalPencairan,
                              Double plafondAmount, Double utilizationAmount,
                              Double retentionAmt) {
        this.debtorName = debtorName;
        this.branchCode = branchCode;
        this.npwp = npwp;
        this.bouwheerName = bouwheerName;
        this.totalPencairan = totalPencairan;
        this.plafondAmount = plafondAmount;
        this.utilizationAmount = utilizationAmount;
        this.retentionAmt = retentionAmt;
    }
}
