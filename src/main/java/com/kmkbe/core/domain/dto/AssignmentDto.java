package com.kmkbe.core.domain.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDto {
    private String financingHdrCode;
    private String custName;
    private String bouwheerName;
    private Date verifDate;
    private Date dueDate;
    private BigDecimal financingAmount;
    private String custStatus;
    private String status;
    private String statusLabel;
    private Instant dtmCrt;
}
