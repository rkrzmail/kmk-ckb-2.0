package com.kmkbe.core.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProyeksiDto {
    private String debtorName;
    private Boolean debtorStatus;
    private String bouwheerName;
    private String cwrNo;
}
