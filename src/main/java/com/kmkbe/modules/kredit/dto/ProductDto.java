package com.kmkbe.modules.kredit.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
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
    private BigDecimal ntfFrom;
    private BigDecimal ntfTo;
    private BigDecimal effectiveRate;
    private BigDecimal provisionRate;
    private BigDecimal surveyFee;
    private BigDecimal legalFee;
    private BigDecimal adminLimitFee;
    private BigDecimal adminRate;
    private BigDecimal insuranceRate;
    private BigDecimal othersFee;
    private Boolean isActive;
    private String usrCrt;
    private Instant dtmCrt;
    private String usrUpd;
    private Instant dtmUpd;
}
