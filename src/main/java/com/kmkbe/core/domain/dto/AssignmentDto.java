package com.kmkbe.core.domain.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDto {
    private UUID financingHdrCode;
    private String agreementCode;
    private UUID custCode;
    private String custName;
    private UUID bouwheerCode;
    private String bouwheerName;
    private Date verifDate;
    private Date dueDate;
    private BigDecimal financingAmount;
    private String custStatus;
    private String status;
    private String statusLabel;
    private LocalDateTime dtmCrt;
    private String agreementDoc;
}
