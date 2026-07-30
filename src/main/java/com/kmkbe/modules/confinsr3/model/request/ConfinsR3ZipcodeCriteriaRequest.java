package com.kmkbe.modules.confinsr3.model.request;

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
  private String propName;
  private String value;
}
