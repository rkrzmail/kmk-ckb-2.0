package com.kmkbe.core.domain.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDto {
    private UUID financingHdrCode;
    private UUID custCode;
    private String custName;
    private String bouwheerName;
    private Date verifDate;
    private Date dueDate;
    private BigDecimal financingAmount;
    private String custStatus;
    private String status;
    private String statusLabel;
    private Instant dtmCrt;
    private String agreementDoc;
}
