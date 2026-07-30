package com.kmkbe.feign.model.request;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
public class CsulGetZipCodeRequest implements Serializable {
  private String zipcode;
}
