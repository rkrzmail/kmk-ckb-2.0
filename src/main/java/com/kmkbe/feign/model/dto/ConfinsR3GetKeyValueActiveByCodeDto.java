package com.kmkbe.feign.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfinsR3GetKeyValueActiveByCodeDto implements Serializable {
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
