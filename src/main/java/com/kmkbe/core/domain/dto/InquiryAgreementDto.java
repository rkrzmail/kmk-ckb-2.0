package com.kmkbe.core.domain.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryAgreementDto {
    private String bankName;
    private String rekeningNo;
    private String currency;
    private BigDecimal disburseAmt;
}
