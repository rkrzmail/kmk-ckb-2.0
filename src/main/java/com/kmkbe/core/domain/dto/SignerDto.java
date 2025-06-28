package com.kmkbe.core.domain.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignerDto {
    private UUID financingHdrCode;
    private UUID custCode;
    private String custName;
    private String bouwheerName;
    private String custStatus;
}
