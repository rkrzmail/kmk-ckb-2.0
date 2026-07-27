package com.kmkbe.helpers.utils;

import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.helpers.base.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;

public final class PageableUtil {

  private static final String ASC_VALUE = "ASC";

  private static final String SORT="[a-zA-Z]";
  private PageableUtil() {

  }

  public static PageRequest createPageRequestNative(BasePaginationRequest req, Integer defaultPageSize, Integer defaultPageNumber, String defaultSortBy, String defaultSortType) {

    if (req.getSortBy() != null && req.getSortBy().matches(SORT)) {
      req.setSortBy(LOWER_CAMEL.to(LOWER_UNDERSCORE, req.getSortBy()));
    }

    if (defaultSortBy.matches(SORT)) {
      defaultSortBy = LOWER_CAMEL.to(LOWER_UNDERSCORE, defaultSortBy);
    }

    if (req.getPageSize() != null) {
      defaultPageSize = req.getPageSize();
    }

    if (req.getPageNumber() != null) {
      defaultPageNumber = req.getPageNumber();
    }

    if (req.getSortBy() != null) {
      defaultSortBy = req.getSortBy();
    }

    if (req.getSortType() != null) {
      defaultSortType = req.getSortType();
    }

    //paging in jpa starts from 0
    return PageRequest.of(defaultPageNumber - 1, defaultPageSize, ASC_VALUE.equalsIgnoreCase(defaultSortType) ? Sort.Direction.ASC : Sort.Direction.DESC, defaultSortBy);

  }

  public static PageRequest createPageRequest(BasePaginationRequest req, Integer defaultPageSize, Integer defaultPageNumber, String defaultSortBy, String defaultSortType) {
    if (req.getSortBy() != null && req.getSortBy().contains("_")) {
      req.setSortBy(LOWER_UNDERSCORE.to(LOWER_CAMEL, req.getSortBy()));
    }

    if (defaultSortBy.contains("_")) {
      defaultSortBy = LOWER_UNDERSCORE.to(LOWER_CAMEL, defaultSortBy);
    }


    if (req.getPageSize() != null) {
      defaultPageSize = req.getPageSize();
    }

    if (req.getPageNumber() != null) {
      defaultPageNumber = req.getPageNumber();
    }

    if (req.getSortBy() != null) {
      defaultSortBy = req.getSortBy();
    }

    if (req.getSortType() != null) {
      defaultSortType = req.getSortType();
    }

    //paging in jpa starts from 0
    return PageRequest.of(defaultPageNumber - 1, defaultPageSize, ASC_VALUE.equalsIgnoreCase(defaultSortType) ? Sort.Direction.ASC : Sort.Direction.DESC, defaultSortBy);

  }

  public static <T> Pagination pageToPagination(Page<T> page) {
    return Pagination.builder()
      .pageSize(page.getSize())

      //paging in jpa starts from 0, so need to plus one for back to user page number
      .currentPage(page.getNumber() + 1)
      .totalPages(page.getTotalPages())
      .totalRecords(page.getTotalElements())
      .isFirstPage(page.isFirst())
      .isLastPage(page.isLast())
      .build();
  }

}
