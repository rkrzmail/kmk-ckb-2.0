package com.kmkbe.core.domain.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignerCheckResultDto {
    private List<String> ConfinsSigners;          // Semua signer dari API eksternal
    private List<String> DBSigners;     // Yang match dengan database
    private List<String> unmatchedSigners;

  @Override
  public String toString() {
    return "SignerCheckResultDto{" +
      "ConfinsSigners=" + ConfinsSigners +
      ", DBSigners=" + DBSigners +
      ", unmatchedSigners=" + unmatchedSigners +
      '}';
  }
}
