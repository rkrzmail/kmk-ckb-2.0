package com.kmkbe.modules.confinsr3.model.response;

import com.kmkbe.helpers.base.BaseResponse;
import lombok.*;

@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class KeyValueActiveByCodeResponse extends BaseResponse {
  private String key;
  private String value;
}
