package com.kmkbe.modules.confinsr3.model.response;

import com.kmkbe.helpers.base.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetCustomerNoCompanyResponse extends BaseResponse {
  private transient GetCustomerResponse customer;
  private transient GetCustomerCompanyInfoResponse customerCampany;
}
