package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AgreementByNumberResponse(
  @JsonProperty("AgrmntNo") String agreementNumber,
  @JsonProperty("AgrmntCreatedDt") LocalDateTime agreementCreatedDate,
  @JsonProperty("StatusCode") String statusCode
) {}
