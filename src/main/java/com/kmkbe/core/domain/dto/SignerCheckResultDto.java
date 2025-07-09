package com.kmkbe.core.domain.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignerCheckResultDto {
    private List<String> allSigners;          // Semua signer dari API eksternal
    private List<String> matchedSigners;     // Yang match dengan database
    private List<String> unmatchedSigners;
}
