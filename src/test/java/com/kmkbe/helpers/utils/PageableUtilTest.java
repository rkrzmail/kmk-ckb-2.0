package com.kmkbe.helpers.utils;

import com.kmkbe.helpers.base.BasePaginationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageableUtilTest {

  @Test
  void createPageRequestConvertsSnakeCaseAndAppliesOverrides() {
    BasePaginationRequest req = new BasePaginationRequest();
    req.setSortBy("dtm_crt");
    req.setSortType("asc");
    req.setPageNo(3);
    req.setPageSize(5);

    PageRequest page = PageableUtil.createPageRequest(req, 10, 1, "id", "desc");

    assertThat(page.getPageNumber()).isEqualTo(2);
    assertThat(page.getPageSize()).isEqualTo(5);
    Sort.Order order = page.getSort().getOrderFor("dtmCrt");
    assertThat(order).isNotNull();
    assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
  }

  @Test
  void createPageRequestUsesDefaultsWhenRequestEmpty() {
    PageRequest page = PageableUtil.createPageRequest(new BasePaginationRequest(), 10, 1, "id", "desc");

    assertThat(page.getPageNumber()).isZero();
    assertThat(page.getPageSize()).isEqualTo(10);
    assertThat(page.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.DESC);
  }

  @Test
  void createPageRequestNativeAppliesPaging() {
    BasePaginationRequest req = new BasePaginationRequest();
    req.setPageNo(1);
    req.setPageSize(20);
    req.setSortType("desc");

    PageRequest page = PageableUtil.createPageRequestNative(req, 10, 1, "id", "asc");

    assertThat(page.getPageNumber()).isZero();
    assertThat(page.getPageSize()).isEqualTo(20);
    assertThat(page.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.DESC);
  }

  @Test
  void pageToPaginationShiftsPageNumberByOne() {
    var page = new PageImpl<>(List.of("a", "b"), PageRequest.of(0, 2), 2L);

    var pagination = PageableUtil.pageToPagination(page);

    assertThat(pagination.getCurrentPage()).isEqualTo(1);
    assertThat(pagination.getPageSize()).isEqualTo(2);
    assertThat(pagination.getTotalPages()).isEqualTo(1);
    assertThat(pagination.getTotalRecords()).isEqualTo(2L);
    assertThat(pagination.getIsFirstPage()).isTrue();
    assertThat(pagination.getIsLastPage()).isTrue();
  }
}
