package com.kmkbe.modules.confinsr3.service;

import com.kmkbe.adapter.ApiConfinsR3Adapter;
import com.kmkbe.core.domain.dto.CustomerRemoteDto;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.feign.model.dto.*;
import com.kmkbe.feign.model.request.ConfinsR3GetCustomerNoRequest;
import com.kmkbe.feign.model.request.ConfinsR3GetKeyValueActiveByCodeRequest;
import com.kmkbe.feign.model.request.ConfinsR3GetPagingObjectBySQLRequest;
import com.kmkbe.feign.model.request.ConfinsR3KeyAndValueObjRequest;
import com.kmkbe.feign.model.response.ConfinsR3ApiResponseWrapper;
import com.kmkbe.modules.confinsr3.model.request.ConfinsR3ZipcodeCriteriaRequest;
import com.kmkbe.modules.master.request.AreaPageRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfinsR3ServiceCoverageTest {
  @Mock ApiConfinsR3Adapter adapter;
  @InjectMocks ConfinsR3Service service;

  @Test
  void findZipcodeSuccessAndMissing() {
    when(adapter.getZipcode("12345")).thenReturn(ConfinsR3GetZipCodeDto.builder()
      .zipcode("12345").city("Jakarta").build());

    var result = service.findZipcode(" 12345 ");

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getData().getZipcode()).isEqualTo("12345");
    assertThat(result.getData().getCity()).isEqualTo("Jakarta");

    when(adapter.getZipcode("99999")).thenReturn(null);
    assertThatThrownBy(() -> service.findZipcode("99999")).isInstanceOf(BusinessException.class);
  }

  @Test
  void allZipcodeSuccessAndFailure() {
    when(adapter.getAllZipcode(any())).thenReturn(wrapper("200", List.of(
      ConfinsR3GetZipCodeDto.builder().zipcode("12345").city("Jakarta").build()), 0));

    var result = service.allZipcode(1, 10);

    assertThat(result.getData()).singleElement().extracting("zipcode").isEqualTo("12345");

    when(adapter.getAllZipcode(any())).thenReturn(wrapper("500", List.of(), 0));
    assertThatThrownBy(() -> service.allZipcode(1, 10)).isInstanceOf(BusinessException.class);
  }

  @Test
  void pageZipcodeSendsValidSortAndCriteriaAndHandlesFailure() {
    AreaPageRequest request = new AreaPageRequest();
    request.setPageNo(1);
    request.setPageSize(10);
    request.setSortBy("RZ.ZIPCODE");
    request.setSortType("ASC");
    request.setCriteria(List.of(ConfinsR3ZipcodeCriteriaRequest.builder()
      .propName("city").value("jakarta").build()));
    request.setSearchBy("zipcode");
    request.setSearchValue("12345");
    when(adapter.getAllZipcode(any())).thenReturn(wrapper("200", List.of(), 25));

    var result = service.pageZipcode(request);

    assertThat(result.getData().getTotalPage()).isEqualTo(3);
    assertThat(result.getData().getTotalData()).isEqualTo(25);
    assertThat(result.getData().getList()).isEmpty();

    when(adapter.getAllZipcode(any())).thenReturn(wrapper("500", List.of(), 0));
    assertThatThrownBy(() -> service.pageZipcode(request)).isInstanceOf(BusinessException.class);
  }

  @Test
  void pageZipcodeRejectsInvalidPaginationSortAndCriteria() {
    AreaPageRequest missingPage = new AreaPageRequest();
    assertThatThrownBy(() -> service.pageZipcode(missingPage)).isInstanceOf(BusinessException.class)
      .hasMessageContaining("pageNo dan pageSize wajib");

    AreaPageRequest mismatchedSort = pageRequest();
    mismatchedSort.setSortBy("city");
    assertThatThrownBy(() -> service.pageZipcode(mismatchedSort)).isInstanceOf(BusinessException.class)
      .hasMessageContaining("diisi bersama");

    AreaPageRequest invalidDirection = pageRequest();
    invalidDirection.setSortBy("city");
    invalidDirection.setSortType("sideways");
    assertThatThrownBy(() -> service.pageZipcode(invalidDirection)).isInstanceOf(BusinessException.class)
      .hasMessageContaining("ASC atau DESC");

    AreaPageRequest blankCriteria = pageRequest();
    blankCriteria.setCriteria(List.of(new ConfinsR3ZipcodeCriteriaRequest()));
    assertThatThrownBy(() -> service.pageZipcode(blankCriteria)).isInstanceOf(BusinessException.class)
      .hasMessageContaining("criteria harus memiliki propName dan value");

    AreaPageRequest nullCriteria = pageRequest();
    nullCriteria.setCriteria(java.util.Collections.singletonList(null));
    assertThatThrownBy(() -> service.pageZipcode(nullCriteria)).isInstanceOf(BusinessException.class);

    verify(adapter, never()).getAllZipcode(any());
  }

  @Test
  void pageZipcodeCoversRemainingValidationBranches() {
    AreaPageRequest missingPageSize = new AreaPageRequest();
    missingPageSize.setPageNo(1);
    assertThatThrownBy(() -> service.pageZipcode(missingPageSize)).isInstanceOf(BusinessException.class)
      .hasMessageContaining("pageNo dan pageSize wajib");

    AreaPageRequest blankSort = pageRequest();
    blankSort.setSortBy("  ");
    blankSort.setSortType(" ");
    when(adapter.getAllZipcode(any())).thenReturn(wrapper("200", List.of(), 0));
    assertThat(service.pageZipcode(blankSort).getData().getList()).isEmpty();

    AreaPageRequest blankCriteriaValue = pageRequest();
    blankCriteriaValue.setCriteria(List.of(ConfinsR3ZipcodeCriteriaRequest.builder()
      .propName("city").value(" ").build()));
    assertThatThrownBy(() -> service.pageZipcode(blankCriteriaValue)).isInstanceOf(BusinessException.class)
      .hasMessageContaining("criteria harus memiliki propName dan value");

    AreaPageRequest nullCriteriaField = pageRequest();
    nullCriteriaField.setCriteria(List.of(ConfinsR3ZipcodeCriteriaRequest.builder()
      .value("Jakarta").build()));
    assertThatThrownBy(() -> service.pageZipcode(nullCriteriaField)).isInstanceOf(BusinessException.class)
      .hasMessageContaining("Field area tidak valid");
  }

  @Test
  void findCwrByCustomerSuccessAndFailure() {
    when(adapter.getCwrByCustomer(any())).thenReturn(wrapper("200", List.of(
      ConfinsR3GetCwrCustomerDto.builder().cwrNo("CWR-1").custName("Customer").build()), 0));

    var result = service.findCwrByCustomer(1, 10, "CUST");

    assertThat(result.getData()).singleElement().extracting("cwrNo").isEqualTo("CWR-1");

    when(adapter.getCwrByCustomer(any())).thenReturn(wrapper("500", List.of(), 0));
    assertThatThrownBy(() -> service.findCwrByCustomer(1, 10, "CUST")).isInstanceOf(BusinessException.class);
  }

  @Test
  void findByCustomerSuccessAndFailure() {
    when(adapter.getByCustomer(any())).thenReturn(wrapper("200", List.of(
      ConfinsR3GetCustomerDto.builder().custName("Customer").build()), 0));

    var result = service.findByCustomer(1, 10, "Customer");

    assertThat(result.getData()).singleElement().extracting("custName").isEqualTo("Customer");

    when(adapter.getByCustomer(any())).thenReturn(wrapper("500", List.of(), 0));
    assertThatThrownBy(() -> service.findByCustomer(1, 10, "Customer")).isInstanceOf(BusinessException.class);
  }

  @Test
  void findByCustomerNoSuccessAndMissing() {
    when(adapter.getByCustomerNo(any(ConfinsR3GetCustomerNoRequest.class)))
      .thenReturn(ConfinsR3GetCustomerNoDto.builder().custNo("CUST-1").custName("Customer").build());

    var result = service.findByCustomerNo("CUST-1");

    assertThat(result.getData().getCustNo()).isEqualTo("CUST-1");
    assertThat(result.getData().getCustName()).isEqualTo("Customer");

    when(adapter.getByCustomerNo(any(ConfinsR3GetCustomerNoRequest.class))).thenReturn(null);
    assertThatThrownBy(() -> service.findByCustomerNo("CUST-2")).isInstanceOf(BusinessException.class);
  }

  @Test
  void findKeyValueByCodeSuccessAndMissing() {
    when(adapter.getKyValueByCode(any(ConfinsR3GetKeyValueActiveByCodeRequest.class)))
      .thenReturn(ConfinsR3GetKeyValueActiveByCodeDto.builder()
        .returnObject(List.of(ConfinsR3GetKeyValueActiveByCodeDto.KeyType.builder()
          .key("KEY").value("VALUE").build()))
        .build());

    var result = service.findKeyValueByCode("TYPE");

    assertThat(result.getData()).singleElement().satisfies(item -> {
      assertThat(item.getKey()).isEqualTo("KEY");
      assertThat(item.getValue()).isEqualTo("VALUE");
    });

    when(adapter.getKyValueByCode(any(ConfinsR3GetKeyValueActiveByCodeRequest.class))).thenReturn(null);
    assertThatThrownBy(() -> service.findKeyValueByCode("TYPE")).isInstanceOf(BusinessException.class);
  }

  @Test
  void findByCustomerNoCompanySuccessAndMissing() {
    when(adapter.getByCustomerNoCompany(any(ConfinsR3GetCustomerNoRequest.class)))
      .thenReturn(ConfinsR3GetCustomerNoCompanyDto.builder()
        .customer(ConfinsR3GetCustomerDto.builder().custName("Customer").build())
        .customerCampany(ConfinsR3GetCustomerCompanyInfoDto.builder().registrationNo("REG-1").build())
        .build());

    var result = service.findByCustomerNoCompany("CUST-1");

    assertThat(result.getData().getCustomer().getCustName()).isEqualTo("Customer");
    assertThat(result.getData().getCustomerCampany().getRegistrationNo()).isEqualTo("REG-1");

    when(adapter.getByCustomerNoCompany(any(ConfinsR3GetCustomerNoRequest.class))).thenReturn(null);
    assertThatThrownBy(() -> service.findByCustomerNoCompany("CUST-2")).isInstanceOf(BusinessException.class);
  }

  @Test
  void findByCustomerNoPersonalSuccessAndMissing() {
    when(adapter.getByCustomerNoPersonal(any(ConfinsR3GetCustomerNoRequest.class)))
      .thenReturn(ConfinsR3GetCustomerNoPersonalDto.builder()
        .customer(ConfinsR3GetCustomerDto.builder().custName("Customer").build())
        .customerPersonal(ConfinsR3GetCustomerPersonalInfoDto.builder().fullName("Full Name").build())
        .build());

    var result = service.findByCustomerNoPersonal("CUST-1");

    assertThat(result.getData().getCustomer().getCustName()).isEqualTo("Customer");
    assertThat(result.getData().getCustomerPersonal().getFullName()).isEqualTo("Full Name");

    when(adapter.getByCustomerNoPersonal(any(ConfinsR3GetCustomerNoRequest.class))).thenReturn(null);
    assertThatThrownBy(() -> service.findByCustomerNoPersonal("CUST-2")).isInstanceOf(BusinessException.class);
  }

  @Test
  void findCustomerByKeyAndValueSuccessAndMissing() {
    when(adapter.getCustomerByKeyAndValue(any(ConfinsR3KeyAndValueObjRequest.class)))
      .thenReturn(CustomerRemoteDto.builder().build());

    var result = service.findCustomerByKeyAndValue("custNo", "CUST-1");

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getData()).isNotNull();

    when(adapter.getCustomerByKeyAndValue(any(ConfinsR3KeyAndValueObjRequest.class))).thenReturn(null);
    assertThatThrownBy(() -> service.findCustomerByKeyAndValue("custNo", "CUST-2"))
      .isInstanceOf(BusinessException.class);
  }

  private static AreaPageRequest pageRequest() {
    AreaPageRequest request = new AreaPageRequest();
    request.setPageNo(1);
    request.setPageSize(10);
    return request;
  }

  private static <T> ConfinsR3ApiResponseWrapper<T> wrapper(String code, List<T> data, int count) {
    return ConfinsR3ApiResponseWrapper.<T>builder()
      .code(code)
      .message("message")
      .data(data)
      .count(count)
      .build();
  }
}
