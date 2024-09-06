package com.kmkbe.core.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Builder
@Getter
public class AgreementDto implements Serializable {
    private Integer no;
    private String cwrCode;
    private String agreementNo;
    private UUID financingHdrCode;
    private UUID bouwheerCode;
    private UUID custCode;
    private String bouwheerName;
    private String custName;
    private String bankName;
    private String rekeningNo;
    private BigDecimal financingAmt;
    private Date disburseDate;
    private BigDecimal disburseAmt;
    private String currency;
}
