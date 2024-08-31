package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributionSubmissionDto {
    private String financingHdrCode;
    private String custName;
    private String bouwheerName;
    private String city;
    private Date dueDate;
    private BigDecimal financingAmount;
    private String branchRecommendedCode;
    private String branchRecommended;
    private String currentBranchCode;
    private String currentBranch;
    private String custStatus;
    private StatusLabelDto status;
    private Instant dtmCrt;
}
