package com.kmkbe.core.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class VisitorDto {
    private String debtorName;
    private Boolean debtorStatus;
    private String bouwheerName;
}
