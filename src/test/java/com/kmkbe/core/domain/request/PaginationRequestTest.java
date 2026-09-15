package com.kmkbe.core.domain.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.web.bind.WebDataBinder;

import static org.assertj.core.api.Assertions.assertThat;

class PaginationRequestTest {

  @ParameterizedTest
  @ValueSource(strings = {"asc", "desc"})
  void bindsSortingAlongsideExistingPaginationAndSearch(String sortType) {
    PaginationRequest request = new PaginationRequest();
    WebDataBinder binder = new WebDataBinder(request);
    MutablePropertyValues parameters = new MutablePropertyValues();
    parameters.add("sortBy", "dtmCrt");
    parameters.add("sortType", sortType);
    parameters.add("pageNo", "2");
    parameters.add("pageSize", "10");
    parameters.add("searchBy", "custName");
    parameters.add("searchValue", "Vendor");

    binder.bind(parameters);

    assertThat(binder.getBindingResult().hasErrors()).isFalse();
    assertThat(request.getSortBy()).isEqualTo("dtmCrt");
    assertThat(request.getSortType()).isEqualTo(sortType);
    assertThat(request.getPageNo()).isEqualTo(2);
    assertThat(request.getPageSize()).isEqualTo(10);
    assertThat(request.getSearchBy()).isEqualTo("custName");
    assertThat(request.getSearchValue()).isEqualTo("Vendor");
  }

  @Test
  void existingRequestWithoutSortingKeepsSortingUnset() {
    PaginationRequest request = new PaginationRequest();
    WebDataBinder binder = new WebDataBinder(request);
    MutablePropertyValues parameters = new MutablePropertyValues();
    parameters.add("pageNo", "1");
    parameters.add("pageSize", "5");

    binder.bind(parameters);

    assertThat(binder.getBindingResult().hasErrors()).isFalse();
    assertThat(request.getPageNo()).isEqualTo(1);
    assertThat(request.getPageSize()).isEqualTo(5);
    assertThat(request.getSortBy()).isNull();
    assertThat(request.getSortType()).isNull();
  }

  @Test
  void sortingFieldsCanBeSetIndependently() {
    PaginationRequest request = new PaginationRequest();
    request.setSortBy("cwrStartDate");

    assertThat(request.getSortBy()).isEqualTo("cwrStartDate");
    assertThat(request.getSortType()).isNull();

    request.setSortType("desc");

    assertThat(request.getSortType()).isEqualTo("desc");
  }
}
