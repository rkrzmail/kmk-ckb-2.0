package com.kmkbe.core.domain.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusLabelDto {
    private String status;
    private String statusLabel;
    private String color;
}
