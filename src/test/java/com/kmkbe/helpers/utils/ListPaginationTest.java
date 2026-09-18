package com.kmkbe.helpers.utils;

import com.kmkbe.core.domain.dto.SignerCsulDto;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ListPaginationTest {
  private static final Map<String, java.util.function.Function<SignerCsulDto, ? extends Comparable<?>>> FIELDS = Map.of(
    "signerId", SignerCsulDto::getSignerId,
    "karyawanName", SignerCsulDto::getKaryawanName,
    "signhubStatus", SignerCsulDto::getSignhubStatus
  );

  @Test
  void filtersAndSortsBeforePaginating() {
    var rows = List.of(
      SignerCsulDto.builder().signerId(1L).karyawanName("Andi").signhubStatus("Active").build(),
      SignerCsulDto.builder().signerId(2L).karyawanName("Budi").signhubStatus("Pending").build(),
      SignerCsulDto.builder().signerId(3L).karyawanName("Cici").signhubStatus("Active").build()
    );
    var request = new PaginationRequest();
    request.setPageNo(1);
    request.setPageSize(1);
    request.setSearchBy("signhubStatus");
    request.setSearchValue("active");
    request.setSortBy("karyawanName");
    request.setSortType("desc");

    var first = ListPagination.of(rows, request, FIELDS, "signerId");
    assertThat(first.getTotalData()).isEqualTo(2);
    assertThat(first.getTotalPage()).isEqualTo(2);
    assertThat(first.getList()).extracting(SignerCsulDto::getKaryawanName).containsExactly("Cici");

    request.setPageNo(2);
    assertThat(ListPagination.of(rows, request, FIELDS, "signerId").getList())
      .extracting(SignerCsulDto::getKaryawanName).containsExactly("Andi");
  }

  @Test
  void rejectsUnsupportedFieldsAndInvalidPaging() {
    var request = new PaginationRequest();
    request.setSortBy("unknown");
    assertThatThrownBy(() -> ListPagination.of(List.<SignerCsulDto>of(), request, FIELDS, "signerId"))
      .isInstanceOf(BusinessException.class).hasMessageContaining("sortBy tidak didukung");

    request.setSortBy("signerId");
    request.setSearchBy("unknown");
    request.setSearchValue("x");
    assertThatThrownBy(() -> ListPagination.of(List.<SignerCsulDto>of(), request, FIELDS, "signerId"))
      .isInstanceOf(BusinessException.class).hasMessageContaining("searchBy tidak didukung");

    request.setSearchValue(null);
    request.setPageNo(0);
    assertThatThrownBy(() -> ListPagination.of(List.<SignerCsulDto>of(), request, FIELDS, "signerId"))
      .isInstanceOf(BusinessException.class).hasMessageContaining("pageNo dan pageSize");
  }
}
