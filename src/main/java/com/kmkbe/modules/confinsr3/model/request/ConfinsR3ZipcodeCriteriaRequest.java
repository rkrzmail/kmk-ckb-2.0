package com.kmkbe.modules.confinsr3.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kmkbe.helpers.base.BaseRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfinsR3ZipcodeCriteriaRequest extends BaseRequest {
  @JsonProperty("propName")
  private String propName;
  @JsonProperty("value")
  private String value;
}
