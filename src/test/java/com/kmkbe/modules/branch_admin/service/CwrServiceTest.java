package com.kmkbe.modules.branch_admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.kmkbe.core.domain.dto.BaseMstRemoteResponseDto;
import com.kmkbe.core.domain.dto.CwrListDto;
import com.kmkbe.core.domain.dto.DetailCwrDto;
import com.kmkbe.core.domain.dto.InquiryAgreementByNoCwrRemoteDto;
import com.kmkbe.core.domain.dto.InquiryCwrDto;
import com.kmkbe.core.domain.dto.InquiryCwrRemoteDto;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.Cwr;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.CwrRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.branch_admin.request.CreateInquiryCwrRequest;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.remote.request.InquiryCwrRemoteRequest;
import com.kmkbe.modules.remote.service.CwrRemoteService;
import com.kmkbe.modules.user.entity.MstUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CwrServiceTest {

  private static final UUID CUSTOMER_CODE = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID FINANCING_HDR_CODE = UUID.fromString("22222222-2222-2222-2222-222222222222");

  @Mock private CustomerRepository customerRepository;
  @Mock private CwrRemoteService cwrRemoteService;
  @Mock private CwrRepository cwrRepository;
  @Mock private FinancingHdrRepository financingHdrRepository;
  @Mock private AgreementRepository agreementRepository;

  private CwrService service;

  @BeforeEach
  void setUp() {
    service = new CwrService(
        customerRepository,
        cwrRemoteService,
        cwrRepository,
        financingHdrRepository,
        new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false),
        agreementRepository
    );
  }

  @Test
  void listReturnsCwrRowsWithAgreementFinancingAmountAndDefaultPagination() {
    Customer customer = customer();
    Cwr cwr = cwr("CWR001", customer, bouwheer());
    Agreement agreement = Agreement.builder()
        .financingHdr(financingHdr(customer, bouwheer()))
        .financingAmt(777D)
        .build();
    when(customerRepository.findByCustCode(CUSTOMER_CODE)).thenReturn(Optional.of(customer));
    when(cwrRepository.findAllByCustomerOrderByDtmUpdDesc(customer)).thenReturn(List.of(cwr));
    when(agreementRepository.findByCwr_CwrCode("CWR001")).thenReturn(List.of(agreement));

    PaginationResult<CwrListDto> result = service.list(CUSTOMER_CODE.toString(), new PaginationRequest());

    assertThat(result.getCurrentPage()).isEqualTo(1);
    assertThat(result.getList()).hasSize(1);
    assertThat(result.getList().get(0).getCwrCode()).isEqualTo("CWR001");
    assertThat(result.getList().get(0).getBouwheerName()).isEqualTo("PT BOUWHEER");
    assertThat(result.getList().get(0).getFinancingAmt()).isEqualTo(777D);
  }

  @Test
  void listFiltersEverySupportedSearchFieldAndReturnsEmptyForNoMatch() {
    Customer customer = customer();
    Cwr cwr = cwr("CWR001", customer, bouwheer());
    when(customerRepository.findByCustCode(CUSTOMER_CODE)).thenReturn(Optional.of(customer));
    when(cwrRepository.findAllByCustomerOrderByDtmUpdDesc(customer)).thenReturn(List.of(cwr));
    when(agreementRepository.findByCwr_CwrCode("CWR001")).thenReturn(List.of());

    assertThat(service.list(CUSTOMER_CODE.toString(), pagination("office", "bouwheer")).getList()).hasSize(1);
    assertThat(service.list(CUSTOMER_CODE.toString(), pagination("cwrNo", "CWR001")).getList()).hasSize(1);
    assertThat(service.list(CUSTOMER_CODE.toString(), pagination("cwrStartDate", "01/01/2026")).getList()).hasSize(1);
    assertThat(service.list(CUSTOMER_CODE.toString(), pagination("cwrEndDate", "31/12/2026")).getList()).hasSize(1);
    assertThat(service.list(CUSTOMER_CODE.toString(), pagination("typeCurrency", "idr")).getList()).hasSize(1);
    assertThat(service.list(CUSTOMER_CODE.toString(), pagination("plafondValue", "1000")).getList()).hasSize(1);
    assertThat(service.list(CUSTOMER_CODE.toString(), pagination("submissionValue", "250")).getList()).hasSize(1);
    assertThat(service.list(CUSTOMER_CODE.toString(), pagination("unknown", "none")).getList()).isEmpty();
  }

  @Test
  void listThrowsWhenCustomerMissing() {
    when(customerRepository.findByCustCode(CUSTOMER_CODE)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.list(CUSTOMER_CODE.toString(), new PaginationRequest()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Customer not found or not valid");
  }

  @Test
  void detailReturnsComputedRemainingPlafondAndThrowsInvalidStates() {
    Customer customer = customer();
    Cwr cwr = cwr("CWR001", customer, bouwheer());
    FinancingHdr financingHdr = financingHdr(customer, bouwheer());
    when(cwrRepository.findById("CWR001")).thenReturn(Optional.of(cwr));
    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr));

    DetailCwrDto result = service.detail("CWR001", FINANCING_HDR_CODE.toString());

    assertThat(result.getCwrCode()).isEqualTo("CWR001");
    assertThat(result.getPlafondAmt()).isEqualByComparingTo(BigDecimal.valueOf(1_000D));
    assertThat(result.getRealisationAmt()).isEqualByComparingTo(BigDecimal.valueOf(250D));
    assertThat(result.getRemainingPlafondAmt()).isEqualByComparingTo(BigDecimal.valueOf(750D));
    assertThat(result.getFinancingAmt()).isEqualByComparingTo(BigDecimal.valueOf(500D));

    when(cwrRepository.findById("404")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.detail("404", FINANCING_HDR_CODE.toString()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("CWR not found");

    Cwr noCustomer = cwr("NO-CUST", null, bouwheer());
    when(cwrRepository.findById("NO-CUST")).thenReturn(Optional.of(noCustomer));
    assertThatThrownBy(() -> service.detail("NO-CUST", FINANCING_HDR_CODE.toString()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Customer not found or not valid");

    when(cwrRepository.findById("CWR002")).thenReturn(Optional.of(cwr("CWR002", customer, bouwheer())));
    when(financingHdrRepository.findByFinancingHdrCode(UUID.fromString("33333333-3333-3333-3333-333333333333"))).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.detail("CWR002", "33333333-3333-3333-3333-333333333333"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Financing not found or not valid");
  }

  @Test
  void inquiryCwrReturnsRemoteDataAndWrapsEmptyOrRemoteErrorAsCommonInvalidException() throws Exception {
    when(cwrRepository.findTopByCwrCode("CWR001")).thenReturn(Optional.empty());
    when(cwrRemoteService.inquiryCwr(any(InquiryCwrRemoteRequest.class))).thenReturn(remoteCwrResponse(List.of(remoteCwr("CWR001"))));

    InquiryCwrDto result = service.inquiryCwr("CWR001");

    assertThat(result.getCwrCode()).isEqualTo("CWR001");
    assertThat(result.getCurrency()).isEqualTo("IDR");
    assertThat(result.getPlafondAmt()).isEqualByComparingTo(BigDecimal.valueOf(1_000D));
    assertThat(result.getRealisationAmt()).isEqualByComparingTo(BigDecimal.valueOf(250D));

    when(cwrRemoteService.inquiryCwr(any(InquiryCwrRemoteRequest.class))).thenReturn(remoteCwrResponse(List.of()));
    assertThatThrownBy(() -> service.inquiryCwr("EMPTY"))
        .isInstanceOf(CommonInvalidException.class)
        .hasMessage("Harap input CWR aktif di Confins terlebih dahulu");

    when(cwrRemoteService.inquiryCwr(any(InquiryCwrRemoteRequest.class))).thenThrow(new RuntimeException("remote down"));
    assertThatThrownBy(() -> service.inquiryCwr("ERROR"))
        .isInstanceOf(CommonInvalidException.class)
        .hasMessage("Harap input CWR aktif di Confins terlebih dahulu");
  }

  @Test
  void createInquiryCwrPersistsRemoteRowsAndThrowsInvalidReferences() throws Exception {
    Customer customer = customer();
    Bouwheer bouwheer = bouwheer();
    FinancingHdr financingHdr = financingHdr(customer, bouwheer);
    CreateInquiryCwrRequest request = createRequest("CWR001");
    when(cwrRepository.findTopByCwrCode("CWR001")).thenReturn(Optional.empty());
    when(cwrRemoteService.inquiryCwr(any(InquiryCwrRemoteRequest.class))).thenReturn(remoteCwrResponse(List.of(remoteCwr("CWR001"))));
    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr));

    service.createInquiryCwr(MstUser.builder().username("maker").build(), request);

    ArgumentCaptor<Cwr> cwrCaptor = ArgumentCaptor.forClass(Cwr.class);
    verify(cwrRepository).save(cwrCaptor.capture());
    assertThat(cwrCaptor.getValue().getCwrCode()).isEqualTo("CWR001");
    assertThat(cwrCaptor.getValue().getUsrCrt()).isEqualTo("maker");
    assertThat(cwrCaptor.getValue().getBouwheer()).isSameAs(bouwheer);
    assertThat(cwrCaptor.getValue().getCustomer()).isSameAs(customer);

    when(cwrRemoteService.inquiryCwr(any(InquiryCwrRemoteRequest.class))).thenThrow(new RuntimeException("remote down"));
    assertThatThrownBy(() -> service.createInquiryCwr(MstUser.builder().username("maker").build(), createRequest("REMOTE")))
        .isInstanceOf(CommonInvalidException.class)
        .hasMessage("Harap input CWR aktif di Confins terlebih dahulu");
  }

  @Test
  void createInquiryCwrThrowsForMissingFinancingBouwheerCustomerAndDoesNothingWhenRemoteEmpty() throws Exception {
    when(cwrRepository.findTopByCwrCode(any())).thenReturn(Optional.empty());
    when(cwrRemoteService.inquiryCwr(any(InquiryCwrRemoteRequest.class))).thenReturn(remoteCwrResponse(List.of()));
    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.createInquiryCwr(MstUser.builder().username("maker").build(), createRequest("CWR001")))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Financing not found or not valid");

    FinancingHdr noBouwheer = financingHdr(customer(), null);
    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(noBouwheer));
    assertThatThrownBy(() -> service.createInquiryCwr(MstUser.builder().username("maker").build(), createRequest("CWR002")))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Bouwheer not found or not valid");

    FinancingHdr noCustomer = financingHdr(null, bouwheer());
    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(noCustomer));
    assertThatThrownBy(() -> service.createInquiryCwr(MstUser.builder().username("maker").build(), createRequest("CWR003")))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Customer not found or not valid");

    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr(customer(), bouwheer())));
    service.createInquiryCwr(MstUser.builder().username("maker").build(), createRequest("EMPTY"));
  }

  @Test
  void inquiryListAggrReturnsAllowedAgreementStatusesAndIgnoresRemoteFailures() throws Exception {
    InquiryAgreementByNoCwrRemoteDto prospect = agreement("A1", "prospect");
    InquiryAgreementByNoCwrRemoteDto blank = agreement("A2", " ");
    InquiryAgreementByNoCwrRemoteDto goLive = agreement("A3", "Go Live");
    InquiryAgreementByNoCwrRemoteDto live = agreement("A4", "Live");
    InquiryAgreementByNoCwrRemoteDto nullStatus = agreement("A5", null);
    InquiryAgreementByNoCwrRemoteDto closed = agreement("A6", "Closed");
    BaseMstRemoteResponseDto<List<InquiryAgreementByNoCwrRemoteDto>> response = new BaseMstRemoteResponseDto<>();
    response.setData(List.of(prospect, blank, goLive, live, nullStatus, closed));
    when(cwrRemoteService.inquiryAgreementByNoCwr("CWR001")).thenReturn(response);

    assertThat(service.inquiryListAggr("CWR001")).containsExactly("A1", "A2", "A3", "A4", "A5");

    response.setData(null);
    assertThat(service.inquiryListAggr("CWR001")).isEmpty();

    when(cwrRemoteService.inquiryAgreementByNoCwr("ERROR")).thenThrow(new RuntimeException("remote down"));
    assertThat(service.inquiryListAggr("ERROR")).isEmpty();
  }

  @Test
  void validateCwrThrowsWhenDuplicateExists() {
    when(cwrRepository.findTopByCwrCode("CWR001")).thenReturn(Optional.of(cwr("CWR001", customer(), bouwheer())));

    assertThatThrownBy(() -> service.validateCwr("CWR001"))
        .isInstanceOf(CommonInvalidException.class)
        .hasMessage("Nomor CWR sudah di input sebelumnya");
  }

  @Test
  void sampleParsesEmbeddedRemotePayload() {
    InquiryCwrRemoteDto result = ReflectionTestUtils.invokeMethod(service, "sample");

    assertThat(result).isNotNull();
    assertThat(result.getCwrNo()).isEqualTo("41450CWR2024626");
    assertThat(result.getCurrency()).isEqualTo("IDR");
  }

  private static PaginationRequest pagination(String searchBy, String searchValue) {
    PaginationRequest request = new PaginationRequest();
    request.setPageNo(1);
    request.setPageSize(10);
    request.setSearchBy(searchBy);
    request.setSearchValue(searchValue);
    return request;
  }

  private static Customer customer() {
    Customer customer = new Customer();
    customer.setCustCode(CUSTOMER_CODE);
    customer.setCustName("Customer");
    customer.setCustTypeCode("PERSONAL");
    customer.setCustIdTypeCode("KTP");
    customer.setCustIdNo("123");
    customer.setCustEmail("customer@example.com");
    return customer;
  }

  private static Bouwheer bouwheer() {
    return Bouwheer.builder()
        .bouwheerCode(UUID.fromString("44444444-4444-4444-4444-444444444444"))
        .bouwheerName("PT BOUWHEER")
        .build();
  }

  private static Cwr cwr(String code, Customer customer, Bouwheer bouwheer) {
    return Cwr.builder()
        .cwrCode(code)
        .customer(customer)
        .bouwheer(bouwheer)
        .branchCode("JKT")
        .cwrType("FACTORING")
        .cwrTypeDesc("Factoring")
        .facility("Modal Kerja")
        .isRevolving(true)
        .currency("IDR")
        .cwrStartDate(LocalDateTime.of(2026, 1, 1, 0, 0))
        .cwrEndDate(LocalDateTime.of(2026, 12, 31, 0, 0))
        .plafondAmt(1_000D)
        .realisationAmt(250D)
        .status("ACTIVE")
        .usrCrt("maker")
        .dtmCrt(LocalDateTime.of(2026, 1, 1, 0, 0))
        .build();
  }

  private static FinancingHdr financingHdr(Customer customer, Bouwheer bouwheer) {
    FinancingHdr financingHdr = new FinancingHdr();
    financingHdr.setFinancingHdrCode(FINANCING_HDR_CODE);
    financingHdr.setCustomer(customer);
    financingHdr.setBouwheer(bouwheer);
    financingHdr.setFinancingAmt(500D);
    return financingHdr;
  }

  private static InquiryCwrRemoteDto remoteCwr(String cwrNo) {
    return InquiryCwrRemoteDto.builder()
        .cwrNo(cwrNo)
        .officeCode("JKT")
        .cwrType("FACTORING")
        .cwrTypeDesc("Factoring")
        .facility("Modal Kerja")
        .isRevolving(true)
        .currency("IDR")
        .startDt("2026-01-01T00:00:00")
        .endDt("2026-12-31T00:00:00")
        .plafondAmt(1_000D)
        .realisationAmt(250D)
        .cwrStatDescr("ACTIVE")
        .build();
  }

  private static BaseMstRemoteResponseDto<List<InquiryCwrRemoteDto>> remoteCwrResponse(List<InquiryCwrRemoteDto> data) {
    BaseMstRemoteResponseDto<List<InquiryCwrRemoteDto>> response = new BaseMstRemoteResponseDto<>();
    response.setData(data);
    return response;
  }

  private static CreateInquiryCwrRequest createRequest(String cwrNo) {
    CreateInquiryCwrRequest request = new CreateInquiryCwrRequest();
    request.setFinancingHdrCode(FINANCING_HDR_CODE.toString());
    request.setCwrNo(cwrNo);
    return request;
  }

  private static InquiryAgreementByNoCwrRemoteDto agreement(String agreementNo, String status) {
    InquiryAgreementByNoCwrRemoteDto dto = new InquiryAgreementByNoCwrRemoteDto();
    dto.agrmntNo = agreementNo;
    dto.agrmntStat = status;
    return dto;
  }
}
