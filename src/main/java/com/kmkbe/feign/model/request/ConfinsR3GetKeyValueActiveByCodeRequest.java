package com.kmkbe.feign.model.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ConfinsR3GetKeyValueActiveByCodeRequest implements Serializable {
  private String refMasterTypeCode;
  private String mappingCode;
  private String requestDateTime;
}
