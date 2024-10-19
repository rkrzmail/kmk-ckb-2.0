package com.kmkbe.core.domain.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreditFacilityNewDto {

    private String agreementCode;
    private String financingHdrCode;
    private String custName;
    private String bouwheerCode;
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
