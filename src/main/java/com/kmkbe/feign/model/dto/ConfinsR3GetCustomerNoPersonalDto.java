package com.kmkbe.feign.model.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfinsR3GetCustomerNoPersonalDto {
  @JsonProperty("CustObj")
  private transient ConfinsR3GetCustomerDto customer;

  @JsonProperty("CustPersonalObj")
  private transient ConfinsR3GetCustomerPersonalInfoDto customerPersonal;
}
