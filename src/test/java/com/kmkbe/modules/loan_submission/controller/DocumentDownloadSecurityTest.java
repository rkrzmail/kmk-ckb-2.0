package com.kmkbe.modules.loan_submission.controller;

import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.AgreementFile;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.LegalFile;
import com.kmkbe.core.domain.repository.AgreementFileRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.LegalFileRepository;
import com.kmkbe.core.domain.repository.MstFileTypeRepository;
import com.kmkbe.core.middleware.JwtAuthenticationFilter;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.core.utils.UriUtils;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import com.kmkbe.modules.loan_submission.service.LegalFileService;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import com.kmkbe.modules.user.entity.MstUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.lang.reflect.Method;
import java.nio.file.AccessDeniedException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentDownloadSecurityTest {

    @Mock private AgreementFileRepository agreementFileRepository;
    @Mock private MstFileTypeRepository mstFileTypeRepository;
    @Mock private LegalFileRepository legalFileRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private LegalFileService legalFileService;
    @Mock private CustomerRemoteService customerRemoteService;
    @Mock private CustomerRepository customerRepository;
    @Mock private FinancingHdrRepository financingHdrRepository;
    @Mock private AuditTrailService auditTrailService;
    @Mock private CurrentUserService currentUserService;

    private DocumentService service;

    private final UUID ownerCode = UUID.randomUUID();
    private final UUID attackerCode = UUID.randomUUID();

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
                auditTrailService,
                currentUserService
        );
    }

    private Customer customer(UUID code) {
        return Customer.builder().custCode(code).custName("n").build();
    }

    private LegalFile legalFile(UUID owner) {
        return LegalFile.builder()
                .fileId(1L)
                .custCode(customer(owner))
                .fileName("ktp.pdf")
                .filePath("cust/loan/ktp.pdf")
                .contentType("application/pdf")
                .build();
    }

    // ---------- C10.1: whitelist ----------

    @Test
    @DisplayName("C10: endpoint download dokumen tidak lagi ada di whitelist publik filter")
    void downloadEndpointsRemovedFromPublicWhitelist() {
        assertThat(Arrays.asList(JwtAuthenticationFilter.ENDPOINTS_WHITELIST))
                .doesNotContain(
                        "/api/v1/documents/download/**",
                        "/api/v1/documents/download/loan/**",
                        "/api/v1/documents/download/agreement/**"
                );
    }

    // ---------- C10.2: controller tidak terima token via query ----------

    @Test
    @DisplayName("C10: controller tidak lagi menerima token via query param (anti log-leak)")
    void controllerHasNoTokenQueryParam() {
        for (Method m : DocumentController.class.getDeclaredMethods()) {
            if (m.getName().equals("download") || m.getName().equals("downloadAgreement")) {
                assertThat(Arrays.stream(m.getParameters())
                        .map(p -> p.getName().toLowerCase()))
                        .doesNotContain("token");
            }
        }
    }

    @Test
    @DisplayName("C10: fileUrl yang digenerate tidak menyematkan token di query string")
    void generatedFileUrlContainsNoToken() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/loan-submissions/x");
        request.addHeader("Authorization", "Bearer secret-jwt");
        String url = UriUtils.fileUlr(request, 123, UriUtils.DocType.loan);
        assertThat(url).contains("/api/v1/documents/download/loan/123");
        assertThat(url.toLowerCase()).doesNotContain("token");
        assertThat(url).doesNotContain("secret-jwt");
    }

    // ---------- C10.3: loan doc ownership ----------

    @Test
    @DisplayName("C10: pemilik dokumen boleh mengunduh dokumen loan miliknya")
    void ownerCanDownloadOwnLoanDocument() throws Exception {
        when(legalFileService.findByFileId(1L)).thenReturn(legalFile(ownerCode));
        when(currentUserService.internalUser()).thenThrow(new SignatureException());
        when(currentUserService.customer()).thenReturn(customer(ownerCode));
        when(fileStorageService.downloadUploadFile(any(), eq("cust/loan/ktp.pdf"), eq("ktp.pdf"), anyString()))
                .thenReturn(ResponseEntity.ok(new ByteArrayResource("x".getBytes())));

        var response = service.documentByLegalFileId(new MockHttpServletRequest(), 1L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(fileStorageService).downloadUploadFile(any(), eq("cust/loan/ktp.pdf"), eq("ktp.pdf"), anyString());
    }

    @Test
    @DisplayName("C10: IDOR diblokir - nasabah A tidak bisa mengunduh dokumen nasabah B")
    void attackerCannotDownloadOthersLoanDocument() throws Exception {
        when(legalFileService.findByFileId(1L)).thenReturn(legalFile(ownerCode));
        when(currentUserService.internalUser()).thenThrow(new SignatureException());
        when(currentUserService.customer()).thenReturn(customer(attackerCode));

        assertThatThrownBy(() -> service.documentByLegalFileId(new MockHttpServletRequest(), 1L))
                .isInstanceOf(AccessDeniedException.class);

        verify(fileStorageService, never()).downloadUploadFile(any(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("C10: tanpa login (SignatureException) download ditolak 401")
    void unauthenticatedDownloadRejected() throws Exception {
        when(legalFileService.findByFileId(1L)).thenReturn(legalFile(ownerCode));
        when(currentUserService.internalUser()).thenThrow(new SignatureException());
        when(currentUserService.customer()).thenThrow(new SignatureException("You are not authorized"));

        assertThatThrownBy(() -> service.documentByLegalFileId(new MockHttpServletRequest(), 1L))
                .isInstanceOf(SignatureException.class);

        verify(fileStorageService, never()).downloadUploadFile(any(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("C10: user internal berwenang boleh mengunduh dokumen milik nasabah manapun")
    void internalUserCanDownloadAnyLoanDocument() throws Exception {
        when(legalFileService.findByFileId(1L)).thenReturn(legalFile(ownerCode));
        when(currentUserService.internalUser()).thenReturn(mock(MstUser.class));
        when(fileStorageService.downloadUploadFile(any(), eq("cust/loan/ktp.pdf"), eq("ktp.pdf"), anyString()))
                .thenReturn(ResponseEntity.ok(new ByteArrayResource("x".getBytes())));

        var response = service.documentByLegalFileId(new MockHttpServletRequest(), 1L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(currentUserService, never()).customer();
    }

    // ---------- C10.4: agreement doc ownership via FinancingHdr ----------

    private AgreementFile agreementFile(UUID owner) {
        Customer cust = customer(owner);
        FinancingHdr hdr = new FinancingHdr();
        hdr.setCustomer(cust);
        Agreement agreement = new Agreement();
        agreement.setFinancingHdr(hdr);
        return AgreementFile.builder()
                .agreementFileId(7L)
                .agreement(agreement)
                .fileName("akad.pdf")
                .filePath("cust/agreement/akad.pdf")
                .contentType("application/pdf")
                .build();
    }

    @Test
    @DisplayName("C10: pemilik boleh mengunduh dokumen agreement via relasi FinancingHdr")
    void ownerCanDownloadOwnAgreementDocument() throws Exception {
        AgreementFile file = agreementFile(ownerCode);
        when(agreementFileRepository.findById(7L)).thenReturn(Optional.of(file));
        when(currentUserService.internalUser()).thenThrow(new SignatureException());
        when(currentUserService.customer()).thenReturn(customer(ownerCode));
        when(fileStorageService.downloadUploadFile(any(), eq("cust/agreement/akad.pdf"), eq("akad.pdf"), eq("")))
                .thenReturn(ResponseEntity.ok(new ByteArrayResource("x".getBytes())));

        var response = service.agreementDocByAgreementId(new MockHttpServletRequest(), 7L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    @DisplayName("C10: IDOR agreement diblokir + relasi rusak fail-closed")
    void attackerBlockedAndBrokenRelationFailClosed() throws Exception {
        // attacker vs owner
        AgreementFile file = agreementFile(ownerCode);
        when(agreementFileRepository.findById(7L)).thenReturn(Optional.of(file));
        when(currentUserService.internalUser()).thenThrow(new SignatureException());
        when(currentUserService.customer()).thenReturn(customer(attackerCode));

        assertThatThrownBy(() -> service.agreementDocByAgreementId(new MockHttpServletRequest(), 7L))
                .isInstanceOf(AccessDeniedException.class);

        // relasi agreement null -> fail closed
        AgreementFile broken = AgreementFile.builder()
                .agreementFileId(8L).agreement(null)
                .fileName("x.pdf").filePath("p/x.pdf").contentType("application/pdf").build();
        when(agreementFileRepository.findById(8L)).thenReturn(Optional.of(broken));

        assertThatThrownBy(() -> service.agreementDocByAgreementId(new MockHttpServletRequest(), 8L))
                .isInstanceOf(AccessDeniedException.class);

        verify(fileStorageService, never()).downloadUploadFile(any(), anyString(), anyString(), anyString());
    }
}
