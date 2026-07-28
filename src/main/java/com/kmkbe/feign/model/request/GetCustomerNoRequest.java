package com.kmkbe.feign.model.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class GetCustomerNoRequest implements Serializable {
  private String requestDateTime;
  private String rowVersion;
  private String custNo;
}
