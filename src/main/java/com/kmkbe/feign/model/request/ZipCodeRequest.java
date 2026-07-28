package com.kmkbe.feign.model.request;

import lombok.*;

import java.io.Serializable;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ZipCodeRequest implements Serializable {
  private String zipcode;
}
