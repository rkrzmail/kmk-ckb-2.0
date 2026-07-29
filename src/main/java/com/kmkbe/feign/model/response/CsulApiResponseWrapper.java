package com.kmkbe.feign.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CsulApiResponseWrapper<T> {
  private String status;

  @JsonProperty("status_code")
  private int statusCode;

  private T data;
}

