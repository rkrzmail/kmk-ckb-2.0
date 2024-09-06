package com.kmkbe.core.domain.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CwrDto {
    private String cwrCode;
    private String bouwheerCode;
    private String custCode;
    private String bouwheerName;
    private String custName;
    private String branchCode;
    private String cwrType;
    private String cwrTypeDesc;
    private String facility;
    private Boolean isRevolving;
    private String currency;
    private Date cwrStartDate;
    private Date cwrEndDate;
    private Double plafondAmt;
    private Double realisationAmt;
    private String status;
}
