package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.entity.AgreementFileSigning;
import com.kmkbe.core.domain.entity.Debtor;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.NotifDebtor;
import com.kmkbe.core.domain.repository.AgreementFileSigningRepository;
import com.kmkbe.core.domain.repository.DebtorRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.NotifDebtorRepository;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.modules.customer.model.entity.Customer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class AgreementFileSigningServiceTest {

    @Test
    void saveSigningResultCreatesSigningRecordUpdatesStepAndCreatesNotification() {
        AgreementFileSigningRepository signingRepository = mock(AgreementFileSigningRepository.class);
        FinancingHdrRepository financingHdrRepository = mock(FinancingHdrRepository.class);
        DebtorRepository debtorRepository = mock(DebtorRepository.class);
        NotifDebtorRepository notifDebtorRepository = mock(NotifDebtorRepository.class);
        AuditTrailService auditTrailService = mock(AuditTrailService.class);
        AgreementFileSigningService service = new AgreementFileSigningService(
                signingRepository,
                financingHdrRepository,
                debtorRepository,
                notifDebtorRepository,
                auditTrailService
        );
        String agreementCode = "AGR001";
        String documentId = "DOC001";
        String username = "maker";
        UUID financingHdrCode = UUID.randomUUID();
        UUID custCode = UUID.randomUUID();
        FinancingHdr financingHdr = new FinancingHdr();
        Customer customer = new Customer();
        customer.setCustCode(custCode);
        financingHdr.setCustomer(customer);
        Debtor debtor = Debtor.builder()
                .karyawanName("Debtor Signer")
                .email("debtor@example.com")
                .identityNo("123456")
                .build();

        when(financingHdrRepository.findDebtorNameByFinancingHdrCode(financingHdrCode)).thenReturn("Debtor Name");
        when(debtorRepository.findActiveSignerByDebtorName("Debtor Name")).thenReturn(List.of(debtor));
        when(signingRepository.findByAgreementCode(agreementCode)).thenReturn(List.of());
        when(signingRepository.save(any(AgreementFileSigning.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(financingHdrRepository.save(any(FinancingHdr.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(financingHdrRepository.findByFinancingHdrCode(financingHdrCode)).thenReturn(Optional.of(financingHdr));

        service.saveSigningResult(agreementCode, documentId, username, financingHdrCode.toString());

        ArgumentCaptor<AgreementFileSigning> signingCaptor = ArgumentCaptor.forClass(AgreementFileSigning.class);
        verify(signingRepository).save(signingCaptor.capture());
        AgreementFileSigning savedSigning = signingCaptor.getValue();
        assertThat(savedSigning.getAgreementCode()).isEqualTo(agreementCode);
        assertThat(savedSigning.getDocumentId()).isEqualTo(documentId);
        assertThat(savedSigning.getStamp()).isEqualTo("Not Signed");
        assertThat(savedSigning.getSigner()).isEqualTo("Debtor Signer");
        assertThat(savedSigning.getEmailSigner()).isEqualTo("debtor@example.com");
        assertThat(savedSigning.getIdentityNo()).isEqualTo("123456");
        assertThat(savedSigning.getFinancingHdrCode()).isEqualTo(financingHdrCode.toString());

        assertThat(financingHdr.getFinancingStep()).isEqualTo("SIGNING");
        verify(financingHdrRepository).save(financingHdr);

        ArgumentCaptor<NotifDebtor> notificationCaptor = ArgumentCaptor.forClass(NotifDebtor.class);
        verify(notifDebtorRepository).save(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().getNotification()).isEqualTo("Permintaan Tanda Tangan Dokumen");
        assertThat(notificationCaptor.getValue().getFinancingHdrCode()).isEqualTo(financingHdrCode.toString());
        assertThat(notificationCaptor.getValue().getCustCode()).isEqualTo(custCode.toString());
        assertThat(notificationCaptor.getValue().getUsrCrt()).isEqualTo(username);
    }

    @Test
    void saveSigningResultUpdatesExistingRecordAndDeletesDuplicates() {
        AgreementFileSigningRepository signingRepository = mock(AgreementFileSigningRepository.class);
        FinancingHdrRepository financingHdrRepository = mock(FinancingHdrRepository.class);
        DebtorRepository debtorRepository = mock(DebtorRepository.class);
        NotifDebtorRepository notifDebtorRepository = mock(NotifDebtorRepository.class);
        AuditTrailService auditTrailService = mock(AuditTrailService.class);
        AgreementFileSigningService service = new AgreementFileSigningService(
                signingRepository,
                financingHdrRepository,
                debtorRepository,
                notifDebtorRepository,
                auditTrailService
        );
        String agreementCode = "AGR002";
        String documentId = "DOC002";
        String username = "maker";
        UUID financingHdrCode = UUID.randomUUID();
        UUID custCode = UUID.randomUUID();
        AgreementFileSigning existing = AgreementFileSigning.builder()
                .agreementCode(agreementCode)
                .fileName("existing.pdf")
                .build();
        AgreementFileSigning duplicate = AgreementFileSigning.builder()
                .agreementCode(agreementCode)
                .fileName("duplicate.pdf")
                .build();
        FinancingHdr financingHdr = new FinancingHdr();
        Customer customer = new Customer();
        customer.setCustCode(custCode);
        financingHdr.setCustomer(customer);
        Debtor debtor = Debtor.builder()
                .karyawanName("Existing Signer")
                .email("existing@example.com")
                .identityNo("654321")
                .build();

        when(financingHdrRepository.findDebtorNameByFinancingHdrCode(financingHdrCode)).thenReturn("Debtor Name");
        when(debtorRepository.findActiveSignerByDebtorName("Debtor Name")).thenReturn(List.of(debtor));
        when(signingRepository.findByAgreementCode(agreementCode)).thenReturn(List.of(existing, duplicate));
        when(signingRepository.save(any(AgreementFileSigning.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(financingHdrRepository.save(any(FinancingHdr.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(financingHdrRepository.findByFinancingHdrCode(financingHdrCode)).thenReturn(Optional.of(financingHdr));

        service.saveSigningResult(agreementCode, documentId, username, financingHdrCode.toString());

        verify(signingRepository).deleteAll(List.of(duplicate));
        verify(signingRepository).save(existing);
        assertThat(existing.getFileName()).isEqualTo("existing.pdf");
        assertThat(existing.getDocumentId()).isEqualTo(documentId);
        assertThat(existing.getSigner()).isEqualTo("Existing Signer");
        assertThat(financingHdr.getFinancingStep()).isEqualTo("SIGNING");
    }

    @Test
    void saveSigningResultUpdatesSingleExistingRecordWithoutDeletingDuplicates() {
        AgreementFileSigningRepository signingRepository = mock(AgreementFileSigningRepository.class);
        FinancingHdrRepository financingHdrRepository = mock(FinancingHdrRepository.class);
        DebtorRepository debtorRepository = mock(DebtorRepository.class);
        NotifDebtorRepository notifDebtorRepository = mock(NotifDebtorRepository.class);
        AuditTrailService auditTrailService = mock(AuditTrailService.class);
        AgreementFileSigningService service = new AgreementFileSigningService(
                signingRepository,
                financingHdrRepository,
                debtorRepository,
                notifDebtorRepository,
                auditTrailService
        );
        String agreementCode = "AGR002A";
        String documentId = "DOC002A";
        String username = "maker";
        UUID financingHdrCode = UUID.randomUUID();
        UUID custCode = UUID.randomUUID();
        AgreementFileSigning existing = AgreementFileSigning.builder()
                .agreementCode(agreementCode)
                .fileName("existing.pdf")
                .build();
        FinancingHdr financingHdr = new FinancingHdr();
        Customer customer = new Customer();
        customer.setCustCode(custCode);
        financingHdr.setCustomer(customer);
        Debtor debtor = Debtor.builder()
                .karyawanName("Single Signer")
                .email("single@example.com")
                .identityNo("777")
                .build();

        when(financingHdrRepository.findDebtorNameByFinancingHdrCode(financingHdrCode)).thenReturn("Debtor Name");
        when(debtorRepository.findActiveSignerByDebtorName("Debtor Name")).thenReturn(List.of(debtor));
        when(signingRepository.findByAgreementCode(agreementCode)).thenReturn(List.of(existing));
        when(signingRepository.save(any(AgreementFileSigning.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(financingHdrRepository.save(any(FinancingHdr.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(financingHdrRepository.findByFinancingHdrCode(financingHdrCode)).thenReturn(Optional.of(financingHdr));

        service.saveSigningResult(agreementCode, documentId, username, financingHdrCode.toString());

        verify(signingRepository, never()).deleteAll(any());
        assertThat(existing.getDocumentId()).isEqualTo(documentId);
    }

    @Test
    void saveSigningResultThrowsWhenNoActiveSignerExists() {
        AgreementFileSigningRepository signingRepository = mock(AgreementFileSigningRepository.class);
        FinancingHdrRepository financingHdrRepository = mock(FinancingHdrRepository.class);
        DebtorRepository debtorRepository = mock(DebtorRepository.class);
        NotifDebtorRepository notifDebtorRepository = mock(NotifDebtorRepository.class);
        AuditTrailService auditTrailService = mock(AuditTrailService.class);
        AgreementFileSigningService service = new AgreementFileSigningService(
                signingRepository,
                financingHdrRepository,
                debtorRepository,
                notifDebtorRepository,
                auditTrailService
        );
        UUID financingHdrCode = UUID.randomUUID();
        when(financingHdrRepository.findDebtorNameByFinancingHdrCode(financingHdrCode)).thenReturn("Debtor Name");
        when(debtorRepository.findActiveSignerByDebtorName("Debtor Name")).thenReturn(List.of());

        assertThatThrownBy(() -> service.saveSigningResult("AGR003", "DOC003", "maker", financingHdrCode.toString()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Tidak ada data signer active dari financingHdr = " + financingHdrCode);
        verify(signingRepository, never()).save(any(AgreementFileSigning.class));
    }

    @Test
    void saveSigningResultThrowsWhenFinancingHdrMissingForNotification() {
        AgreementFileSigningRepository signingRepository = mock(AgreementFileSigningRepository.class);
        FinancingHdrRepository financingHdrRepository = mock(FinancingHdrRepository.class);
        DebtorRepository debtorRepository = mock(DebtorRepository.class);
        NotifDebtorRepository notifDebtorRepository = mock(NotifDebtorRepository.class);
        AuditTrailService auditTrailService = mock(AuditTrailService.class);
        AgreementFileSigningService service = new AgreementFileSigningService(
                signingRepository,
                financingHdrRepository,
                debtorRepository,
                notifDebtorRepository,
                auditTrailService
        );
        UUID financingHdrCode = UUID.randomUUID();
        Debtor debtor = Debtor.builder()
                .karyawanName("Signer")
                .email("signer@example.com")
                .identityNo("123")
                .build();
        when(financingHdrRepository.findDebtorNameByFinancingHdrCode(financingHdrCode)).thenReturn("Debtor Name");
        when(debtorRepository.findActiveSignerByDebtorName("Debtor Name")).thenReturn(List.of(debtor));
        when(signingRepository.findByAgreementCode("AGR004")).thenReturn(List.of());
        when(signingRepository.save(any(AgreementFileSigning.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(financingHdrRepository.findByFinancingHdrCode(financingHdrCode)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.saveSigningResult("AGR004", "DOC004", "maker", financingHdrCode.toString()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("FinancingHdr dengan code " + financingHdrCode + " tidak ditemukan");
        verify(notifDebtorRepository, never()).save(any(NotifDebtor.class));
    }
}
