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
@AllArgsConstructor
public class SummaryByBranchDto implements Serializable {
    private String debtorName;
    private String npwp;
    private String bouwheerName;
    private Double totalPencairan;
    private Double jumlahPlafonAmount;
    private Double totalUtilizationAmount;
    private Double totalNilaiRetensi;
}
