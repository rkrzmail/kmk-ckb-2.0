package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@NoArgsConstructor
public class ProductDto implements Serializable {
    private Long productId;
    private String productCode;
    private String branchCode;
    private UUID bouwheerCode;
    private String bouwheerName;
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
