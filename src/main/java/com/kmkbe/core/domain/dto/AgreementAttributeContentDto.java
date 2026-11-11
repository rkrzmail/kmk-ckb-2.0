package com.kmkbe.core.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AgreementAttributeContentDto(
  @JsonProperty("AppId") Integer appId,
  @JsonProperty("RefAttrCode") String refAttrCode,
  @JsonProperty("AttrValue") String attrValue
) {}
