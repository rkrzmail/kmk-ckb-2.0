package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.entity.CsulSigner;
import com.kmkbe.core.domain.entity.Debtor;
import com.kmkbe.core.domain.repository.CsulSignerRepository;
import com.kmkbe.core.domain.repository.DebtorRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SigningEligibilityService {

  public static final String SIGNER_NOT_REGISTERED_MESSAGE =
    "E-Signer belum terdaftar pada sistem E-Sign. Silakan lakukan registrasi dan pastikan status E-Signer telah aktif sebelum mengunggah dokumen persetujuan.";

  private final FinancingHdrRepository financingHdrRepository;
  private final DebtorRepository debtorRepository;
  private final CsulSignerRepository csulSignerRepository;

  public void validateForSigning(
    String financingHdrCode,
    String branchManager,
    String areaSalesManager
  ) {
    validateDebtorSigner(financingHdrCode);
    validateCsulSigner(branchManager, "Branch Manager");
    validateCsulSigner(areaSalesManager, "Area Sales Manager");
  }

  public void validateDebtorSigner(String financingHdrCode) {
    UUID financingCode;
    try {
      financingCode = UUID.fromString(financingHdrCode);
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("Format financingHdrCode tidak valid: " + financingHdrCode, exception);
    }

    String debtorName = financingHdrRepository.findDebtorNameByFinancingHdrCode(financingCode);
    if (debtorName == null || debtorName.isBlank()) {
      throw new IllegalStateException("Signer Person tidak tersedia untuk financingHdrCode " + financingHdrCode);
    }

    List<Debtor> activeSigners = safeList(debtorRepository.findActiveSignerByDebtorName(debtorName));
    if (!activeSigners.isEmpty()) {
      if (activeSigners.stream().anyMatch(signer -> !isActiveSignhubStatus(signer.getSignhubStatus()))) {
        throw new IllegalStateException(SIGNER_NOT_REGISTERED_MESSAGE);
      }
      return;
    }

    List<Debtor> signers = safeList(debtorRepository.findByDebtorName(debtorName));
    boolean hasUnregisteredSigner = signers.stream()
      .filter(signer -> !Boolean.FALSE.equals(signer.getIsActive()))
      .anyMatch(signer -> !isActiveSignhubStatus(signer.getSignhubStatus()));

    if (hasUnregisteredSigner) {
      throw new IllegalStateException(SIGNER_NOT_REGISTERED_MESSAGE);
    }

    throw new IllegalStateException("Tidak ada data signer active dari financingHdr = " + financingHdrCode);
  }

  private void validateCsulSigner(String signerName, String signerRole) {
    CsulSigner signer = csulSignerRepository.findByKaryawanName(signerName)
      .orElseThrow(() -> new IllegalStateException(
        signerRole + " " + signerName + " tidak ditemukan di csul_signer"
      ));

    if (Boolean.FALSE.equals(signer.getIsActive()) || !isActiveSignhubStatus(signer.getSignhubStatus())) {
      throw new IllegalStateException(SIGNER_NOT_REGISTERED_MESSAGE);
    }
  }

  private boolean isActiveSignhubStatus(String status) {
    return "active".equalsIgnoreCase(status) || "registered".equalsIgnoreCase(status);
  }

  private List<Debtor> safeList(List<Debtor> signers) {
    return signers == null ? Collections.emptyList() : signers;
  }
}
