package com.kmkbe.modules.confinsr3.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.adapter.ApiConfinsR3Adapter;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.feign.model.dto.ConfinsR3GetZipCodeDto;
import com.kmkbe.feign.model.request.ConfinsR3GetPagingObjectBySQLRequest;
import com.kmkbe.feign.model.response.ConfinsR3ApiResponseWrapper;
import com.kmkbe.modules.confinsr3.model.request.ConfinsR3ZipcodeCriteriaRequest;
import com.kmkbe.modules.master.request.AreaPageRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfinsR3ServiceAreaPageTest {
  @Mock private ApiConfinsR3Adapter adapter;
  @InjectMocks private ConfinsR3Service service;

  @Test
  void legacyAreaCriteriaRemainOptionalWithoutSortAndSearch() {
    AreaPageRequest request = pageRequest();
    request.setCriteria(List.of(ConfinsR3ZipcodeCriteriaRequest.builder()
      .propName("RZ.AREA_CODE_1")
      .value("Jakarta")
      .build()));
    when(adapter.getAllZipcode(any())).thenReturn(emptyResponse());

    service.pageZipcode(request);

    ArgumentCaptor<ConfinsR3GetPagingObjectBySQLRequest> captor = pagingRequest();
    verify(adapter).getAllZipcode(captor.capture());
    assertThat(captor.getValue().getPageNo()).isEqualTo(1);
    assertThat(captor.getValue().getRowPerPage()).isEqualTo(10);
    assertThat(captor.getValue().getOrderBy()).isNull();
    assertThat(captor.getValue().getCriteria()).hasSize(1);
    assertThat(captor.getValue().getCriteria().getFirst().getPropName()).isEqualTo("RZ.AREA_CODE_1");
    assertThat(captor.getValue().getCriteria().getFirst().getValue()).isEqualTo("%JAKARTA%");
  }

  @Test
  void basePaginationSearchAndSortAreSentToConfins() throws Exception {
    AreaPageRequest request = pageRequest();
    request.setSearchBy("kelurahan");
    request.setSearchValue("Gambir");
    request.setSortBy("city");
    request.setSortType("desc");
    when(adapter.getAllZipcode(any())).thenReturn(emptyResponse());

    service.pageZipcode(request);

    ArgumentCaptor<ConfinsR3GetPagingObjectBySQLRequest> captor = pagingRequest();
    verify(adapter).getAllZipcode(captor.capture());
    assertThat(captor.getValue().getOrderBy()).isEqualTo(Map.of("key", "RZ.CITY", "value", "false"));
    assertThat(captor.getValue().getCriteria()).hasSize(1);
    assertThat(captor.getValue().getCriteria().getFirst().getPropName()).isEqualTo("RZ.AREA_CODE_2");
    assertThat(captor.getValue().getCriteria().getFirst().getValue()).isEqualTo("%GAMBIR%");
    assertThat(new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(captor.getValue()))
      .path("orderBy").path("key").asText()).isEqualTo("RZ.CITY");
  }

  @Test
  void provinceCanBeUsedForCriteriaSearchAndSort() {
    AreaPageRequest request = pageRequest();
    request.setCriteria(List.of(ConfinsR3ZipcodeCriteriaRequest.builder()
      .propName("province")
      .value("Jawa Barat")
      .build()));
    request.setSearchBy("province");
    request.setSearchValue("Jawa");
    request.setSortBy("province");
    request.setSortType("asc");
    when(adapter.getAllZipcode(any())).thenReturn(emptyResponse());

    service.pageZipcode(request);

    ArgumentCaptor<ConfinsR3GetPagingObjectBySQLRequest> captor = pagingRequest();
    verify(adapter).getAllZipcode(captor.capture());
    assertThat(captor.getValue().getOrderBy()).isEqualTo(Map.of("key", "RZ.PROVINCE", "value", "true"));
    assertThat(captor.getValue().getCriteria()).extracting("propName")
      .containsExactly("RZ.PROVINCE", "RZ.PROVINCE");
    assertThat(captor.getValue().getCriteria()).extracting("value")
      .containsExactly("%JAWA BARAT%", "%JAWA%");
  }

  @Test
  void invalidSearchSortAndPagingAreRejectedBeforeCallingConfins() {
    AreaPageRequest missingSearchBy = pageRequest();
    missingSearchBy.setSearchValue("Gambir");
    assertThatThrownBy(() -> service.pageZipcode(missingSearchBy)).isInstanceOf(BusinessException.class);

    AreaPageRequest invalidSort = pageRequest();
    invalidSort.setSortBy("unknown");
    invalidSort.setSortType("asc");
    assertThatThrownBy(() -> service.pageZipcode(invalidSort)).isInstanceOf(BusinessException.class);

    AreaPageRequest missingPage = new AreaPageRequest();
    assertThatThrownBy(() -> service.pageZipcode(missingPage)).isInstanceOf(BusinessException.class);
    verify(adapter, never()).getAllZipcode(any());
  }

  private static AreaPageRequest pageRequest() {
    AreaPageRequest request = new AreaPageRequest();
    request.setPageNo(1);
    request.setPageSize(10);
    return request;
  }

  private static ConfinsR3ApiResponseWrapper<ConfinsR3GetZipCodeDto> emptyResponse() {
    return ConfinsR3ApiResponseWrapper.<ConfinsR3GetZipCodeDto>builder()
      .code("200")
      .data(List.of())
      .build();
  }

  private static ArgumentCaptor<ConfinsR3GetPagingObjectBySQLRequest> pagingRequest() {
    return ArgumentCaptor.forClass(ConfinsR3GetPagingObjectBySQLRequest.class);
  }
}
