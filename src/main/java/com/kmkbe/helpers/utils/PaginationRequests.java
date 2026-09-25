package com.kmkbe.helpers.utils;

import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.exception.BusinessException;
import org.springframework.http.HttpStatus;

public final class PaginationRequests {
  private PaginationRequests() {}

  // These legacy endpoints allow omitted paging/search/sort fields.
  public static PaginationRequest from(BasePaginationRequest source) {
    if ((source.getPageNo() != null && source.getPageNo() < 1)
      || (source.getPageSize() != null && source.getPageSize() < 1)) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, 400, "pageNo dan pageSize harus minimal 1.");
    }
    var target = new PaginationRequest();
    target.setPageNo(source.getPageNo());
    target.setPageSize(source.getPageSize());
    target.setSortBy(source.getSortBy());
    target.setSortType(source.getSortType());
    if (source.getSearchValue() != null && !source.getSearchValue().isBlank()) {
      if (source.getSearchBy() == null || source.getSearchBy().isBlank()) {
        throw new BusinessException(HttpStatus.BAD_REQUEST, 400, "searchBy wajib diisi jika searchValue digunakan.");
      }
      target.setSearchBy(source.getSearchBy().trim());
      target.setSearchValue(source.getSearchValue().trim());
    }
    return target;
  }
}
