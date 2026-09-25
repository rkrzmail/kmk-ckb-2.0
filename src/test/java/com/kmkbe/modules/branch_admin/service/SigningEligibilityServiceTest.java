package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.entity.CsulSigner;
import com.kmkbe.core.domain.entity.Debtor;
import com.kmkbe.core.domain.repository.CsulSignerRepository;
import com.kmkbe.core.domain.repository.DebtorRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SigningEligibilityServiceTest {

  private static final UUID FINANCING_HDR_CODE =
    UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Mock private FinancingHdrRepository financingHdrRepository;
  @Mock private DebtorRepository debtorRepository;
  @Mock private CsulSignerRepository csulSignerRepository;

  private SigningEligibilityService service;

  @BeforeEach
  void setUp() {
    service = new SigningEligibilityService(
      financingHdrRepository,
      debtorRepository,
      csulSignerRepository
    );
  }

  @Test
  void validateForSigningAcceptsActiveDebtorAndRegisteredCsulSigners() {
    when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE))
      .thenReturn("Debtor");
    when(debtorRepository.findActiveSignerByDebtorName("Debtor"))
      .thenReturn(List.of(debtor("active", true)));
    when(csulSignerRepository.findByKaryawanName("BM"))
      .thenReturn(Optional.of(csulSigner("Registered", true)));
    when(csulSignerRepository.findByKaryawanName("ASM"))
      .thenReturn(Optional.of(csulSigner("active", true)));

    assertThatCode(() -> service.validateForSigning(
      FINANCING_HDR_CODE.toString(), "BM", "ASM"
    )).doesNotThrowAnyException();
  }

  @Test
  void validateDebtorSignerRejectsActiveSignerThatIsNotRegistered() {
    when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE))
      .thenReturn("Debtor");
    when(debtorRepository.findActiveSignerByDebtorName("Debtor"))
      .thenReturn(List.of(debtor("not register", true)));

    assertThatThrownBy(() -> service.validateDebtorSigner(FINANCING_HDR_CODE.toString()))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage(SigningEligibilityService.SIGNER_NOT_REGISTERED_MESSAGE);
  }

  @Test
  void validateDebtorSignerRecognizesUnregisteredSignerWhenNoConfinsActiveSignerExists() {
    when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE))
      .thenReturn("Debtor");
    when(debtorRepository.findActiveSignerByDebtorName("Debtor")).thenReturn(List.of());
    when(debtorRepository.findByDebtorName("Debtor"))
      .thenReturn(List.of(debtor(null, true)));

    assertThatThrownBy(() -> service.validateDebtorSigner(FINANCING_HDR_CODE.toString()))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage(SigningEligibilityService.SIGNER_NOT_REGISTERED_MESSAGE);
  }

  @Test
  void validateDebtorSignerReportsMissingSignerAndInvalidFinancingCode() {
    when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE))
      .thenReturn("Debtor");
    when(debtorRepository.findActiveSignerByDebtorName("Debtor")).thenReturn(null);
    when(debtorRepository.findByDebtorName("Debtor")).thenReturn(null);

    assertThatThrownBy(() -> service.validateDebtorSigner(FINANCING_HDR_CODE.toString()))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Tidak ada data signer active");
    assertThatThrownBy(() -> service.validateDebtorSigner("invalid-uuid"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Format financingHdrCode tidak valid: invalid-uuid");
  }

  @Test
  void validateForSigningRejectsUnregisteredOrMissingCsulSigner() {
    when(financingHdrRepository.findDebtorNameByFinancingHdrCode(FINANCING_HDR_CODE))
      .thenReturn("Debtor");
    when(debtorRepository.findActiveSignerByDebtorName("Debtor"))
      .thenReturn(List.of(debtor("active", true)));
    when(csulSignerRepository.findByKaryawanName("BM"))
      .thenReturn(Optional.of(csulSigner("Not Registered", true)));

    assertThatThrownBy(() -> service.validateForSigning(
      FINANCING_HDR_CODE.toString(), "BM", "ASM"
    ))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage(SigningEligibilityService.SIGNER_NOT_REGISTERED_MESSAGE);

    when(csulSignerRepository.findByKaryawanName("BM")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.validateForSigning(
      FINANCING_HDR_CODE.toString(), "BM", "ASM"
    ))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Branch Manager BM tidak ditemukan di csul_signer");
  }

  private Debtor debtor(String signhubStatus, Boolean active) {
    Debtor debtor = new Debtor();
    debtor.setSignhubStatus(signhubStatus);
    debtor.setIsActive(active);
    return debtor;
  }

  private CsulSigner csulSigner(String signhubStatus, Boolean active) {
    return CsulSigner.builder()
      .signhubStatus(signhubStatus)
      .isActive(active)
      .build();
  }
}
