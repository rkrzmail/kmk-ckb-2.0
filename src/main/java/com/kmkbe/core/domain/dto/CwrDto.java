package com.kmkbe.core.domain.dto;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO for {@link com.kmkbe.core.domain.entity.Cwr}
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CwrDto implements Serializable {
    private String bouwheerCode;
    private String custCode;
    private String bouwheerName;
    private String custName;
    private String cwrCode;
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
