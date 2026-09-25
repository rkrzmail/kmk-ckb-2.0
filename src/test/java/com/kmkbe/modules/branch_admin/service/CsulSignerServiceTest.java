package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.dto.BaseLdapRemoteResponseDto;
import com.kmkbe.core.domain.dto.SignerCsulDto;
import com.kmkbe.core.domain.dto.SignerCsulRequest;
import com.kmkbe.core.domain.dto.SignerGroupedDto;
import com.kmkbe.core.domain.entity.CsulSigner;
import com.kmkbe.core.domain.repository.CsulSignerRepository;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.remote.service.AuthRemoteService;
import com.kmkbe.modules.remote.service.EmailAo;
import com.kmkbe.modules.user.entity.MstBranch;
import com.kmkbe.modules.user.repository.MstBranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsulSignerServiceTest {

  @Mock private RestTemplate restTemplate;
  @Mock private CsulSignerRepository csulSignerRepository;
  @Mock private MstBranchRepository mstBranchRepository;
  @Mock private AuthRemoteService authRemoteService;
  @Mock private EmailAo emailAo;
  @Mock private AuditTrailService auditTrailService;

  private CsulSignerService service;

  @BeforeEach
  void setUp() {
    service = new CsulSignerService(
        restTemplate,
        csulSignerRepository,
        mstBranchRepository,
        authRemoteService,
        emailAo,
        auditTrailService
    );
    lenient().when(csulSignerRepository.save(any(CsulSigner.class))).thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void signerCsulListMapsRepositoryRows() {
    when(csulSignerRepository.findByUsrCrt("maker"))
        .thenReturn(List.of(signer(1L, "Budi", "Branch Manager", "Registered")));

    List<SignerCsulDto> result = service.signerCsulList("maker");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getSignerId()).isEqualTo(1L);
    assertThat(result.get(0).getKaryawanName()).isEqualTo("Budi");
    assertThat(result.get(0).getSignhubStatus()).isEqualTo("Registered");
  }

  @Test
  void detailSignerReturnsDtoAndThrowsWhenMissing() {
    when(csulSignerRepository.findById(1L)).thenReturn(Optional.of(signer(1L, "Ani", "Area Sales Manager", "Registered")));
    when(csulSignerRepository.findById(99L)).thenReturn(Optional.empty());

    assertThat(service.detailSigner(1L).getEmail()).isEqualTo("ani@example.com");
    assertThatThrownBy(() -> service.detailSigner(99L))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Signer dengan ID 99 tidak ditemukan");
  }

  @Test
  void getSignersGroupedFetchesJwtCleansAdminPrefixAndCamelCasesNames() {
    BaseLdapRemoteResponseDto<String> jwt = new BaseLdapRemoteResponseDto<>();
    jwt.setData("jwt-token");
    when(authRemoteService.fetchAuthJwt()).thenReturn(jwt);
    when(mstBranchRepository.findByBranchName("Jakarta")).thenReturn(Optional.of(MstBranch.builder().branchCode("JKT").build()));
    when(emailAo.getEmailByPosition("JKT", "BM/BOH", "jwt-token"))
        .thenReturn(List.of(
            Map.of("employeeCode", "E1", "employeeName", "BUDI SANTOSO", "email", "budi@example.com"),
            mapWithNullableName()
        ));
    when(emailAo.getEmailByPosition("JKT", "RM", "jwt-token"))
        .thenReturn(List.of(Map.of("employeeCode", "E2", "employeeName", "", "email", "rm@example.com")));

    Map<String, Object> result = service.getSignersGrouped("Admin Jakarta");

    List<Map<String, Object>> branchManagers = (List<Map<String, Object>>) result.get("BranchManager");
    List<Map<String, Object>> areaSalesManagers = (List<Map<String, Object>>) result.get("AreaSalesManager");
    assertThat(branchManagers).hasSize(2);
    assertThat(branchManagers.get(0)).containsEntry("employeeName", "Budi Santoso");
    assertThat(branchManagers.get(1)).containsEntry("employeeName", null);
    assertThat(areaSalesManagers.get(0)).containsEntry("employeeName", "");
  }

  @Test
  void getSignersGroupedThrowsWhenBranchMissing() {
    BaseLdapRemoteResponseDto<String> jwt = new BaseLdapRemoteResponseDto<>();
    jwt.setData("jwt-token");
    when(authRemoteService.fetchAuthJwt()).thenReturn(jwt);
    when(mstBranchRepository.findByBranchName("Unknown")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getSignersGrouped("Unknown"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("BranchCode tidak ditemukan untuk username: Unknown");
  }

  @Test
  void createSignerRejectsDuplicateIdentityAndDuplicateName() {
    SignerCsulRequest request = request("BranchManager", "123", "Budi");
    when(csulSignerRepository.existsByIdentityNo("123")).thenReturn(true);

    assertThatThrownBy(() -> service.createSigner(request, "maker"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("NIK sudah terdaftar");

    when(csulSignerRepository.existsByIdentityNo("123")).thenReturn(false);
    when(csulSignerRepository.existsByKaryawanName("Budi")).thenReturn(true);

    assertThatThrownBy(() -> service.createSigner(request, "maker"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Signer sudah di daftarkan");
  }

  @Test
  void createSignerSavesRegisteredAndNotRegisteredStatuses() {
    SignerCsulRequest registered = request("BranchManager", "123", "Budi");
    when(csulSignerRepository.existsByIdentityNo(any())).thenReturn(false);
    when(csulSignerRepository.existsByKaryawanName(any())).thenReturn(false);
    when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(registrationData("Vida", "2")));
    when(csulSignerRepository.save(any(CsulSigner.class))).thenAnswer(invocation -> {
      CsulSigner entity = invocation.getArgument(0);
      entity.setSignerId(10L);
      return entity;
    });

    SignerCsulRequest result = service.createSigner(registered, "maker");

    assertThat(result.getJabatan()).isEqualTo("Branch Manager");
    assertThat(result.getRegistrationMessage()).isEqualTo("Signer person sudah register dan aktivasi ");

    SignerCsulRequest notRegistered = request("AreaSalesManager", "456", "Sari");
    when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(registrationData("Vida", "1")));

    SignerCsulRequest second = service.createSigner(notRegistered, "maker");

    assertThat(second.getJabatan()).isEqualTo("Area Sales Manager");
    assertThat(second.getRegistrationMessage()).isEqualTo("Harap daftarkan signer ke eSignHub terlebih dahulu");

    ArgumentCaptor<CsulSigner> signerCaptor = ArgumentCaptor.forClass(CsulSigner.class);
    verify(csulSignerRepository, times(2)).save(signerCaptor.capture());
    assertThat(signerCaptor.getAllValues()).extracting(CsulSigner::getSignhubStatus)
        .containsExactly("Registered", "Not Registered");
  }

  @Test
  void createSignerHandlesDefaultJabatanMissingVidaEmptyDataApiStatusAndNullBody() {
    when(csulSignerRepository.existsByIdentityNo(any())).thenReturn(false);
    when(csulSignerRepository.existsByKaryawanName(any())).thenReturn(false);
    when(csulSignerRepository.save(any(CsulSigner.class))).thenAnswer(invocation -> invocation.getArgument(0));

    when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("registrationData", List.of(Map.of("vendor", "Other", "registrationStatus", "2")))));
    assertThat(service.createSigner(request("Custom Title", "1", "One"), "maker").getJabatan()).isEqualTo("Custom Title");

    when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("registrationData", List.of())));
    assertThat(service.createSigner(request(null, "2", "Two"), "maker").getJabatan()).isNull();

    when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("status", Map.of("code", 8165, "message", "ignored"))));
    assertThat(service.createSigner(request("BranchManager", "3", "Three"), "maker").getRegistrationMessage())
        .isEqualTo("Harap daftarkan signer ke eSignHub terlebih dahulu");

    when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("status", Map.of("code", 500, "message", "vendor error"))));
    assertThatThrownBy(() -> service.createSigner(request("BranchManager", "4", "Four"), "maker"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("vendor error");

    when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(null));
    assertThatThrownBy(() -> service.createSigner(request("BranchManager", "5", "Five"), "maker"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("API registrasi tidak memberikan response");
  }

  @Test
  void createSignerThrowsForUnknownRegistrationStatus() {
    when(csulSignerRepository.existsByIdentityNo(any())).thenReturn(false);
    when(csulSignerRepository.existsByKaryawanName(any())).thenReturn(false);
    when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(registrationData("Vida", "9")));

    assertThatThrownBy(() -> service.createSigner(request("BranchManager", "123", "Budi"), "maker"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Status registrasi tidak dikenali: 9");
  }

  @Test
  void updateSignerUpdatesEntityForRegisteredAndNotRegistered() {
    CsulSigner entity = signer(1L, "Old", "Old", "Not Registered");
    when(csulSignerRepository.findById(1L)).thenReturn(Optional.of(entity));
    when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(registrationData("Vida", "2")));

    SignerCsulRequest result = service.updateSigner(1L, request("AreaSalesManager", "123", "New"), "updater");

    assertThat(result.getRegistrationMessage()).isEqualTo("Signer berhasil di update. Signer person sudah register dan aktivasi");
    assertThat(entity.getKaryawanName()).isEqualTo("New");
    assertThat(entity.getJabatan()).isEqualTo("Area Sales Manager");
    assertThat(entity.getSignhubStatus()).isEqualTo("Registered");
    verify(csulSignerRepository).save(entity);

    when(csulSignerRepository.findById(2L)).thenReturn(Optional.of(signer(2L, "Old2", "Old", "Registered")));
    when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(registrationData("Vida", "0")));

    SignerCsulRequest second = service.updateSigner(2L, request("BranchManager", "456", "Other"), "updater");

    assertThat(second.getRegistrationMessage()).isEqualTo("Signer berhasil di update. Harap daftarkan signer ke eSignHub terlebih dahulu");
  }

  @Test
  void updateSignerThrowsWhenMissingOrStatusUnknown() {
    when(csulSignerRepository.findById(99L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.updateSigner(99L, request("BranchManager", "123", "Budi"), "updater"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Signer dengan ID 99 tidak ditemukan");

    when(csulSignerRepository.findById(1L)).thenReturn(Optional.of(signer(1L, "Old", "Old", "Not Registered")));
    when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(registrationData("Vida", "8")));
    assertThatThrownBy(() -> service.updateSigner(1L, request("BranchManager", "123", "Budi"), "updater"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Status registrasi tidak dikenali: 8");
  }

  @Test
  void updateSignerTreatsNonVidaRegistrationAsNotRegistered() {
    CsulSigner entity = signer(1L, "Old", "Old", "Registered");
    when(csulSignerRepository.findById(1L)).thenReturn(Optional.of(entity));
    when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(registrationData("Other", "2")));

    SignerCsulRequest result = service.updateSigner(1L, request("BranchManager", "123", "Budi"), "updater");

    assertThat(result.getRegistrationMessage()).isEqualTo("Signer berhasil di update. Harap daftarkan signer ke eSignHub terlebih dahulu");
    assertThat(entity.getSignhubStatus()).isEqualTo("Not Registered");
  }

  @Test
  void getSignersGrouped2ReturnsOnlyRegisteredGroupedByKnownRoles() {
    when(csulSignerRepository.findAll()).thenReturn(List.of(
        signer(1L, "BM", "Branch Manager", "Registered"),
        signer(2L, "ASM", "Area Sales Manager", "registered"),
        signer(3L, "Inactive", "Branch Manager", "Not Registered")
    ));

    Map<String, Object> result = service.getSignersGrouped2();

    List<SignerGroupedDto> branchManagers = (List<SignerGroupedDto>) result.get("Branch Manager");
    List<SignerGroupedDto> areaSalesManagers = (List<SignerGroupedDto>) result.get("Area Sales Manager");
    assertThat(branchManagers).extracting(SignerGroupedDto::getKaryawanName).containsExactly("BM");
    assertThat(areaSalesManagers).extracting(SignerGroupedDto::getKaryawanName).containsExactly("ASM");
  }

  @Test
  void updateSignerStatusUpdatesRegisteredSignerAndReturnsMessageForNotRegistered() {
    CsulSigner signer = signer(1L, "Budi", "Branch Manager", "Not Registered");
    when(csulSignerRepository.findByIdentityNo("123")).thenReturn(Optional.of(signer));
    when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(registrationData("Vida", "2")));

    assertThat(service.updateSignerStatus("123", "updater")).isEqualTo("Signer sudah register dan aktivasi");
    assertThat(signer.getSignhubStatus()).isEqualTo("Registered");
    assertThat(signer.getUsrUpd()).isEqualTo("updater");
    verify(csulSignerRepository).save(signer);

    when(csulSignerRepository.findByIdentityNo("456")).thenReturn(Optional.of(signer(2L, "Ani", "Branch Manager", "Not Registered")));
    when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(registrationData("Vida", "1")));

    assertThat(service.updateSignerStatus("456", "updater")).isEqualTo("Signer masih belum terdaftar");
  }

  @Test
  void updateSignerStatusThrowsWhenSignerMissingAndSendsAuditPayload() {
    when(csulSignerRepository.findByIdentityNo("404")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.updateSignerStatus("404", "updater"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Signer dengan identityNo 404 tidak ditemukan");

    CsulSigner signer = signer(1L, "Budi", "Branch Manager", "Not Registered");
    when(csulSignerRepository.findByIdentityNo("123")).thenReturn(Optional.of(signer));
    when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(registrationData("Vida", "1")));

    service.updateSignerStatus("123", "auditor");

    ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
    verify(restTemplate).postForEntity(any(String.class), captor.capture(), eq(Map.class));
    Map<String, Object> body = (Map<String, Object>) captor.getValue().getBody();
    assertThat(body).containsEntry("dataType", "NIK").containsEntry("userData", "123");
    assertThat((Map<String, Object>) body.get("audit")).containsEntry("callerId", "auditor");
  }

  @Test
  void updateSignerStatusTreatsNonVidaRegistrationAsNotRegistered() {
    CsulSigner signer = signer(1L, "Budi", "Branch Manager", "Not Registered");
    when(csulSignerRepository.findByIdentityNo("123")).thenReturn(Optional.of(signer));
    when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(registrationData("Other", "2")));

    assertThat(service.updateSignerStatus("123", "updater")).isEqualTo("Signer masih belum terdaftar");
  }

  private static SignerCsulRequest request(String jabatan, String identityNo, String name) {
    return SignerCsulRequest.builder()
        .karyawanName(name)
        .jabatan(jabatan)
        .identityNo(identityNo)
        .email(name.toLowerCase() + "@example.com")
        .noTelp("08123")
        .isActive(true)
        .build();
  }

  private static CsulSigner signer(Long id, String name, String jabatan, String status) {
    return CsulSigner.builder()
        .signerId(id)
        .karyawanName(name)
        .jabatan(jabatan)
        .identityNo("ID" + id)
        .email(name.toLowerCase() + "@example.com")
        .noTelp("08123")
        .isActive(true)
        .signhubStatus(status)
        .build();
  }

  private static Map<String, Object> registrationData(String vendor, String status) {
    return Map.of("registrationData", List.of(Map.of("vendor", vendor, "registrationStatus", status)));
  }

  private static Map<String, String> mapWithNullableName() {
    Map<String, String> value = new java.util.HashMap<>();
    value.put("employeeCode", "E3");
    value.put("employeeName", null);
    value.put("email", "null-name@example.com");
    return value;
  }
}
