package com.kmkbe.core.domain.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CwrListDto extends CwrDto implements Serializable {
    private Integer no;
    private Double financingAmt;
}
