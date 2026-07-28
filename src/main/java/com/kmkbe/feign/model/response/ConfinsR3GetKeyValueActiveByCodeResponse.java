package com.kmkbe.feign.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class ConfinsR3GetKeyValueActiveByCodeResponse implements Serializable {
  @JsonProperty("ReturnObject")
  private transient List<KeyType> returnObject;

  @Data
  @Builder
  public static class KeyType {
    @JsonProperty("Key")
    private String key;
    @JsonProperty("Value")
    private String value;
  }
}
