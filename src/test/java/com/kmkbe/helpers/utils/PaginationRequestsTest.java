package com.kmkbe.helpers.utils;

import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.exception.BusinessException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class PaginationRequestsTest {
  @Test
  void preservesPagingSortingAndTrimsSearch() {
    var source = new BasePaginationRequest(5, 2, "custName", "desc", " NamaDebitur ", " Vendor ");
    var result = PaginationRequests.from(source);
    assertThat(result.getPageNo()).isEqualTo(2);
    assertThat(result.getPageSize()).isEqualTo(5);
    assertThat(result.getSortBy()).isEqualTo("custName");
    assertThat(result.getSortType()).isEqualTo("desc");
    assertThat(result.getSearchBy()).isEqualTo("NamaDebitur");
    assertThat(result.getSearchValue()).isEqualTo("Vendor");
  }

  @Test
  void acceptsLegacyOmittedFieldsAndClearedSearch() {
    assertThat(PaginationRequests.from(new BasePaginationRequest()).getPageNo()).isNull();
    var source = new BasePaginationRequest();
    source.setSearchBy("custName");
    source.setSearchValue(" ");
    assertThat(PaginationRequests.from(source).getSearchBy()).isNull();
  }

  @Test
  void rejectsInvalidPagingAndSearchWithoutField() {
    assertThatThrownBy(() -> PaginationRequests.from(new BasePaginationRequest(0, 1, null, null, null, null)))
      .isInstanceOf(BusinessException.class);
    assertThatThrownBy(() -> PaginationRequests.from(new BasePaginationRequest(5, 0, null, null, null, null)))
      .isInstanceOf(BusinessException.class);
    assertThatThrownBy(() -> PaginationRequests.from(new BasePaginationRequest(5, 1, null, null, null, "Vendor")))
      .isInstanceOf(BusinessException.class).hasMessageContaining("searchBy wajib");
  }
}
