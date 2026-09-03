package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.ApiResponse;
import com.kmkbe.core.domain.dto.AssignmentDto;
import com.kmkbe.core.domain.dto.DebtorDto;
import com.kmkbe.core.domain.dto.ExternalApiResponse;
import com.kmkbe.core.domain.dto.ExternalDownloadResponse;
import com.kmkbe.core.domain.dto.PersonDto;
import com.kmkbe.core.domain.dto.SignerAgreementDto;
import com.kmkbe.core.domain.dto.SignerCheckResultDto;
import com.kmkbe.core.domain.dto.SignerDocDto;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.AgreementFileSigning;
import com.kmkbe.core.domain.entity.Cwr;
import com.kmkbe.core.domain.entity.Debtor;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.AgreementFileSigningRepository;
import com.kmkbe.core.domain.repository.AgreementRepository;
import com.kmkbe.core.domain.repository.DebtorRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.NotifDebtorRepository;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignerServiceTest {

  private static final UUID FINANCING_HDR_CODE = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID CUSTOMER_CODE = UUID.fromString("22222222-2222-2222-2222-222222222222");

  @Mock private FinancingHdrRepository financingHdrRepository;
  @Mock private RestTemplate restTemplate;
  @Mock private DebtorRepository debtorRepository;
  @Mock private EmailService emailService;
  @Mock private AgreementRepository agreementRepository;
  @Mock private AgreementFileSigningRepository agreementFileSigningRepository;
  @Mock private AssignmentSubmissionService assignmentSubmissionService;
  @Mock private NotifDebtorRepository notifDebtorRepository;
  @Mock private AuditTrailService auditTrailService;
  @Mock private BaseRemoteService baseRemoteService;
  @Mock private SigningEligibilityService signingEligibilityService;
  @Mock private HttpServletRequest httpServletRequest;

  private SignerService service;

  @BeforeEach
  void setUp() {
    service = new SignerService(
        financingHdrRepository,
        restTemplate,
        debtorRepository,
        emailService,
        agreementRepository,
        agreementFileSigningRepository,
        assignmentSubmissionService,
        notifDebtorRepository,
        auditTrailService,
        signingEligibilityService,
        baseRemoteService
    );
    ReflectionTestUtils.setField(service, "adinsKey", "adins-key");
    ReflectionTestUtils.setField(service, "adInsKey", "adins-key");
    lenient().when(financingHdrRepository.save(any(FinancingHdr.class))).thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void assignmentListGroupByCustomerKeepsFirstRowPerCustomer() throws Exception {
    AssignmentDto first = AssignmentDto.builder().custCode(CUSTOMER_CODE).financingHdrCode(FINANCING_HDR_CODE).build();
    AssignmentDto duplicate = AssignmentDto.builder().custCode(CUSTOMER_CODE).financingHdrCode(UUID.randomUUID()).build();
    when(assignmentSubmissionService.assignmentList(eq(httpServletRequest), any(PaginationRequest.class)))
        .thenReturn(PaginationResult.<AssignmentDto>builder().currentPage(2).totalPage(9).totalData(2L).list(List.of(first, duplicate)).build());

    PaginationResult<AssignmentDto> result = service.assignmentListGroupByCustomer(httpServletRequest, new PaginationRequest());

    assertThat(result.getCurrentPage()).isEqualTo(2);
    assertThat(result.getTotalPage()).isEqualTo(1);
    assertThat(result.getTotalData()).isEqualTo(1);
    assertThat(result.getList()).containsExactly(first);
  }

  @Test
  void detailSignerReturnsSuccessAndFailResult() {
    when(debtorRepository.findById(1L)).thenReturn(Optional.of(debtor(1L)));
    when(debtorRepository.findById(99L)).thenReturn(Optional.empty());

    CommonResult<DebtorDto> found = service.detailSigner(1L);
    CommonResult<DebtorDto> missing = service.detailSigner(99L);

    assertThat(found.getCode()).isEqualTo(200);
    assertThat(found.getData().getKaryawanName()).isEqualTo("Signer One");
    assertThat(missing.getCode()).isEqualTo(400);
    assertThat(missing.getMessage()).contains("Signer tidak ditemukan");
  }

  @Test
  void signerPersonListChecksRegistrationAndSignerStatus() {
    Debtor signer = debtor(1L);
    signer.setFinancingHdrCode(FINANCING_HDR_CODE.toString());
    when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("Debtor");
    when(debtorRepository.findByDebtorName("Debtor")).thenReturn(List.of(signer));
    when(restTemplate.exchange(eq("https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/checkRegistration"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(signhubRegistrationStatus("2", "Vida")));
    when(agreementRepository.findCwr(FINANCING_HDR_CODE)).thenReturn(Optional.of(agreement()));
    when(restTemplate.exchange(eq("http://172.21.10.149:8083/mou_getsigner.php"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("ReturnObject", List.of(Map.of("SignerName", "Signer One")))));
    when(debtorRepository.findById(1L)).thenReturn(Optional.of(signer));
    when(debtorRepository.save(any(Debtor.class))).thenAnswer(invocation -> invocation.getArgument(0));

    List<DebtorDto> result = service.signerPersonList(FINANCING_HDR_CODE.toString(), "maker");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getSignhubStatus()).isEqualTo("active");
    assertThat(result.get(0).getSignerStatus()).isEqualTo("active");
  }

  @Test
  void signerPersonListResetsStatusWhenRegistrationFlowCannotUpdateDebtor() {
    Debtor signer = debtor(1L);
    signer.setFinancingHdrCode(FINANCING_HDR_CODE.toString());
    when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("Debtor");
    when(debtorRepository.findByDebtorName("Debtor")).thenReturn(List.of(signer));
    when(restTemplate.exchange(eq("https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/checkRegistration"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(signhubRegistrationStatus("0", "Vida")));
    when(agreementRepository.findCwr(FINANCING_HDR_CODE)).thenReturn(Optional.empty());
    when(debtorRepository.findById(1L)).thenReturn(Optional.empty());

    List<DebtorDto> result = service.signerPersonList(FINANCING_HDR_CODE.toString(), "maker");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getSignhubStatus()).isEqualTo("not register");
    assertThat(result.get(0).getSignerStatus()).isEqualTo("not active");
  }

  @Test
  void signerPersonListWrapsFailedAsyncProcessing() {
    Debtor signer = debtor(1L);
    SignerService spy = org.mockito.Mockito.spy(service);
    java.util.concurrent.CompletableFuture<DebtorDto> failed = new java.util.concurrent.CompletableFuture<>();
    failed.completeExceptionally(new RuntimeException("boom"));
    when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("Debtor");
    when(debtorRepository.findByDebtorName("Debtor")).thenReturn(List.of(signer));
    org.mockito.Mockito.doReturn(failed).when(spy).processDebtorAsync(any(Debtor.class), anyString(), anyString());

    assertThatThrownBy(() -> spy.signerPersonList(FINANCING_HDR_CODE.toString(), "maker"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Error processing debtors");
  }

  @Test
  void checkSignerDanasaktiWrapsFailedAsyncProcessing() {
    Debtor signer = debtor(1L);
    SignerService spy = org.mockito.Mockito.spy(service);
    java.util.concurrent.CompletableFuture<DebtorDto> failed = new java.util.concurrent.CompletableFuture<>();
    failed.completeExceptionally(new RuntimeException("boom"));
    when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("Debtor");
    when(debtorRepository.findByDebtorName("Debtor")).thenReturn(List.of(signer));
    org.mockito.Mockito.doReturn(failed).when(spy).processDebtorAsync(any(Debtor.class), anyString(), anyString());

    assertThatThrownBy(() -> spy.checkSignerDanasakti(FINANCING_HDR_CODE.toString(), "maker"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Error processing debtors");
  }

  @Test
  void signerPersonListCoversRegistrationAndSignerFallbackVariants() {
    Debtor signer = debtor(1L);
    signer.setFinancingHdrCode(FINANCING_HDR_CODE.toString());
    when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("Debtor");
    when(debtorRepository.findByDebtorName("Debtor")).thenReturn(List.of(signer));
    when(restTemplate.exchange(eq("https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/checkRegistration"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(signhubRegistrationStatus("9", "Vida")));
    when(agreementRepository.findCwr(FINANCING_HDR_CODE)).thenReturn(Optional.of(agreement()));
    when(restTemplate.exchange(eq("http://172.21.10.149:8083/mou_getsigner.php"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of()));
    when(debtorRepository.findById(1L)).thenReturn(Optional.of(signer));
    when(debtorRepository.save(any(Debtor.class))).thenAnswer(invocation -> invocation.getArgument(0));

    List<DebtorDto> noReturnObject = service.signerPersonList(FINANCING_HDR_CODE.toString(), "maker");

    assertThat(noReturnObject.get(0).getSignhubStatus()).isEqualTo("not register");
    assertThat(noReturnObject.get(0).getSignerStatus()).isEqualTo("not active");

    when(restTemplate.exchange(eq("https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/checkRegistration"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(new ResponseEntity<>(Map.of("status", Map.of("code", 1)), HttpStatus.BAD_REQUEST));
    when(restTemplate.exchange(eq("http://172.21.10.149:8083/mou_getsigner.php"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(new ResponseEntity<>(Map.of("ReturnObject", List.of(Map.of("SignerName", "Signer One"))), HttpStatus.BAD_REQUEST));

    List<DebtorDto> nonOk = service.signerPersonList(FINANCING_HDR_CODE.toString(), "maker");

    assertThat(nonOk.get(0).getSignhubStatus()).isEqualTo("not register");
    assertThat(nonOk.get(0).getSignerStatus()).isEqualTo("not active");
  }

  @Test
  void signerPersonListCoversRegistrationNullAndEmptyBranches() {
    Debtor signer = debtor(1L);
    signer.setFinancingHdrCode(FINANCING_HDR_CODE.toString());
    when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("Debtor");
    when(debtorRepository.findByDebtorName("Debtor")).thenReturn(List.of(signer));
    when(debtorRepository.findById(1L)).thenReturn(Optional.of(signer));
    when(debtorRepository.save(any(Debtor.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(agreementRepository.findCwr(FINANCING_HDR_CODE)).thenReturn(Optional.of(agreement()));

    when(restTemplate.exchange(eq("https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/checkRegistration"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(null));
    when(restTemplate.exchange(eq("http://172.21.10.149:8083/mou_getsigner.php"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(null));
    assertThat(service.signerPersonList(FINANCING_HDR_CODE.toString(), "maker").get(0).getSignerStatus()).isEqualTo("not active");

    when(restTemplate.exchange(eq("https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/checkRegistration"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("status", Map.of("code", 0))));
    when(restTemplate.exchange(eq("http://172.21.10.149:8083/mou_getsigner.php"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("ReturnObject", List.of(Map.of("SignerName", "Signer One")))));
    assertThat(service.signerPersonList(FINANCING_HDR_CODE.toString(), "maker").get(0).getSignerStatus()).isEqualTo("active");

    when(restTemplate.exchange(eq("https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/checkRegistration"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("status", Map.of("code", 0), "registrationData", List.of())));
    when(restTemplate.exchange(eq("http://172.21.10.149:8083/mou_getsigner.php"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("ReturnObject", List.of(Map.of("SignerName", "Signer One")))));
    service.signerPersonList(FINANCING_HDR_CODE.toString(), "maker");

    when(restTemplate.exchange(eq("https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/checkRegistration"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("status", Map.of("code", 0), "registrationData", List.of(Map.of("vendor", "Other", "registrationStatus", "2")))));
    when(restTemplate.exchange(eq("http://172.21.10.149:8083/mou_getsigner.php"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("ReturnObject", List.of(Map.of("SignerName", "Signer One")))));
    service.signerPersonList(FINANCING_HDR_CODE.toString(), "maker");

    when(restTemplate.exchange(eq("https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/checkRegistration"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of()));
    when(restTemplate.exchange(eq("http://172.21.10.149:8083/mou_getsigner.php"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("ReturnObject", List.of(Map.of("SignerName", "Signer One")))));
    service.signerPersonList(FINANCING_HDR_CODE.toString(), "maker");

    when(restTemplate.exchange(eq("https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/checkRegistration"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
          put("status", new java.util.HashMap<String, Object>() {{
            put("code", null);
          }});
        }}));
    when(restTemplate.exchange(eq("http://172.21.10.149:8083/mou_getsigner.php"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("ReturnObject", List.of(Map.of("SignerName", "Signer One")))));
    service.signerPersonList(FINANCING_HDR_CODE.toString(), "maker");

    when(restTemplate.exchange(eq("https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/checkRegistration"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("status", Map.of("code", 1), "registrationData", List.of(Map.of("vendor", "Vida", "registrationStatus", "2")))));
    when(restTemplate.exchange(eq("http://172.21.10.149:8083/mou_getsigner.php"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("ReturnObject", List.of(Map.of("SignerName", "Signer One")))));
    service.signerPersonList(FINANCING_HDR_CODE.toString(), "maker");

    when(restTemplate.exchange(eq("https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/checkRegistration"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
          put("status", Map.of("code", 0));
          put("registrationData", null);
        }}));
    when(restTemplate.exchange(eq("http://172.21.10.149:8083/mou_getsigner.php"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("ReturnObject", List.of(Map.of("SignerName", "Signer One")))));
    service.signerPersonList(FINANCING_HDR_CODE.toString(), "maker");
  }

  @Test
  void signerPersonListResetsPersistedDebtorWhenRegistrationApiThrows() {
    Debtor signer = debtor(1L);
    signer.setFinancingHdrCode(FINANCING_HDR_CODE.toString());
    when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("Debtor");
    when(debtorRepository.findByDebtorName("Debtor")).thenReturn(List.of(signer));
    when(restTemplate.exchange(eq("https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/checkRegistration"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenThrow(new RuntimeException("registration down"));
    when(debtorRepository.findById(1L)).thenReturn(Optional.of(signer));
    when(debtorRepository.save(any(Debtor.class))).thenAnswer(invocation -> invocation.getArgument(0));

    List<DebtorDto> result = service.signerPersonList(FINANCING_HDR_CODE.toString(), "maker");

    assertThat(result.get(0).getSignhubStatus()).isEqualTo("not register");
    assertThat(result.get(0).getSignerStatus()).isEqualTo("not active");
  }

  @Test
  void checkSignerDanasaktiHandlesEmptyAndPopulatedDebtors() {
    when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE))
        .thenReturn("Debtor")
        .thenReturn("Debtor");
    when(debtorRepository.findByDebtorName("Debtor"))
        .thenReturn(List.of())
        .thenReturn(List.of(debtor(1L)));

    assertThat(service.checkSignerDanasakti(FINANCING_HDR_CODE.toString(), "maker")).isEmpty();

    when(restTemplate.exchange(eq("https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/user/checkRegistration"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(signhubRegistrationStatus("1", "Vida")));
    when(agreementRepository.findCwr(FINANCING_HDR_CODE)).thenReturn(Optional.of(agreement()));
    when(restTemplate.exchange(eq("http://172.21.10.149:8083/mou_getsigner.php"), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("ReturnObject", List.of(Map.of("SignerName", "Other")))));
    when(debtorRepository.findById(1L)).thenReturn(Optional.of(debtor(1L)));
    when(debtorRepository.save(any(Debtor.class))).thenAnswer(invocation -> invocation.getArgument(0));

    List<DebtorDto> result = service.checkSignerDanasakti(FINANCING_HDR_CODE.toString(), "maker");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getSignhubStatus()).isEqualTo("pending");
    assertThat(result.get(0).getSignerStatus()).isEqualTo("not active");
  }

  @Test
  void createDebtorSendsInvitationWhenNotRegisteredAndRejectsDuplicate() {
    DebtorDto request = debtorDto();
    when(debtorRepository.existsByIdentityNo("KTP001")).thenReturn(false);
    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(registrationResponse("0")))
        .thenReturn(ResponseEntity.ok(Map.of("status", Map.of("code", 0), "link", "https://invite")));
    when(debtorRepository.save(any(Debtor.class))).thenAnswer(invocation -> {
      Debtor debtor = invocation.getArgument(0);
      debtor.setDebtorId(7L);
      return debtor;
    });
    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr()));

    DebtorDto result = service.createDebtor(request, "maker");

    assertThat(result.getRegistrationMessage()).isEqualTo("Registrasi berhasil dan undangan telah dikirim");
    verify(emailService).sendInvitationLinkEmail("signer@example.com", "https://invite", "Signer One");
    verify(notifDebtorRepository).save(any());

    when(debtorRepository.existsByIdentityNo("KTP001")).thenReturn(true);
    assertThatThrownBy(() -> service.createDebtor(request, "maker"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("NIK yang digunakan sudah terdaftar");
  }

  @Test
  void createDebtorHandlesPendingActiveUnknownAndApiErrors() {
    DebtorDto request = debtorDto();
    when(debtorRepository.existsByIdentityNo("KTP001")).thenReturn(false);
    when(debtorRepository.save(any(Debtor.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr()));

    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class))).thenReturn(ResponseEntity.ok(registrationResponse("1")));
    assertThat(service.createDebtor(request, "maker").getRegistrationMessage()).isEqualTo("Akun sudah registrasi, namun belum di aktivasi");

    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class))).thenReturn(ResponseEntity.ok(registrationResponse("2")));
    assertThat(service.createDebtor(request, "maker").getRegistrationMessage()).isEqualTo("Signer person sudah register dan aktivasi");

    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class))).thenReturn(ResponseEntity.ok(registrationResponse("9")));
    assertThatThrownBy(() -> service.createDebtor(request, "maker"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Status registrasi tidak dikenali: 9");

    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class))).thenReturn(ResponseEntity.ok(null));
    assertThatThrownBy(() -> service.createDebtor(request, "maker"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("API registrasi tidak memberikan response");
  }

  @Test
  void createDebtorHandlesRegistrationAndInvitationFailures() {
    DebtorDto request = debtorDto();
    when(debtorRepository.existsByIdentityNo("KTP001")).thenReturn(false);

    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("status", Map.of("code", 1, "message", "registration failed"))));
    assertThatThrownBy(() -> service.createDebtor(request, "maker"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("registration failed");

    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("status", Map.of("code", 8165))))
        .thenReturn(ResponseEntity.ok(null));
    when(debtorRepository.save(any(Debtor.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr()));
    assertThatThrownBy(() -> service.createDebtor(request, "maker"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("API undangan tidak memberikan response");

    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("status", Map.of("code", 8165))))
        .thenReturn(ResponseEntity.ok(Map.of("status", Map.of("code", 99, "message", "invite failed"))));
    assertThatThrownBy(() -> service.createDebtor(request, "maker"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("invite failed");

    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("status", Map.of("code", 8165))))
        .thenReturn(ResponseEntity.ok(Map.of("status", Map.of("code", 0))));
    assertThatThrownBy(() -> service.createDebtor(request, "maker"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Gagal generate link undangan");
  }

  @Test
  void createDebtorFailsWhenFinancingHeaderIsMissingDuringSave() {
    DebtorDto request = debtorDto();
    when(debtorRepository.existsByIdentityNo("KTP001")).thenReturn(false);
    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(registrationResponse("1")));
    when(debtorRepository.save(any(Debtor.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.createDebtor(request, "maker"))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("FinancingHdr dengan code");
  }

  @Test
  void createDebtorDefaultsRegistrationStatusWhenRegistrationDataIsEmptyAndStatusAbsent() {
    DebtorDto request = debtorDto();
    when(debtorRepository.existsByIdentityNo("KTP001")).thenReturn(false);
    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of()))
        .thenReturn(ResponseEntity.ok(Map.of("link", "https://invite")));
    when(debtorRepository.save(any(Debtor.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr()));

    DebtorDto result = service.createDebtor(request, "maker");

    assertThat(result.getRegistrationMessage()).isEqualTo("Registrasi berhasil dan undangan telah dikirim");
  }

  @Test
  void createDebtorDefaultsRegistrationStatusWhenRegistrationDataIsNullOrEmpty() {
    DebtorDto request = debtorDto();
    when(debtorRepository.existsByIdentityNo("KTP001")).thenReturn(false);
    when(debtorRepository.save(any(Debtor.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr()));

    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
          put("registrationData", null);
        }}))
        .thenReturn(ResponseEntity.ok(Map.of("link", "https://invite")));
    assertThat(service.createDebtor(request, "maker").getRegistrationMessage()).isEqualTo("Registrasi berhasil dan undangan telah dikirim");

    when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("registrationData", List.of())))
        .thenReturn(ResponseEntity.ok(Map.of("link", "https://invite")));
    assertThat(service.createDebtor(request, "maker").getRegistrationMessage()).isEqualTo("Registrasi berhasil dan undangan telah dikirim");
  }

  @Test
  void mergeSignersRemovesDuplicateNameAndPosition() {
    PersonDto left = person("Budi", "Direktur", "Ani", "Manager");
    PersonDto right = person("Budi", "Direktur");

    PersonDto result = service.mergeSigners(List.of(left, right));

    assertThat(result.getStatusCode()).isEqualTo("200");
    assertThat(result.getSigners()).hasSize(2);
  }

  @Test
  void mergeSignersReturnsErrorMessageAndNeverThrowsWhenSignerListIsNull() {
    PersonDto nullSignerList = new PersonDto();
    nullSignerList.setStatusCode("200");
    nullSignerList.setMessage("Success");

    PersonDto externalError = new PersonDto();
    externalError.setStatusCode("500");
    externalError.setMessage("Error: CustNo tidak tersedia untuk agreement AGR001");

    PersonDto result = service.mergeSigners(List.of(nullSignerList, externalError));

    assertThat(result.getStatusCode()).isEqualTo("500");
    assertThat(result.getMessage()).isEqualTo("Error: CustNo tidak tersedia untuk agreement AGR001");
    assertThat(result.getSigners()).isEmpty();
  }

  @Test
  void signerAgreementReturnsRowsNotFoundAndBadRequest() {
    Agreement agreement = Agreement.builder().agreementCode("AGR001").financingHdr(financingHdr()).build();
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(List.of(agreement));
    assertThat(service.signerAgreement(FINANCING_HDR_CODE.toString())).extracting(SignerAgreementDto::getAgreementCode).containsExactly("AGR001");

    when(agreementRepository.findByFinancingHdr_FinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(List.of());
    assertThat(service.signerAgreement(FINANCING_HDR_CODE.toString()).get(0).getAgreementCode()).isEqualTo("NOT_FOUND");

    assertThatThrownBy(() -> service.signerAgreement("not-uuid"))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Format UUID tidak valid");
  }

  @Test
  void getSignersFromExternalApiMapsResponseAndFailure() {
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode2(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of(agreement()));
    when(baseRemoteService.Mou_GetSigner_forward()).thenReturn("http://signer");
    when(restTemplate.exchange(eq("http://signer"), eq(HttpMethod.POST), any(), eq(ExternalApiResponse.class)))
        .thenReturn(ResponseEntity.ok(externalSignerResponse("Budi")));

    PersonDto result = service.getSignersFromExternalApi(FINANCING_HDR_CODE.toString(), "AGR001");

    assertThat(result.getStatusCode()).isEqualTo("200");
    assertThat(result.getSigners()).extracting(PersonDto.Signer::getSignerName).containsExactly("Budi");

    when(agreementRepository.findByFinancingHdr_FinancingHdrCode2(FINANCING_HDR_CODE, "MISSING")).thenReturn(Optional.empty());
    PersonDto error = service.getSignersFromExternalApi(FINANCING_HDR_CODE.toString(), "MISSING");
    assertThat(error.getStatusCode()).isEqualTo("500");
    assertThat(error.getMessage()).contains("Agreement not found");
    assertThat(error.getSigners()).isEmpty();
  }

  @Test
  void getSignersFromExternalApiMapsNullAndEmptyExternalResponses() {
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode2(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of(agreement()));
    when(baseRemoteService.Mou_GetSigner_forward()).thenReturn("http://signer");
    when(restTemplate.exchange(eq("http://signer"), eq(HttpMethod.POST), any(), eq(ExternalApiResponse.class)))
        .thenReturn(ResponseEntity.ok(null));

    PersonDto noResponse = service.getSignersFromExternalApi(FINANCING_HDR_CODE.toString(), "AGR001");

    assertThat(noResponse.getStatusCode()).isEqualTo("500");
    assertThat(noResponse.getSigners()).isEmpty();

    ExternalApiResponse empty = new ExternalApiResponse();
    empty.setStatusCode("200");
    empty.setMessage("Success");
    empty.setReturnObject(List.of());
    when(restTemplate.exchange(eq("http://signer"), eq(HttpMethod.POST), any(), eq(ExternalApiResponse.class)))
        .thenReturn(ResponseEntity.ok(empty));

    PersonDto emptyResponse = service.getSignersFromExternalApi(FINANCING_HDR_CODE.toString(), "AGR001");

    assertThat(emptyResponse.getMessage()).isEqualTo("Tidak ada data signer yang tersedia");
    assertThat(emptyResponse.getSigners()).isEmpty();

    ExternalApiResponse nullReturnObject = new ExternalApiResponse();
    nullReturnObject.setStatusCode("200");
    nullReturnObject.setMessage("Success");
    nullReturnObject.setReturnObject(null);
    when(restTemplate.exchange(eq("http://signer"), eq(HttpMethod.POST), any(), eq(ExternalApiResponse.class)))
        .thenReturn(ResponseEntity.ok(nullReturnObject));

    PersonDto nullReturn = service.getSignersFromExternalApi(FINANCING_HDR_CODE.toString(), "AGR001");

    assertThat(nullReturn.getSigners()).isEmpty();
  }

  @Test
  void getSignersForGroupMergesFriendAgreementSignersAndValidatesInput() {
    UUID friendCode = UUID.fromString("33333333-3333-3333-3333-333333333333");
    AssignmentDto target = AssignmentDto.builder().custCode(CUSTOMER_CODE).financingHdrCode(FINANCING_HDR_CODE).build();
    AssignmentDto friend = AssignmentDto.builder().custCode(CUSTOMER_CODE).financingHdrCode(friendCode).build();
    Agreement agreement = agreement();
    Agreement friendAgreement = agreement();
    friendAgreement.setAgreementCode("AGR002");
    friendAgreement.getFinancingHdr().setFinancingHdrCode(friendCode);
    when(agreementRepository.findAllByFinancingHdrCodes(List.of(FINANCING_HDR_CODE, friendCode)))
        .thenReturn(List.of(agreement, friendAgreement));
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode2(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of(agreement()));
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode2(friendCode, "AGR002")).thenReturn(Optional.of(friendAgreement));
    when(baseRemoteService.Mou_GetSigner_forward()).thenReturn("http://signer");
    when(restTemplate.exchange(eq("http://signer"), eq(HttpMethod.POST), any(), eq(ExternalApiResponse.class)))
        .thenReturn(ResponseEntity.ok(externalSignerResponse("Budi")))
        .thenReturn(ResponseEntity.ok(externalSignerResponse("Ani")));

    PersonDto result = service.getSignersForGroup(FINANCING_HDR_CODE.toString(), List.of(target, friend));

    assertThat(result.getSigners()).extracting(PersonDto.Signer::getSignerName).containsExactlyInAnyOrder("Budi", "Ani");

    assertThatThrownBy(() -> service.getSignersForGroup(FINANCING_HDR_CODE.toString(), List.of()))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("FinancingHdrCode tidak ditemukan");

    when(agreementRepository.findAllByFinancingHdrCodes(List.of(FINANCING_HDR_CODE, friendCode)))
        .thenReturn(List.of(agreement));
    assertThatThrownBy(() -> service.getSignersForGroup(FINANCING_HDR_CODE.toString(), List.of(target, friend)))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Agreement tidak ditemukan");
  }

  @Test
  void getSignersFromExternalApi2CachesAndCompareSigners() {
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode2(FINANCING_HDR_CODE, "AGR001")).thenReturn(Optional.of(agreement()));
    when(baseRemoteService.Mou_GetSigner_forward()).thenReturn("http://signer");
    when(restTemplate.exchange(eq("http://signer"), eq(HttpMethod.POST), any(), eq(ExternalApiResponse.class)))
        .thenReturn(ResponseEntity.ok(externalSignerResponse("Budi")));

    assertThat(service.getSignersFromExternalApi2(FINANCING_HDR_CODE.toString(), "AGR001")).containsExactly("Budi");
    assertThat(service.getSignersFromExternalApi2(FINANCING_HDR_CODE.toString(), "AGR001")).containsExactly("Budi");

    when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("Debtor");
    when(debtorRepository.findKaryawanNamesByDebtorName("Debtor")).thenReturn(List.of("Budi"));
    SignerCheckResultDto compare = service.compareSigners(FINANCING_HDR_CODE.toString(), "AGR001");
    assertThat(compare.getUnmatchedSigners()).isEmpty();

    SignerCheckResultDto noMatch = service.createComparisonResult(List.of("Ani"), List.of("Budi"));
    assertThat(noMatch.getUnmatchedSigners()).containsExactly("Ani");
  }

  @Test
  void getSignersFromExternalApi2ValidatesAgreementAndCustNo() {
    Agreement blankCustNoAgreement = agreement();
    blankCustNoAgreement.getCwr().getCustomer().setCustNo(" ");
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode2(FINANCING_HDR_CODE, "AGR001"))
        .thenReturn(Optional.of(blankCustNoAgreement));

    assertThatThrownBy(() -> service.getSignersFromExternalApi2(FINANCING_HDR_CODE.toString(), "AGR001"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("custNo untuk financingHdrCode");

    when(agreementRepository.findByFinancingHdr_FinancingHdrCode2(FINANCING_HDR_CODE, "AGR002"))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getSignersFromExternalApi2(FINANCING_HDR_CODE.toString(), "AGR002"))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to get signers from external API");

    assertThatThrownBy(() -> service.compareSigners("not-uuid", "AGR001"))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to get signers from database");

    Agreement nullCustNoAgreement = agreement();
    nullCustNoAgreement.getCwr().getCustomer().setCustNo(null);
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode2(FINANCING_HDR_CODE, "AGR003"))
        .thenReturn(Optional.of(nullCustNoAgreement));
    assertThatThrownBy(() -> service.getSignersFromExternalApi2(FINANCING_HDR_CODE.toString(), "AGR003"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("custNo untuk financingHdrCode");
  }

  @Test
  void downloadDocumentValidatesInputAndMapsExternalResponses() {
    assertThat(((ApiResponse<?>) service.downloadDocument("", "maker").getBody()).getMessage()).isEqualTo("DocumentId is required");

    when(agreementFileSigningRepository.findByDocumentId("DOC001")).thenReturn(Optional.of(AgreementFileSigning.builder().documentId("DOC001").build()));
    ExternalDownloadResponse success = new ExternalDownloadResponse();
    ExternalDownloadResponse.Status status = new ExternalDownloadResponse.Status();
    status.setCode(0);
    status.setMessage("ok");
    success.setStatus(status);
    success.setPdfBase64(Base64.getEncoder().encodeToString("pdf".getBytes(StandardCharsets.UTF_8)));
    when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(ExternalDownloadResponse.class)))
        .thenReturn(ResponseEntity.ok(success));

    ResponseEntity<ApiResponse<?>> response = service.downloadDocument("DOC001", "maker");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat((Map<String, Object>) response.getBody().getData()).containsEntry("filename", "document_DOC001.pdf");

    status.setCode(99);
    status.setMessage("archive");
    ResponseEntity<ApiResponse<?>> archived = service.downloadDocument("DOC001", "maker");
    assertThat(archived.getBody().isSuccess()).isFalse();
    assertThat(archived.getBody().getMessage()).contains("diarsipkan");
  }

  @Test
  void downloadDocumentHandlesExternalAndPayloadFailures() {
    assertThat(((ApiResponse<?>) service.downloadDocument(null, "maker").getBody()).getMessage()).isEqualTo("DocumentId is required");

    when(agreementFileSigningRepository.findByDocumentId("MISSING")).thenReturn(Optional.empty());
    ResponseEntity<ApiResponse<?>> missing = service.downloadDocument("MISSING", "maker");
    assertThat(missing.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(missing.getBody().getMessage()).isEqualTo("Document not found in database");

    when(agreementFileSigningRepository.findByDocumentId("DOC001")).thenReturn(Optional.of(AgreementFileSigning.builder().documentId("DOC001").build()));
    when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(ExternalDownloadResponse.class)))
        .thenThrow(new org.springframework.web.client.HttpClientErrorException(HttpStatus.BAD_REQUEST, "bad", "external".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

    ResponseEntity<ApiResponse<?>> externalError = service.downloadDocument("DOC001", "maker");

    assertThat(externalError.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(externalError.getBody().getMessage()).isEqualTo("External API error");

    when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(ExternalDownloadResponse.class)))
        .thenReturn(ResponseEntity.ok(null));
    ResponseEntity<ApiResponse<?>> empty = service.downloadDocument("DOC001", "maker");
    assertThat(empty.getBody().getMessage()).isEqualTo("Empty response from external API");

    ExternalDownloadResponse invalid = new ExternalDownloadResponse();
    ExternalDownloadResponse.Status status = new ExternalDownloadResponse.Status();
    status.setCode(0);
    status.setMessage("ok");
    invalid.setStatus(status);
    invalid.setPdfBase64("not-base64");
    when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(ExternalDownloadResponse.class)))
        .thenReturn(ResponseEntity.ok(invalid));
    ResponseEntity<ApiResponse<?>> invalidFormat = service.downloadDocument("DOC001", "maker");
    assertThat(invalidFormat.getBody().getMessage()).isEqualTo("Invalid document format");

    when(agreementFileSigningRepository.findByDocumentId("BROKEN")).thenThrow(new RuntimeException("db down"));
    ResponseEntity<ApiResponse<?>> unexpected = service.downloadDocument("BROKEN", "maker");
    assertThat(unexpected.getBody().getMessage()).contains("Internal server error: db down");
  }

  @Test
  void signerDocListMapsStatusVariationsAndFallbackDates() {
    Agreement agreement = Agreement.builder().agreementCode("AGR001").build();
    FinancingHdr financingHdr = financingHdr();
    AgreementFileSigning failed = signing("DOC-FAILED", null);
    AgreementFileSigning process = signing("DOC-PROCESS", LocalDateTime.of(2026, 1, 2, 0, 0));
    AgreementFileSigning waiting = signing("DOC-WAITING", null);
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(List.of(agreement));
    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr));
    when(financingHdrRepository.findSignerNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(List.of("Signer One"));
    when(agreementFileSigningRepository.findByKaryawan("Signer One")).thenReturn(List.of(failed, process, waiting));
    when(agreementRepository.findCwrCodesByAgreementCodes(List.of("AGR001", "AGR001", "AGR001")))
        .thenReturn(java.util.Collections.singletonList(new Object[]{"AGR001", "CWR001"}));
    when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("statusSigning", List.of(
            Map.of("documentId", "DOC-FAILED", "signer", List.of(Map.of("signStatus", "2"))),
            Map.of("documentId", "DOC-PROCESS", "signer", List.of(Map.of("signStatus", "3"))),
            Map.of("documentId", "DOC-WAITING", "signer", List.of(Map.of("signStatus", "0")))
        ))));
    when(agreementFileSigningRepository.save(any(AgreementFileSigning.class))).thenAnswer(invocation -> invocation.getArgument(0));

    List<SignerDocDto> result = service.signerDocList(FINANCING_HDR_CODE.toString(), "maker");

    assertThat(result).extracting(SignerDocDto::getStatus)
        .containsExactly("Sign Failed", "Signing in Process", "Menunggu TTD");
    assertThat(financingHdr.getFinancingStep()).isEqualTo("SIGNING");
  }

  @Test
  void signerDocListHandlesMissingFinancingHeaderAndExternalStatusFailure() {
    Agreement agreement = Agreement.builder().agreementCode("AGR001").build();
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(List.of(agreement));
    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.signerDocList(FINANCING_HDR_CODE.toString(), "maker"))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Financing header not found");

    FinancingHdr financingHdr = financingHdr();
    AgreementFileSigning signing = signing("DOC001", null);
    signing.setDtmCrt(null);
    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr));
    when(financingHdrRepository.findSignerNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(List.of("Signer One"));
    when(agreementFileSigningRepository.findByKaryawan("Signer One")).thenReturn(List.of(signing));
    when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class))).thenThrow(new RuntimeException("external down"));
    when(agreementFileSigningRepository.save(any(AgreementFileSigning.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(agreementRepository.findCwrCodesByAgreementCodes(List.of("AGR001")))
        .thenReturn(java.util.Collections.singletonList(new Object[]{"AGR001", "CWR001"}));

    List<SignerDocDto> result = service.signerDocList(FINANCING_HDR_CODE.toString(), "maker");

    assertThat(result.get(0).getStatus()).isEqualTo("Menunggu TTD");
    assertThat(result.get(0).getVerifDate()).isNull();
  }

  @Test
  void signerDocListCoversExternalStatusNullEmptyAndPartialBranches() {
    Agreement agreement = Agreement.builder().agreementCode("AGR001").build();
    FinancingHdr financingHdr = financingHdr();
    AgreementFileSigning signedWithVerifDate = signing("DOC-SIGNED", LocalDateTime.of(2026, 1, 3, 0, 0));
    AgreementFileSigning partial = signing("DOC-PARTIAL", null);
    AgreementFileSigning noMatch = signing("DOC-NOMATCH", null);
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(List.of(agreement));
    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr));
    when(financingHdrRepository.findSignerNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(List.of("Signer One"));
    when(agreementFileSigningRepository.findByKaryawan("Signer One")).thenReturn(List.of(signedWithVerifDate, partial, noMatch));
    when(agreementFileSigningRepository.save(any(AgreementFileSigning.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("statusSigning", List.of(
            Map.of("documentId", "DOC-SIGNED", "signer", List.of(Map.of("signStatus", "1"))),
            Map.of("documentId", "DOC-PARTIAL", "signer", List.of(Map.of("signStatus", "1"), Map.of("signStatus", "0")))
        ))));
    when(agreementRepository.findCwrCodesByAgreementCodes(List.of("AGR001", "AGR001", "AGR001")))
        .thenReturn(java.util.Collections.singletonList(new Object[]{"AGR001", "CWR001"}));

    List<SignerDocDto> result = service.signerDocList(FINANCING_HDR_CODE.toString(), "maker");

    assertThat(result).extracting(SignerDocDto::getStatus)
        .containsExactly("signed", "Signing in Process", null);

    when(agreementFileSigningRepository.findByKaryawan("Signer One")).thenReturn(List.of(signing("DOC-NULL", null), signing("DOC-EMPTY", null)));
    when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
          put("statusSigning", null);
        }}))
        .thenReturn(ResponseEntity.ok(Map.of("statusSigning", List.of())));
    when(agreementRepository.findCwrCodesByAgreementCodes(List.of("AGR001", "AGR001")))
        .thenReturn(java.util.Collections.singletonList(new Object[]{"AGR001", "CWR001"}));

    List<SignerDocDto> nullAndEmpty = service.signerDocList(FINANCING_HDR_CODE.toString(), "maker");

    assertThat(nullAndEmpty).hasSize(2);
  }

  @Test
  void signerDocListCoversNonSuccessfulAndEmptyExternalStatusResponses() {
    Agreement agreement = Agreement.builder().agreementCode("AGR001").build();
    FinancingHdr financingHdr = financingHdr();
    AgreementFileSigning non2xx = signing("DOC-NON-2XX", null);
    AgreementFileSigning emptyBody = signing("DOC-EMPTY-BODY", null);
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(List.of(agreement));
    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr));
    when(financingHdrRepository.findSignerNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(List.of("Signer One"));
    when(agreementFileSigningRepository.findByKaryawan("Signer One")).thenReturn(List.of(non2xx, emptyBody));
    when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(new ResponseEntity<>(Map.of("statusSigning", List.of()), HttpStatus.BAD_REQUEST))
        .thenReturn(ResponseEntity.ok(null));
    when(agreementFileSigningRepository.save(any(AgreementFileSigning.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(agreementRepository.findCwrCodesByAgreementCodes(List.of("AGR001", "AGR001")))
        .thenReturn(java.util.Collections.singletonList(new Object[]{"AGR001", "CWR001"}));

    List<SignerDocDto> result = service.signerDocList(FINANCING_HDR_CODE.toString(), "maker");

    assertThat(result).hasSize(2);
  }

  @Test
  void signerDocListChecksExternalStatusAndUpdatesFinancingStep() {
    Agreement agreement = Agreement.builder().agreementCode("AGR001").build();
    FinancingHdr financingHdr = financingHdr();
    AgreementFileSigning signing = AgreementFileSigning.builder()
        .agreementFileId(1L)
        .agreementCode("AGR001")
        .documentId("DOC001")
        .financingHdrCode(FINANCING_HDR_CODE.toString())
        .dtmCrt(LocalDateTime.of(2026, 1, 1, 0, 0))
        .build();
    when(agreementRepository.findByFinancingHdr_FinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(List.of(agreement));
    when(financingHdrRepository.findByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(Optional.of(financingHdr));
    when(financingHdrRepository.findSignerNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(List.of("Signer One"));
    when(agreementFileSigningRepository.findByKaryawan("Signer One")).thenReturn(List.of(signing));
    when(agreementRepository.findCwrCodesByAgreementCodes(List.of("AGR001")))
        .thenReturn(java.util.Collections.singletonList(new Object[]{"AGR001", "CWR001"}));
    when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("statusSigning", List.of(Map.of(
            "documentId", "DOC001",
            "signer", List.of(Map.of("signStatus", "1"), Map.of("signStatus", "1"))
        )))));
    when(agreementFileSigningRepository.save(any(AgreementFileSigning.class))).thenAnswer(invocation -> invocation.getArgument(0));

    List<SignerDocDto> result = service.signerDocList(FINANCING_HDR_CODE.toString(), "maker");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getStatus()).isEqualTo("signed");
    assertThat(result.get(0).getSignProgress()).isEqualTo("2/2");
    assertThat(financingHdr.getFinancingStep()).isEqualTo("SIGNED");

    when(agreementRepository.findByFinancingHdr_FinancingHdrCode(UUID.fromString("33333333-3333-3333-3333-333333333333"))).thenReturn(List.of());
    assertThat(service.signerDocList("33333333-3333-3333-3333-333333333333", "maker")).isEmpty();
    assertThatThrownBy(() -> service.signerDocList("bad-uuid", "maker"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid financingHdrCode format");
  }

  @Test
  void checkSignerDanasaktiAndCheckSendDocumentReturnExpectedStates() {
    when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn(null);
    assertThat(service.checkSignerDanasakti(FINANCING_HDR_CODE.toString(), "maker")).isEmpty();

    when(agreementFileSigningRepository.findByAgreementCode("AGR001")).thenReturn(List.of());
    assertThat(service.checkSendDocument(FINANCING_HDR_CODE.toString(), "AGR001")).containsEntry("needConfirmation", false);

    when(agreementFileSigningRepository.findByAgreementCode("AGR001"))
        .thenReturn(List.of(AgreementFileSigning.builder().signProgress("1/2").build()));
    assertThat(service.checkSendDocument(FINANCING_HDR_CODE.toString(), "AGR001")).containsEntry("needConfirmation", true);

    when(agreementFileSigningRepository.findByAgreementCode("AGR001"))
        .thenReturn(List.of(AgreementFileSigning.builder().signProgress("2/2").build()));
    assertThat(service.checkSendDocument(FINANCING_HDR_CODE.toString(), "AGR001")).containsEntry("needConfirmation", false);

    when(agreementFileSigningRepository.findByAgreementCode("AGR001"))
        .thenReturn(List.of(AgreementFileSigning.builder().signProgress(null).build()));
    assertThat(service.checkSendDocument(FINANCING_HDR_CODE.toString(), "AGR001")).containsEntry("needConfirmation", false);

    when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE)).thenReturn("Debtor");
    when(debtorRepository.findByDebtorName("Debtor")).thenReturn(null);
    assertThat(service.checkSignerDanasakti(FINANCING_HDR_CODE.toString(), "maker")).isEmpty();
  }

  @Test
  void checkSendDocumentRejectsUnregisteredEsignerWithInformativeMessage() {
    org.mockito.Mockito.doThrow(new IllegalStateException(
        SigningEligibilityService.SIGNER_NOT_REGISTERED_MESSAGE
      ))
      .when(signingEligibilityService)
      .validateDebtorSigner(FINANCING_HDR_CODE.toString());

    Map<String, Object> result = service.checkSendDocument(FINANCING_HDR_CODE.toString(), "AGR001");

    assertThat(result)
      .containsEntry("canSend", false)
      .containsEntry("needConfirmation", false)
      .containsEntry("message", SigningEligibilityService.SIGNER_NOT_REGISTERED_MESSAGE);
    org.mockito.Mockito.verifyNoInteractions(agreementFileSigningRepository);
  }

  private static Map<String, Object> registrationResponse(String status) {
    return Map.of("registrationData", List.of(Map.of("registrationStatus", status)));
  }

  private static Map<String, Object> signhubRegistrationStatus(String status, String vendor) {
    return Map.of(
        "status", Map.of("code", 0),
        "registrationData", List.of(Map.of("vendor", vendor, "registrationStatus", status))
    );
  }

  private static AgreementFileSigning signing(String documentId, LocalDateTime verifDate) {
    return AgreementFileSigning.builder()
        .agreementFileId(1L)
        .agreementCode("AGR001")
        .documentId(documentId)
        .financingHdrCode(FINANCING_HDR_CODE.toString())
        .dtmCrt(LocalDateTime.of(2026, 1, 1, 0, 0))
        .verifDate(verifDate)
        .build();
  }

  private static DebtorDto debtorDto() {
    return DebtorDto.builder()
        .debtorName("Debtor")
        .karyawanName("Signer One")
        .jabatan("Director")
        .identityNo("KTP001")
        .email("signer@example.com")
        .noTelp("08123")
        .tempatLahir("Jakarta")
        .tanggalLahir("1990-01-01")
        .jenisKelamin("M")
        .alamat("Jl. Test")
        .rt("001")
        .rw("002")
        .kodePos("12345")
        .kelurahan("Kel")
        .kecamatan("Kec")
        .kota("Jakarta")
        .isActive(true)
        .emailDebtor("debtor@example.com")
        .financingHdrCode(FINANCING_HDR_CODE.toString())
        .build();
  }

  private static Debtor debtor(Long id) {
    Debtor debtor = Debtor.builder()
        .debtorId(id)
        .debtorName("Debtor")
        .karyawanName("Signer One")
        .jabatan("Director")
        .identityNo("KTP001")
        .email("signer@example.com")
        .noTelp("08123")
        .tempatLahir("Jakarta")
        .tanggalLahir("1990-01-01")
        .jenisKelamin("M")
        .alamat("Jl. Test")
        .rt("001")
        .rw("002")
        .kodePos("12345")
        .kelurahan("Kel")
        .kecamatan("Kec")
        .kota("Jakarta")
        .isActive(true)
        .signerStatus("active")
        .signhubStatus("active")
        .emailDebtor("debtor@example.com")
        .financingHdrCode(FINANCING_HDR_CODE.toString())
        .build();
    return debtor;
  }

  private static FinancingHdr financingHdr() {
    Customer customer = new Customer();
    customer.setCustCode(CUSTOMER_CODE);
    customer.setCustNo("CUST001");
    Bouwheer bouwheer = Bouwheer.builder().bouwheerName("Bouwheer").build();
    FinancingHdr financingHdr = new FinancingHdr();
    financingHdr.setFinancingHdrCode(FINANCING_HDR_CODE);
    financingHdr.setCustomer(customer);
    financingHdr.setBouwheer(bouwheer);
    return financingHdr;
  }

  private static Agreement agreement() {
    Cwr cwr = Cwr.builder()
        .cwrCode("CWR001")
        .customer(financingHdr().getCustomer())
        .build();
    return Agreement.builder()
        .agreementCode("AGR001")
        .financingHdr(financingHdr())
        .cwr(cwr)
        .build();
  }

  private static ExternalApiResponse externalSignerResponse(String name) {
    ExternalApiResponse response = new ExternalApiResponse();
    response.setStatusCode("200");
    response.setMessage("Success");
    ExternalApiResponse.SignerData signer = new ExternalApiResponse.SignerData();
    signer.setCwrSignerId(1);
    signer.setCwrCustId(2);
    signer.setSignerType("CUST");
    signer.setSignerName(name);
    signer.setSignerPosition("Director");
    response.setReturnObject(List.of(signer));
    return response;
  }

  private static PersonDto person(String... namesAndPositions) {
    PersonDto dto = new PersonDto();
    java.util.ArrayList<PersonDto.Signer> signers = new java.util.ArrayList<>();
    for (int i = 0; i < namesAndPositions.length; i += 2) {
      PersonDto.Signer signer = new PersonDto.Signer();
      signer.setSignerName(namesAndPositions[i]);
      signer.setSignerPosition(namesAndPositions[i + 1]);
      signers.add(signer);
    }
    dto.setSigners(signers);
    return dto;
  }
}
