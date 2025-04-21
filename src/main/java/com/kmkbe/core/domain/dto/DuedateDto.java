package com.kmkbe.core.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DuedateDto {
    private String debtorName;
    private String npwp;
}
