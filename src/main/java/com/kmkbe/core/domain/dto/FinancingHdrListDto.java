package com.kmkbe.core.domain.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FinancingHdrListDto extends FinancingHdrDto {
    private Integer no;
}
