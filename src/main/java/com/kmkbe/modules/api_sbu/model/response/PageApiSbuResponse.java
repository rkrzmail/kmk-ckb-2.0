package com.kmkbe.modules.api_sbu.model.response;

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
public class PageApiSbuResponse extends BasePaginationResponse {
  private List<ApiSbuResponse> content;
  private Pagination pagination;
}
