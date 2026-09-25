package com.kmkbe.feign.model.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConfinsR3KeyAndValueObjRequest {
  private KeyAndValueObjDTO keyAndValueObj;
  private List<String> includeProperties;
  private String requestDateTime;


  @Data
  @Builder
  public static  class KeyAndValueObjDTO{
    private String key;
    private String operator;
    private String value;
  }
}
