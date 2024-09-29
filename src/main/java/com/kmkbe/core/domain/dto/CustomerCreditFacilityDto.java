package com.kmkbe.core.domain.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreditFacilityDto {
    private Integer no;
    private String agreementCode;
    private String bouwheerCode;
    private String bouwheerName;
    private String invoiceDescription;
    private Date invoiceDate;
    private Date verifDate;
    private Date disburseDate;
    private Date facilityDueDate;
    private BigDecimal invoiceAmount;
    private String status;
    private String statusLabel;
    private Boolean hasAction;
}
