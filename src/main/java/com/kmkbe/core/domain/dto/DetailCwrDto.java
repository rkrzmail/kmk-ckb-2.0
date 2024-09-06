package com.kmkbe.core.domain.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailCwrDto {
    private String cwrCode;
    private Date cwrStartDate;
    private Date cwrEndDate;
    private String currency;
    private BigDecimal plafondAmt;
    private BigDecimal remainingPlafondAmt;
    private BigDecimal realisationAmt;
    private UUID custCode;
    private String custTypeCode;
    private String custIdTypeCode;
    private String custIdNo;
    private String custName;
    private String custEmail;
    private BigDecimal financingAmt;

}
