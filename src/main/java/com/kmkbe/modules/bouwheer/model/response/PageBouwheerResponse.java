package com.kmkbe.modules.bouwheer.model.response;

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
public class PageBouwheerResponse extends BasePaginationResponse {
  private List<BouwheerResponse> content;
  private Pagination pagination;
}
