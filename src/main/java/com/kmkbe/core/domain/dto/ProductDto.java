package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
//@NoArgsConstructor
public class ProductDto implements Serializable {
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
