package com.kmkbe.core.domain.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignerCheckResultDto {
    private List<String> ConfinsSigners;
    private List<String> DBSigners;
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
