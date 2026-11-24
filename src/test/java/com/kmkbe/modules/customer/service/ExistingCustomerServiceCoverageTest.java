package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.dto.BaseMstRemoteResponseDto;
import com.kmkbe.core.domain.dto.CustomerRemoteDto;
import com.kmkbe.core.domain.dto.ExistingCustomerDto;
import com.kmkbe.core.domain.dto.InquiryAgreementByNoCwrRemoteDto;
import com.kmkbe.core.domain.dto.InquiryCwrRemoteDto;
import com.kmkbe.core.domain.repository.CwrRepository;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.remote.request.ExistingCustomerRequest;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import com.kmkbe.modules.remote.service.CwrRemoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExistingCustomerServiceCoverageTest {
  @Mock JdbcTemplate jdbcTemplate;
  @Mock CustomerRemoteService customerRemoteService;
  @Mock CwrRemoteService cwrRemoteService;
  @Mock CwrRepository cwrRepository;
  @Mock CustomerRepository customerRepository;
  ExistingCustomerService service;

  @BeforeEach
  void setUp() {
    service = new ExistingCustomerService(jdbcTemplate, customerRemoteService, cwrRemoteService,
      cwrRepository, customerRepository);
  }

  @Test
  void createOrUpdateInsertAndUpdate() {
    when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("V001")))
      .thenThrow(new EmptyResultDataAccessException(1))
      .thenReturn(existingDto());
    when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any())).thenReturn(1);

    assertThat(service.createOrUpdate("V001", true, "KTP", "123").getVendorCode()).isEqualTo("V001");

    when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("V001")))
      .thenReturn(existingDto(), existingDto());
    when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any())).thenReturn(1);
    assertThat(service.createOrUpdate("V001", false, "NPWP", "456").getVendorCode()).isEqualTo("V001");

    when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("broken")))
      .thenThrow(new RuntimeException("db down"));
    assertThatThrownBy(() -> service.createOrUpdate("broken", true, "KTP", "123"))
      .hasMessage("db down");
  }

  @Test
  void findLastByVendorCodeExecutesRowMapper() {
    stubQuery("V001", existingDto());
    assertThat(service.findLastByVendorCode("V001")).isPresent();

    when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq("missing")))
      .thenThrow(new EmptyResultDataAccessException(1));
    assertThat(service.findLastByVendorCode("missing")).isEmpty();
  }

  @Test
  void inquiryReturnsNullForMissingInputsAndResponses() throws Exception {
    assertThat(service.inquiryAndDecideExistingCustomer(null, "123", customer(), bouwheer())).isNull();
    assertThat(service.inquiryAndDecideExistingCustomer(ExistingCustomerRequest.KeyType.values()[0], null,
      customer(), bouwheer())).isNull();

    when(customerRemoteService.validateExisting(any())).thenReturn(null);
    assertThat(service.inquiryAndDecideExistingCustomer(ExistingCustomerRequest.KeyType.values()[0], "123",
      customer(), bouwheer())).isNull();

    CustomerRemoteDto remote = CustomerRemoteDto.builder().custNo("CUST-1").build();
    when(customerRemoteService.validateExisting(any())).thenReturn(remote);
    when(customerRepository.findByCustCode(any())).thenReturn(Optional.empty());
    when(cwrRemoteService.inquiryCwr(any())).thenReturn(null);
    assertThat(service.inquiryAndDecideExistingCustomer(ExistingCustomerRequest.KeyType.values()[0], "123",
      customer(), bouwheer())).isNull();
  }

  @Test
  void inquiryCreatesCwrForActiveAgreementlessCustomer() throws Exception {
    Customer customer = customer();
    CustomerRemoteDto remote = CustomerRemoteDto.builder().custNo("CUST-1").build();
    when(customerRemoteService.validateExisting(any())).thenReturn(remote);
    when(customerRepository.findByCustCode(customer.getCustCode())).thenReturn(Optional.of(customer));
    when(customerRepository.save(customer)).thenReturn(customer);

    BaseMstRemoteResponseDto<List<InquiryCwrRemoteDto>> cwrResponse = new BaseMstRemoteResponseDto<>();
    cwrResponse.setData(new ArrayList<>(List.of(cwr(1, "active", "2026-11-01T00:00:00", "2026-12-01T00:00:00"))));
    when(cwrRemoteService.inquiryCwr(any())).thenReturn(cwrResponse);

    BaseMstRemoteResponseDto<List<InquiryAgreementByNoCwrRemoteDto>> agreementResponse =
      new BaseMstRemoteResponseDto<>();
    agreementResponse.setData(List.of());
    when(cwrRemoteService.inquiryAgreementByNoCwr("CWR-1")).thenReturn(agreementResponse);
    when(cwrRepository.findTopByCwrCode("CWR-1")).thenReturn(Optional.empty());

    var result = service.inquiryAndDecideExistingCustomer(ExistingCustomerRequest.KeyType.values()[0],
      "123", customer, bouwheer());

    assertThat(result.getCwrCode()).isEqualTo("CWR-1");
    verify(cwrRepository).save(any());

    when(cwrRepository.findTopByCwrCode("CWR-1")).thenReturn(Optional.of(result));
    service.inquiryAndDecideExistingCustomer(ExistingCustomerRequest.KeyType.values()[0],
      "123", customer, bouwheer());
    verify(cwrRepository, org.mockito.Mockito.times(1)).save(any());
  }

  @Test
  void inquirySkipsCreationWhenAgreementExistsOrBouwheerMissing() throws Exception {
    Customer customer = customer();
    when(customerRemoteService.validateExisting(any()))
      .thenReturn(CustomerRemoteDto.builder().custNo("CUST-1").build());
    BaseMstRemoteResponseDto<List<InquiryCwrRemoteDto>> cwrResponse = new BaseMstRemoteResponseDto<>();
    cwrResponse.setData(new ArrayList<>(List.of(cwr(1, "active", "2026-11-01T00:00:00", "2026-12-01T00:00:00"))));
    when(cwrRemoteService.inquiryCwr(any())).thenReturn(cwrResponse);

    BaseMstRemoteResponseDto<List<InquiryAgreementByNoCwrRemoteDto>> agreementResponse =
      new BaseMstRemoteResponseDto<>();
    agreementResponse.setData(List.of(mock(InquiryAgreementByNoCwrRemoteDto.class)));
    when(cwrRemoteService.inquiryAgreementByNoCwr("CWR-1")).thenReturn(agreementResponse);
    assertThat(service.inquiryAndDecideExistingCustomer(ExistingCustomerRequest.KeyType.values()[0],
      "123", customer, bouwheer())).isNull();

    agreementResponse.setData(List.of());
    assertThat(service.inquiryAndDecideExistingCustomer(ExistingCustomerRequest.KeyType.values()[0],
      "123", customer, null)).isNotNull();

    cwrResponse.setData(List.of());
    assertThat(service.inquiryAndDecideExistingCustomer(ExistingCustomerRequest.KeyType.values()[0],
      "123", customer, bouwheer())).isNull();

    when(customerRemoteService.validateExisting(any())).thenThrow(new RuntimeException("remote down"));
    assertThatThrownBy(() -> service.inquiryAndDecideExistingCustomer(ExistingCustomerRequest.KeyType.values()[0],
      "123", customer, bouwheer())).hasMessage("remote down");
  }

  private void stubQuery(String vendorCode, ExistingCustomerDto dto) {
    when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(vendorCode)))
      .thenAnswer(call -> {
        RowMapper<ExistingCustomerDto> mapper = call.getArgument(1);
        ResultSet rs = mock(ResultSet.class);
        Timestamp created = Timestamp.from(Instant.parse("2026-11-01T00:00:00Z"));
        when(rs.getTimestamp("dtm_crt")).thenReturn(created);
        when(rs.getTimestamp("dtm_upd")).thenReturn(null);
        return mapper.mapRow(rs, 0);
      });
  }

  private static ExistingCustomerDto existingDto() {
    return ExistingCustomerDto.builder().vendorCode("V001").isExisting(true)
      .identityType("KTP").identityNo("123").build();
  }

  private static InquiryCwrRemoteDto cwr(Integer version, String step, String start, String end) {
    InquiryCwrRemoteDto dto = mock(InquiryCwrRemoteDto.class);
    lenient().when(dto.getVersion()).thenReturn(version);
    lenient().when(dto.getCurrStep()).thenReturn(step);
    lenient().when(dto.getStartDt()).thenReturn(start);
    lenient().when(dto.getEndDt()).thenReturn(end);
    lenient().when(dto.getCwrNo()).thenReturn("CWR-1");
    return dto;
  }

  private static Customer customer() {
    Customer customer = new Customer();
    customer.setCustCode(UUID.randomUUID());
    customer.setCustName("Customer");
    return customer;
  }

  private static Bouwheer bouwheer() {
    Bouwheer bouwheer = new Bouwheer();
    bouwheer.setBouwheerCode(UUID.randomUUID());
    return bouwheer;
  }
}
