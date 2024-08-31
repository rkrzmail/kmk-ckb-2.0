package com.kmkbe.core.domain.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CwrListDto extends CwrDto implements Serializable {
    private Integer no;
}
