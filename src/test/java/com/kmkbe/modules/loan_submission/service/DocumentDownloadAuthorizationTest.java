package com.kmkbe.modules.loan_submission.service;

import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.AgreementFile;
import com.kmkbe.core.domain.entity.Cwr;
import com.kmkbe.core.domain.entity.LegalFile;
import com.kmkbe.core.domain.repository.AgreementFileRepository;
import com.kmkbe.core.domain.repository.DokumentHistoryUploadRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.LegalFileRepository;
import com.kmkbe.core.domain.repository.MstFileTypeRepository;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Regresi test untuk security issue #3 (SECURITY.md):
 * download dokumen wajib melewati otorisasi ownership.
 * Customer hanya boleh dokumen miliknya; user internal (MstUser) boleh
 * mengunduh dokumen debitur; anonymous / principal asing ditolak 403.
 */
@ExtendWith(MockitoExtension.class)
class DocumentDownloadAuthorizationTest {

  @Mock private AgreementFileRepository agreementFileRepository;
  @Mock private MstFileTypeRepository mstFileTypeRepository;
  @Mock private LegalFileRepository legalFileRepository;
  @Mock private FileStorageService fileStorageService;
  @Mock private LegalFileService legalFileService;
  @Mock private CustomerRemoteService customerRemoteService;
  @Mock private CustomerRepository customerRepository;
  @Mock private FinancingHdrRepository financingHdrRepository;
  @Mock private DokumentHistoryUploadRepository dokumentHistoryUploadRepository;
  @Mock private AuditTrailService auditTrailService;
  @Mock private CurrentUserService currentUserService;

  private DocumentService service;
  private Customer owner;
  private Customer other;

  @BeforeEach
  void setUp() {
    service = new DocumentService(
      agreementFileRepository,
      mstFileTypeRepository,
      legalFileRepository,
      fileStorageService,
      legalFileService,
      customerRemoteService,
      customerRepository,
      financingHdrRepository,
      dokumentHistoryUploadRepository,
      auditTrailService,
      currentUserService
    );
    owner = Customer.builder().custCode(UUID.randomUUID()).build();
    other = Customer.builder().custCode(UUID.randomUUID()).build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateAs(Object principal) {
    var token = new UsernamePasswordAuthenticationToken(principal, null,
      List.of(new SimpleGrantedAuthority("ROLE_USER")));
    SecurityContextHolder.getContext().setAuthentication(token);
  }

  private void authenticateAnonymous() {
    var token = new AnonymousAuthenticationToken(
      "key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(token);
  }

  private LegalFile legalFileOf(Customer cust) {
    return LegalFile.builder()
      .custCode(cust)
      .fileName("doc.pdf")
      .filePath("storage/doc.pdf")
      .build();
  }

  private AgreementFile agreementFileOf(Customer cust) {
    Agreement agreement = Agreement.builder()
      .agreementCode("AGR01")
      .cwr(Cwr.builder().customer(cust).build())
      .build();
    return AgreementFile.builder()
      .agreement(agreement)
      .fileName("agreement.pdf")
      .filePath("storage/agreement.pdf")
      .build();
  }

  @Test
  void customerCanDownloadOwnLegalFile() {
    authenticateAs(owner);
    LegalFile file = legalFileOf(owner);
    when(legalFileService.findByFileId(1L)).thenReturn(file);

    assertThatCode(() -> service.documentByLegalFileId(new MockHttpServletRequest(), 1L))
      .doesNotThrowAnyException();
  }

  @Test
  void customerCannotDownloadOtherCustomerLegalFile() {
    authenticateAs(other);
    when(legalFileService.findByFileId(1L)).thenReturn(legalFileOf(owner));

    assertThatThrownBy(() -> service.documentByLegalFileId(new MockHttpServletRequest(), 1L))
      .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
  }

  @Test
  void internalUserCanDownloadAnyLegalFile() {
    authenticateAs(com.kmkbe.modules.user.entity.MstUser.builder()
      .username("Admin Jakarta 1")
      .build());
    when(legalFileService.findByFileId(1L)).thenReturn(legalFileOf(owner));

    assertThatCode(() -> service.documentByLegalFileId(new MockHttpServletRequest(), 1L))
      .doesNotThrowAnyException();
  }

  @Test
  void anonymousIsRejectedOnLegalFileDownload() {
    authenticateAnonymous();
    when(legalFileService.findByFileId(1L)).thenReturn(legalFileOf(owner));

    assertThatThrownBy(() -> service.documentByLegalFileId(new MockHttpServletRequest(), 1L))
      .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
  }

  @Test
  void noAuthenticationIsRejectedOnLegalFileDownload() {
    when(legalFileService.findByFileId(1L)).thenReturn(legalFileOf(owner));

    assertThatThrownBy(() -> service.documentByLegalFileId(new MockHttpServletRequest(), 1L))
      .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
  }

  @Test
  void customerCanDownloadOwnAgreementFile() {
    authenticateAs(owner);
    AgreementFile file = agreementFileOf(owner);
    when(agreementFileRepository.findById(2L)).thenReturn(Optional.of(file));

    assertThatCode(() -> service.agreementDocByAgreementId(new MockHttpServletRequest(), 2L))
      .doesNotThrowAnyException();
  }

  @Test
  void customerCannotDownloadOtherCustomerAgreementFile() {
    authenticateAs(other);
    when(agreementFileRepository.findById(2L)).thenReturn(Optional.of(agreementFileOf(owner)));

    assertThatThrownBy(() -> service.agreementDocByAgreementId(new MockHttpServletRequest(), 2L))
      .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
  }

  @Test
  void internalUserCanDownloadAnyAgreementFile() {
    authenticateAs(com.kmkbe.modules.user.entity.MstUser.builder()
      .username("Admin Jakarta 1")
      .build());
    when(agreementFileRepository.findById(2L)).thenReturn(Optional.of(agreementFileOf(owner)));

    assertThatCode(() -> service.agreementDocByAgreementId(new MockHttpServletRequest(), 2L))
      .doesNotThrowAnyException();
  }

  @Test
  void anonymousIsRejectedOnAgreementFileDownload() {
    authenticateAnonymous();
    when(agreementFileRepository.findById(2L)).thenReturn(Optional.of(agreementFileOf(owner)));

    assertThatThrownBy(() -> service.agreementDocByAgreementId(new MockHttpServletRequest(), 2L))
      .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
  }
}
