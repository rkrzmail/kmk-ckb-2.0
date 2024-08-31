package com.kmkbe.core.domain.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryCwrDto {
    private String cwrCode;
    private Date cwrStartDate;
    private Date cwrEndDate;
    private String currency;
    private BigDecimal plafondAmt;
    private BigDecimal loanAmt;
}
