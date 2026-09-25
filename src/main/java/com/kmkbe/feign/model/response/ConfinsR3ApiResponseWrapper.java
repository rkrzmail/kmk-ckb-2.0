package com.kmkbe.feign.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kmkbe.helpers.base.BaseResponse;
import lombok.*;

import java.util.List;

@Data
@Builder
public class ConfinsR3ApiResponseWrapper<T> extends BaseResponse {
  @JsonProperty("StatusCode")
  private String code;

  @JsonProperty("Message")// HTTP status code received from the service layer (200, 404, etc.)
  private String message;             // User-friendly confirmation/error text.

  /** The primary container for data (the list of records). */
  @JsonProperty("Data")
  private List<T> data;

  @JsonProperty("Count")
  private int count;
}
