package com.kmkbe.feign.model;

import com.kmkbe.feign.model.dto.*;
import com.kmkbe.feign.model.request.*;
import com.kmkbe.feign.model.response.ConfinsR3ApiResponseWrapper;
import com.kmkbe.feign.model.response.CsulApiResponseWrapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FeignModelTest {

  @Test
  void testCsulPostLoginDto() {
    CsulPostLoginDto.AuthData authData = new CsulPostLoginDto.AuthData();
    authData.setToken("tok123");
    authData.setTokenType("Bearer");
    authData.setExpiresIn(3600);

    assertThat(authData.getToken()).isEqualTo("tok123");
    assertThat(authData.getTokenType()).isEqualTo("Bearer");
    assertThat(authData.getExpiresIn()).isEqualTo(3600);
    assertThat(authData.toString()).contains("tok123");

    CsulPostLoginDto dto = CsulPostLoginDto.builder()
      .success(true)
      .statusCode(200)
      .data(authData)
      .error(null)
      .timestamp("2026-11-23T00:00:00")
      .build();

    assertThat(dto.isSuccess()).isTrue();
    assertThat(dto.getStatusCode()).isEqualTo(200);
    assertThat(dto.getData()).isEqualTo(authData);
    assertThat(dto.getError()).isNull();
    assertThat(dto.getTimestamp()).isEqualTo("2026-11-23T00:00:00");
    assertThat(dto.toString()).contains("200");
    assertThat(dto.equals(dto)).isTrue();
    assertThat(dto.hashCode()).isNotZero();

    CsulPostLoginDto empty = new CsulPostLoginDto();
    empty.setSuccess(false);
    empty.setStatusCode(400);
    empty.setData(null);
    empty.setError("Err");
    empty.setTimestamp("now");
    assertThat(empty.isSuccess()).isFalse();
  }

  @Test
  void testConfinsR3GetCustomerCompanyInfoDto() {
    ConfinsR3GetCustomerCompanyInfoDto dto = ConfinsR3GetCustomerCompanyInfoDto.builder()
      .custCompanyId(1L)
      .custId(2L)
      .mrCompanyTypeCode("PT")
      .refIndustryTypeId(3L)
      .registrationNo("REG-123")
      .licenseNo("LIC-123")
      .numOfEmp(50)
      .mrInvestmentTypeCode("INV")
      .build();

    assertThat(dto.getCustCompanyId()).isEqualTo(1L);
    assertThat(dto.getCustId()).isEqualTo(2L);
    assertThat(dto.getMrCompanyTypeCode()).isEqualTo("PT");
    assertThat(dto.getRefIndustryTypeId()).isEqualTo(3L);
    assertThat(dto.getRegistrationNo()).isEqualTo("REG-123");
    assertThat(dto.getLicenseNo()).isEqualTo("LIC-123");
    assertThat(dto.getNumOfEmp()).isEqualTo(50);
    assertThat(dto.getMrInvestmentTypeCode()).isEqualTo("INV");
    assertThat(dto.toString()).contains("REG-123");
    assertThat(dto.hashCode()).isNotZero();
    assertThat(dto.equals(dto)).isTrue();

    ConfinsR3GetCustomerCompanyInfoDto empty = new ConfinsR3GetCustomerCompanyInfoDto();
    empty.setCustCompanyId(10L);
    assertThat(empty.getCustCompanyId()).isEqualTo(10L);
  }

  @Test
  void testConfinsR3GetCustomerDto() {
    LocalDateTime now = LocalDateTime.now();
    ConfinsR3GetCustomerDto dto = ConfinsR3GetCustomerDto.builder()
      .custId(100L)
      .custNo("CUST-100")
      .custName("PT Bintang")
      .mrCustTypeCode("C")
      .mrCustModelCode("M")
      .mrIdTypeCode("KTP")
      .idNo("3201000000000001")
      .idExpiredDt(now)
      .taxIdNo("01.000.000.0-000.000")
      .build();

    assertThat(dto.getCustId()).isEqualTo(100L);
    assertThat(dto.getCustNo()).isEqualTo("CUST-100");
    assertThat(dto.getCustName()).isEqualTo("PT Bintang");
    assertThat(dto.getMrCustTypeCode()).isEqualTo("C");
    assertThat(dto.getMrCustModelCode()).isEqualTo("M");
    assertThat(dto.getMrIdTypeCode()).isEqualTo("KTP");
    assertThat(dto.getIdNo()).isEqualTo("3201000000000001");
    assertThat(dto.getIdExpiredDt()).isEqualTo(now);
    assertThat(dto.getTaxIdNo()).isEqualTo("01.000.000.0-000.000");
    assertThat(dto.toString()).contains("PT Bintang");

    ConfinsR3GetCustomerDto empty = new ConfinsR3GetCustomerDto();
    empty.setCustId(200L);
    assertThat(empty.getCustId()).isEqualTo(200L);
  }

  @Test
  void testConfinsR3GetCustomerNoCompanyDto() {
    ConfinsR3GetCustomerDto cust = ConfinsR3GetCustomerDto.builder().custId(1L).build();
    ConfinsR3GetCustomerCompanyInfoDto company = ConfinsR3GetCustomerCompanyInfoDto.builder().custCompanyId(2L).build();

    ConfinsR3GetCustomerNoCompanyDto dto = ConfinsR3GetCustomerNoCompanyDto.builder()
      .customer(cust)
      .customerCampany(company)
      .build();

    assertThat(dto.getCustomer()).isEqualTo(cust);
    assertThat(dto.getCustomerCampany()).isEqualTo(company);
    assertThat(dto.toString()).contains("customer");

    ConfinsR3GetCustomerNoCompanyDto empty = new ConfinsR3GetCustomerNoCompanyDto();
    empty.setCustomer(null);
    empty.setCustomerCampany(null);
    assertThat(empty.getCustomer()).isNull();
  }

  @Test
  void testConfinsR3GetCustomerNoDto() {
    LocalDate now = LocalDate.now();
    ConfinsR3GetCustomerNoDto dto = ConfinsR3GetCustomerNoDto.builder()
      .custId(1L)
      .custNo("CN-01")
      .custName("John Doe")
      .mrCustTypeCode("P")
      .mrCustModelCode("M")
      .mrIdTypeCode("KTP")
      .idNo("123456789")
      .idExpiredDt(now)
      .build();

    assertThat(dto.getCustId()).isEqualTo(1L);
    assertThat(dto.getCustNo()).isEqualTo("CN-01");
    assertThat(dto.getCustName()).isEqualTo("John Doe");
    assertThat(dto.getMrCustTypeCode()).isEqualTo("P");
    assertThat(dto.getMrCustModelCode()).isEqualTo("M");
    assertThat(dto.getMrIdTypeCode()).isEqualTo("KTP");
    assertThat(dto.getIdNo()).isEqualTo("123456789");
    assertThat(dto.getIdExpiredDt()).isEqualTo(now);

    ConfinsR3GetCustomerNoDto empty = new ConfinsR3GetCustomerNoDto();
    empty.setCustId(5L);
    assertThat(empty.getCustId()).isEqualTo(5L);
  }

  @Test
  void testConfinsR3GetCustomerNoPersonalDto() {
    ConfinsR3GetCustomerDto cust = ConfinsR3GetCustomerDto.builder().custId(1L).build();
    ConfinsR3GetCustomerPersonalInfoDto personal = ConfinsR3GetCustomerPersonalInfoDto.builder().custPersonalId(2L).build();

    ConfinsR3GetCustomerNoPersonalDto dto = ConfinsR3GetCustomerNoPersonalDto.builder()
      .customer(cust)
      .customerPersonal(personal)
      .build();

    assertThat(dto.getCustomer()).isEqualTo(cust);
    assertThat(dto.getCustomerPersonal()).isEqualTo(personal);

    ConfinsR3GetCustomerNoPersonalDto empty = new ConfinsR3GetCustomerNoPersonalDto();
    empty.setCustomer(null);
    empty.setCustomerPersonal(null);
    assertThat(empty.getCustomer()).isNull();
  }

  @Test
  void testConfinsR3GetCustomerPersonalInfoDto() {
    ConfinsR3GetCustomerPersonalInfoDto dto = ConfinsR3GetCustomerPersonalInfoDto.builder()
      .custPersonalId(1L)
      .custId(2L)
      .fullName("Jane Doe")
      .prefixName("Ms.")
      .suffixName("S.T.")
      .build();

    assertThat(dto.getCustPersonalId()).isEqualTo(1L);
    assertThat(dto.getCustId()).isEqualTo(2L);
    assertThat(dto.getFullName()).isEqualTo("Jane Doe");
    assertThat(dto.getPrefixName()).isEqualTo("Ms.");
    assertThat(dto.getSuffixName()).isEqualTo("S.T.");

    ConfinsR3GetCustomerPersonalInfoDto empty = new ConfinsR3GetCustomerPersonalInfoDto();
    empty.setCustPersonalId(3L);
    assertThat(empty.getCustPersonalId()).isEqualTo(3L);
  }

  @Test
  void testConfinsR3GetCwrCustomerDto() {
    ConfinsR3GetCwrCustomerDto dto = ConfinsR3GetCwrCustomerDto.builder()
      .rn(1)
      .rowVersion("v1")
      .cwrNo("CWR-101")
      .custName("PT Sejahtera")
      .custNo("CUST-101")
      .groupNo("GRP-1")
      .build();

    assertThat(dto.getRn()).isEqualTo(1);
    assertThat(dto.getRowVersion()).isEqualTo("v1");
    assertThat(dto.getCwrNo()).isEqualTo("CWR-101");
    assertThat(dto.getCustName()).isEqualTo("PT Sejahtera");
    assertThat(dto.getCustNo()).isEqualTo("CUST-101");
    assertThat(dto.getGroupNo()).isEqualTo("GRP-1");

    ConfinsR3GetCwrCustomerDto empty = new ConfinsR3GetCwrCustomerDto();
    empty.setRn(2);
    assertThat(empty.getRn()).isEqualTo(2);
  }

  @Test
  void testConfinsR3GetKeyValueActiveByCodeDto() {
    ConfinsR3GetKeyValueActiveByCodeDto.KeyType keyType = ConfinsR3GetKeyValueActiveByCodeDto.KeyType.builder()
      .key("K1")
      .value("V1")
      .build();

    assertThat(keyType.getKey()).isEqualTo("K1");
    assertThat(keyType.getValue()).isEqualTo("V1");
    assertThat(keyType.toString()).contains("K1");

    ConfinsR3GetKeyValueActiveByCodeDto dto = ConfinsR3GetKeyValueActiveByCodeDto.builder()
      .returnObject(List.of(keyType))
      .build();

    assertThat(dto.getReturnObject()).containsExactly(keyType);

    ConfinsR3GetKeyValueActiveByCodeDto empty = new ConfinsR3GetKeyValueActiveByCodeDto();
    empty.setReturnObject(null);
    assertThat(empty.getReturnObject()).isNull();
  }

  @Test
  void testConfinsR3GetZipCodeDto() {
    ConfinsR3GetZipCodeDto dto = ConfinsR3GetZipCodeDto.builder()
      .refZipcodeId(10L)
      .areaCode1("KEC01")
      .areaCode2("KEL01")
      .city("Jakarta")
      .zipcode("12345")
      .refProvDistrictId(1)
      .build();

    assertThat(dto.getRefZipcodeId()).isEqualTo(10L);
    assertThat(dto.getAreaCode1()).isEqualTo("KEC01");
    assertThat(dto.getAreaCode2()).isEqualTo("KEL01");
    assertThat(dto.getCity()).isEqualTo("Jakarta");
    assertThat(dto.getZipcode()).isEqualTo("12345");
    assertThat(dto.getRefProvDistrictId()).isEqualTo(1);

    ConfinsR3GetZipCodeDto empty = new ConfinsR3GetZipCodeDto();
    empty.setZipcode("54321");
    assertThat(empty.getZipcode()).isEqualTo("54321");
  }

  @Test
  void testCsulGetVendorDto() {
    CsulGetVendorDto.BuildingDetail building = new CsulGetVendorDto.BuildingDetail();
    building.setOwnershipStatus(1);
    building.setJenis(2);
    building.setCategory(3);
    building.setDocumentNo(4);
    building.setBuildingAddress(5);
    building.setAddressDetail("Jl. Sudirman No. 1");
    building.setAddressInfo("Lantai 2");
    building.setStateName("DKI Jakarta");
    building.setCityName("Jakarta Selatan");
    building.setDistrictName("Kebayoran Baru");

    assertThat(building.getOwnershipStatus()).isEqualTo(1);
    assertThat(building.getJenis()).isEqualTo(2);
    assertThat(building.getCategory()).isEqualTo(3);
    assertThat(building.getDocumentNo()).isEqualTo(4);
    assertThat(building.getBuildingAddress()).isEqualTo(5);
    assertThat(building.getAddressDetail()).isEqualTo("Jl. Sudirman No. 1");
    assertThat(building.getAddressInfo()).isEqualTo("Lantai 2");
    assertThat(building.getStateName()).isEqualTo("DKI Jakarta");
    assertThat(building.getCityName()).isEqualTo("Jakarta Selatan");
    assertThat(building.getDistrictName()).isEqualTo("Kebayoran Baru");

    CsulGetVendorDto dto = CsulGetVendorDto.builder()
      .vendorId("V-1")
      .sapCode("SAP-1")
      .vendorName("PT Vendor CKB")
      .foundedDate("2020-01-01")
      .npwp("01.234.567.8-999.000")
      .npwpUrl("https://docs/npwp.pdf")
      .nipSiup("SIUP-1")
      .nipSiupLink("https://docs/siup.pdf")
      .pkpNumber("PKP-1")
      .pkpLink("https://docs/pkp.pdf")
      .jenisPerusahaan("Perseroan Terbatas")
      .ktpNpwpVendorStockId("STK-1")
      .ktpNpwpVendorStockLink("https://docs/stk.pdf")
      .laporanKeuanganLink("https://docs/lap.pdf")
      .aktaPendirianLink("https://docs/akta.pdf")
      .aktaPerubahanLink("https://docs/ubah.pdf")
      .pengesahanKemenkumhamLink("https://docs/kumham.pdf")
      .email("vendor@example.com")
      .phone("021-1234567")
      .website("https://vendor.com")
      .fax("021-7654321")
      .ktpDirectur("3171000000000001")
      .ktpDirekturLink("https://docs/ktp.pdf")
      .positionRef(1)
      .vendorBuilding(building)
      .bankDetail(Collections.emptyList())
      .vendorRegistrationDoc(Collections.emptyList())
      .otherDocument(Collections.emptyList())
      .build();

    assertThat(dto.getVendorId()).isEqualTo("V-1");
    assertThat(dto.getSapCode()).isEqualTo("SAP-1");
    assertThat(dto.getVendorName()).isEqualTo("PT Vendor CKB");
    assertThat(dto.getFoundedDate()).isEqualTo("2020-01-01");
    assertThat(dto.getNpwp()).isEqualTo("01.234.567.8-999.000");
    assertThat(dto.getNpwpUrl()).isEqualTo("https://docs/npwp.pdf");
    assertThat(dto.getNipSiup()).isEqualTo("SIUP-1");
    assertThat(dto.getNipSiupLink()).isEqualTo("https://docs/siup.pdf");
    assertThat(dto.getPkpNumber()).isEqualTo("PKP-1");
    assertThat(dto.getPkpLink()).isEqualTo("https://docs/pkp.pdf");
    assertThat(dto.getJenisPerusahaan()).isEqualTo("Perseroan Terbatas");
    assertThat(dto.getKtpNpwpVendorStockId()).isEqualTo("STK-1");
    assertThat(dto.getKtpNpwpVendorStockLink()).isEqualTo("https://docs/stk.pdf");
    assertThat(dto.getLaporanKeuanganLink()).isEqualTo("https://docs/lap.pdf");
    assertThat(dto.getAktaPendirianLink()).isEqualTo("https://docs/akta.pdf");
    assertThat(dto.getAktaPerubahanLink()).isEqualTo("https://docs/ubah.pdf");
    assertThat(dto.getPengesahanKemenkumhamLink()).isEqualTo("https://docs/kumham.pdf");
    assertThat(dto.getEmail()).isEqualTo("vendor@example.com");
    assertThat(dto.getPhone()).isEqualTo("021-1234567");
    assertThat(dto.getWebsite()).isEqualTo("https://vendor.com");
    assertThat(dto.getFax()).isEqualTo("021-7654321");
    assertThat(dto.getKtpDirectur()).isEqualTo("3171000000000001");
    assertThat(dto.getKtpDirekturLink()).isEqualTo("https://docs/ktp.pdf");
    assertThat(dto.getPositionRef()).isEqualTo(1);
    assertThat(dto.getVendorBuilding()).isEqualTo(building);
    assertThat(dto.getBankDetail()).isEmpty();
    assertThat(dto.getVendorRegistrationDoc()).isEmpty();
    assertThat(dto.getOtherDocument()).isEmpty();

    CsulGetVendorDto empty = new CsulGetVendorDto();
    empty.setVendorId("V-2");
    assertThat(empty.getVendorId()).isEqualTo("V-2");
  }

  @Test
  void testCsulInquiryInvoiceRemoteDto() {
    CsulInquiryInvoiceRemoteDto.InvoiceRemoteDto inv = CsulInquiryInvoiceRemoteDto.InvoiceRemoteDto.builder()
      .vendorNo("V-100")
      .reference("REF-100")
      .accountingDocument("DOC-100")
      .poNumber("PO-100")
      .amount("1000000")
      .currency("IDR")
      .netDueDate("2026-12-01")
      .postingDate("2026-11-01")
      .description("Pembayaran material")
      .build();

    assertThat(inv.getVendorNo()).isEqualTo("V-100");
    assertThat(inv.getReference()).isEqualTo("REF-100");
    assertThat(inv.getAccountingDocument()).isEqualTo("DOC-100");
    assertThat(inv.getPoNumber()).isEqualTo("PO-100");
    assertThat(inv.getAmount()).isEqualTo("1000000");
    assertThat(inv.getCurrency()).isEqualTo("IDR");
    assertThat(inv.getNetDueDate()).isEqualTo("2026-12-01");
    assertThat(inv.getPostingDate()).isEqualTo("2026-11-01");
    assertThat(inv.getDescription()).isEqualTo("Pembayaran material");

    CsulInquiryInvoiceRemoteDto dto = CsulInquiryInvoiceRemoteDto.builder()
      .blacklistStatus(false)
      .identityNumber("ID-1")
      .identityType("KTP")
      .documentStatus("01")
      .dataVendorEproc(true)
      .count(1)
      .row(List.of(inv))
      .build();

    assertThat(dto.getBlacklistStatus()).isFalse();
    assertThat(dto.getIdentityNumber()).isEqualTo("ID-1");
    assertThat(dto.getIdentityType()).isEqualTo("KTP");
    assertThat(dto.getDocumentStatus()).isEqualTo("01");
    assertThat(dto.getDataVendorEproc()).isTrue();
    assertThat(dto.getCount()).isEqualTo(1);
    assertThat(dto.getRow()).containsExactly(inv);

    CsulInquiryInvoiceRemoteDto empty = new CsulInquiryInvoiceRemoteDto();
    empty.setCount(0);
    assertThat(empty.getCount()).isEqualTo(0);

    CsulInquiryInvoiceRemoteDto.InvoiceRemoteDto emptyInv = new CsulInquiryInvoiceRemoteDto.InvoiceRemoteDto();
    emptyInv.setAmount("500");
    assertThat(emptyInv.getAmount()).isEqualTo("500");
  }

  @Test
  void testRequests() {
    ConfinsR3GetCustomerNoRequest getCustNoReq = ConfinsR3GetCustomerNoRequest.builder()
      .requestDateTime("2026-11-23")
      .rowVersion("1")
      .custNo("CUST01")
      .build();
    assertThat(getCustNoReq.getCustNo()).isEqualTo("CUST01");
    assertThat(getCustNoReq.getRequestDateTime()).isEqualTo("2026-11-23");
    assertThat(getCustNoReq.getRowVersion()).isEqualTo("1");

    ConfinsR3GetKeyValueActiveByCodeRequest keyValReq = ConfinsR3GetKeyValueActiveByCodeRequest.builder()
      .refMasterTypeCode("TYPE_A")
      .mappingCode("MAP_A")
      .requestDateTime("2026-11-23")
      .build();
    assertThat(keyValReq.getRefMasterTypeCode()).isEqualTo("TYPE_A");
    assertThat(keyValReq.getMappingCode()).isEqualTo("MAP_A");
    assertThat(keyValReq.getRequestDateTime()).isEqualTo("2026-11-23");

    ConfinsR3GetPagingObjectBySQLRequest.QueryStringQueryDto queryDto = ConfinsR3GetPagingObjectBySQLRequest.QueryStringQueryDto.builder()
      .name("testQuery")
      .whereQuery(List.of("A=1"))
      .build();
    assertThat(queryDto.getName()).isEqualTo("testQuery");
    assertThat(queryDto.getWhereQuery()).containsExactly("A=1");

    ConfinsR3GetPagingObjectBySQLRequest.CriterionDto criterion = ConfinsR3GetPagingObjectBySQLRequest.CriterionDto.builder()
      .low(0)
      .high(10)
      .dataType("String")
      .isCriteriaDataTable(false)
      .propName("col")
      .value("val")
      .restriction("eq")
      .build();
    assertThat(criterion.getLow()).isEqualTo(0);
    assertThat(criterion.getHigh()).isEqualTo(10);
    assertThat(criterion.getDataType()).isEqualTo("String");
    assertThat(criterion.getIsCriteriaDataTable()).isFalse();
    assertThat(criterion.getPropName()).isEqualTo("col");
    assertThat(criterion.getValue()).isEqualTo("val");
    assertThat(criterion.getRestriction()).isEqualTo("eq");

    ConfinsR3GetPagingObjectBySQLRequest pagingReq = ConfinsR3GetPagingObjectBySQLRequest.builder()
      .includeCount(true)
      .includeData(true)
      .isLoading(false)
      .queryString(queryDto)
      .rowVersion("1")
      .integrationObj(null)
      .joinType("INNER")
      .pageNo(1)
      .rowPerPage(10)
      .orderBy("id ASC")
      .criteria(List.of(criterion))
      .requestDateTime("2026-11-23")
      .build();
    assertThat(pagingReq.getIncludeCount()).isTrue();
    assertThat(pagingReq.getIncludeData()).isTrue();
    assertThat(pagingReq.getIsLoading()).isFalse();
    assertThat(pagingReq.getQueryString()).isEqualTo(queryDto);
    assertThat(pagingReq.getRowVersion()).isEqualTo("1");
    assertThat(pagingReq.getJoinType()).isEqualTo("INNER");
    assertThat(pagingReq.getPageNo()).isEqualTo(1);
    assertThat(pagingReq.getRowPerPage()).isEqualTo(10);
    assertThat(pagingReq.getOrderBy()).isEqualTo("id ASC");
    assertThat(pagingReq.getCriteria()).containsExactly(criterion);
    assertThat(pagingReq.getRequestDateTime()).isEqualTo("2026-11-23");

    ConfinsR3KeyAndValueObjRequest.KeyAndValueObjDTO keyValDto = ConfinsR3KeyAndValueObjRequest.KeyAndValueObjDTO.builder()
      .key("IdNo")
      .operator("=")
      .value("123456")
      .build();
    assertThat(keyValDto.getKey()).isEqualTo("IdNo");
    assertThat(keyValDto.getOperator()).isEqualTo("=");
    assertThat(keyValDto.getValue()).isEqualTo("123456");

    ConfinsR3KeyAndValueObjRequest keyValObjReq = ConfinsR3KeyAndValueObjRequest.builder()
      .keyAndValueObj(keyValDto)
      .includeProperties(List.of("Prop1"))
      .requestDateTime("2026-11-23")
      .build();
    assertThat(keyValObjReq.getKeyAndValueObj()).isEqualTo(keyValDto);
    assertThat(keyValObjReq.getIncludeProperties()).containsExactly("Prop1");
    assertThat(keyValObjReq.getRequestDateTime()).isEqualTo("2026-11-23");

    CsulGetZipCodeRequest zipReq = CsulGetZipCodeRequest.builder()
      .zipcode("12345")
      .build();
    assertThat(zipReq.getZipcode()).isEqualTo("12345");

    CsulPostLoginRequest loginReq = new CsulPostLoginRequest("user", "pass", "sec");
    assertThat(loginReq.getUsername()).isEqualTo("user");
    assertThat(loginReq.getPassword()).isEqualTo("pass");
    assertThat(loginReq.getClientSecret()).isEqualTo("sec");
    loginReq.setUsername("u2");
    loginReq.setPassword("p2");
    loginReq.setClientSecret("s2");
    assertThat(loginReq.getUsername()).isEqualTo("u2");
    assertThat(loginReq.getPassword()).isEqualTo("p2");
    assertThat(loginReq.getClientSecret()).isEqualTo("s2");
  }

  @Test
  void testResponses() {
    ConfinsR3ApiResponseWrapper<String> confinsWrapper = ConfinsR3ApiResponseWrapper.<String>builder()
      .code("200")
      .message("Success")
      .data(List.of("Item1", "Item2"))
      .count(2)
      .build();
    assertThat(confinsWrapper.getCode()).isEqualTo("200");
    assertThat(confinsWrapper.getMessage()).isEqualTo("Success");
    assertThat(confinsWrapper.getData()).containsExactly("Item1", "Item2");
    assertThat(confinsWrapper.getCount()).isEqualTo(2);

    CsulApiResponseWrapper<String> csulWrapper = new CsulApiResponseWrapper<>();
    csulWrapper.setStatus("OK");
    csulWrapper.setStatusCode(200);
    csulWrapper.setData("Hello");
    assertThat(csulWrapper.getStatus()).isEqualTo("OK");
    assertThat(csulWrapper.getStatusCode()).isEqualTo(200);
    assertThat(csulWrapper.getData()).isEqualTo("Hello");
    assertThat(csulWrapper.toString()).contains("OK");
  }
}
