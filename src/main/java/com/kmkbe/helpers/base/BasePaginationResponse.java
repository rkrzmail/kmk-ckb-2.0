package com.kmkbe.helpers.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BasePaginationResponse extends BaseResponse{

  private Pagination pagination;
}
