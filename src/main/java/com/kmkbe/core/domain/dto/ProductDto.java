package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
//@NoArgsConstructor
public class ProductDto implements Serializable {
    private Long productId;
    private String productCode;
    private String branchCode;
    private String productName;
    private LocalDateTime effectiveDate;
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
    private String status;
    private String usrCrt;
    private LocalDateTime dtmCrt;
    private String usrUpd;
    private LocalDateTime dtmUpd;
}
