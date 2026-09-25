package com.kmkbe.modules.customer.model.response;

import com.kmkbe.helpers.base.BasePaginationResponse;
import com.kmkbe.helpers.base.Pagination;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageCustomerResponse extends BasePaginationResponse {
  private List<CustomerResponse> content;
  private Pagination pagination;
}
