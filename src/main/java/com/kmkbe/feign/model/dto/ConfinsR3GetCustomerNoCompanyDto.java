package com.kmkbe.feign.model.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfinsR3GetCustomerNoCompanyDto{
  @JsonProperty("CustObj")
  private transient ConfinsR3GetCustomerDto customer;

  @JsonProperty("CustCompanyObj")
  private transient ConfinsR3GetCustomerCompanyInfoDto customerCampany;
}
