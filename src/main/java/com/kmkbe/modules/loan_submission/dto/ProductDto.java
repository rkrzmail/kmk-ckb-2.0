package com.kmkbe.modules.loan_submission.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@NoArgsConstructor
public class ProductDto {
    private Long productId;
    private String branchCode;
    private String productName;
    private Instant effectiveDate;
    private Double ntfFrom;
    private Double ntfTo;
    private Double effectiveRate;
    private Double provisionRate;
    private Double surveyFee;
    private Double legalFee;
    private Double adminLimitFee;
    private Double adminRate;
    private Double insuranceRate;
    private Double othersFee;
    private Boolean isActive;
    private String usrCrt;
    private Instant dtmCrt;
    private String usrUpd;
    private Instant dtmUpd;
}
