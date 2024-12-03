package com.kmkbe.core.domain.dto;

import com.kmkbe.core.domain.entity.FinancingDtl;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link FinancingDtl}
 */
@Getter
@Setter
@NoArgsConstructor
public class FinancingDtlDto implements Serializable {
    private UUID financingDtlCode;
    private Long financingDtlId;
    private String bouwheerInvNo;
    private LocalDateTime paidToCustDate;
    private LocalDateTime bouwheerPaidDate;
    private InvoiceDto invoice;
}
