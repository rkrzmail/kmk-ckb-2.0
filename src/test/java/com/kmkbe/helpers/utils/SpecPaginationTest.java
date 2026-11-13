package com.kmkbe.helpers.utils;

import com.kmkbe.core.domain.request.PaginationRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpecPaginationTest {

  private static class IntSpec extends SpecPagination<Integer, Integer> {
    IntSpec(List<Integer> data) {
      super(data);
    }

    IntSpec(List<Integer> data, PaginationRequest request) {
      super(data, request);
    }

    @Override
    public Integer eval(Integer data) {
      return data;
    }

    @Override
    public Integer search(Integer data) {
      return getSearchValue().equals(String.valueOf(data)) ? data : null;
    }

    @Override
    public void sort(List<Integer> data) {
      data.sort(Comparator.naturalOrder());
    }
  }

  @Test
  void paginatesAndSortsWithDefaults() {
    var result = SpecPagination.paginationData(new IntSpec(List.of(3, 1, 2)));

    assertThat(result.getCurrentPage()).isEqualTo(1);
    assertThat(result.getTotalPage()).isEqualTo(1);
    assertThat(result.getTotalData()).isEqualTo(3L);
    assertThat(result.getList()).containsExactly(1, 2, 3);
  }

  @Test
  void slicesRequestedPage() {
    PaginationRequest request = new PaginationRequest();
    request.setPageNo(2);
    request.setPageSize(2);

    var result = SpecPagination.paginationData(new IntSpec(List.of(3, 1, 2), request));

    assertThat(result.getCurrentPage()).isEqualTo(2);
    assertThat(result.getTotalPage()).isEqualTo(2);
    assertThat(result.getList()).containsExactly(3);
  }

  @Test
  void appliesSearchFilter() {
    PaginationRequest request = new PaginationRequest();
    request.setSearchBy("value");
    request.setSearchValue("3");

    var result = SpecPagination.paginationData(new IntSpec(List.of(1, 2, 3, 4), request));

    assertThat(result.getTotalData()).isEqualTo(1L);
    assertThat(result.getList()).containsExactly(3);
  }

  @Test
  void searchHelpers() {
    PaginationRequest request = new PaginationRequest();
    request.setSearchBy("name");
    request.setSearchValue("ell");
    IntSpec spec = new IntSpec(List.of(), request);

    assertThat(spec.isSearchBy("NAME")).isTrue();
    assertThat(spec.isSearchUsed()).isTrue();
    assertThat(spec.like("Hello")).isTrue();
    assertThat(spec.equal("ell")).isTrue();
    assertThat(spec.equal("nope")).isFalse();
    assertThat(spec.equalNumber(42.0)).isFalse();
  }

  @Test
  void equalNumberAndEqualDate() {
    PaginationRequest request = new PaginationRequest();
    request.setSearchValue("42");
    assertThat(new IntSpec(List.of(), request).equalNumber(42.0)).isTrue();

    Date date = Date.from(LocalDate.of(2026, 5, 12).atStartOfDay(ZoneId.systemDefault()).toInstant());
    PaginationRequest dateRequest = new PaginationRequest();
    dateRequest.setSearchValue(Utils.formatDateView(date));
    assertThat(new IntSpec(List.of(), dateRequest).equalDate(date)).isTrue();
  }

  @Test
  void searchNotUsedWhenRequestNullOrIncomplete() {
    assertThat(new IntSpec(List.of(1)).isSearchUsed()).isFalse();

    PaginationRequest request = new PaginationRequest();
    request.setSearchBy("name");
    assertThat(new IntSpec(List.of(1), request).isSearchUsed()).isFalse();
  }
}
