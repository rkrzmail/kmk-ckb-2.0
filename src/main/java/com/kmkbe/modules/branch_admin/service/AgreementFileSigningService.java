package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.constant.AuditAction;
import com.kmkbe.core.domain.entity.AgreementFileSigning;
import com.kmkbe.core.domain.entity.Debtor;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.NotifDebtor;
import com.kmkbe.core.domain.mapper.AgreementFileSigningMapper;
import com.kmkbe.core.domain.repository.AgreementFileSigningRepository;
import com.kmkbe.core.domain.repository.DebtorRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.NotifDebtorRepository;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.modules.common.service.AuditTrailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgreementFileSigningService {

  private static final String FILE_TYPE_E_SIGN_DOC = "E_SIGN_DOC";
  private static final String AUDIT_AGREEMENT_SIGNING = "AGREEMENT_SIGNING";
  private static final String SAVE_AUDIT_TRAIL_MESSAGE = "Save Audit Trail !";

  private final AgreementFileSigningRepository agreementFileSigningRepository;
  private final FinancingHdrRepository financingHdrRepository;
  private final DebtorRepository debtorRepository;
  private final NotifDebtorRepository notifDebtorRepository;
  private final AuditTrailService auditTrailService;
  private final CurrentUserService currentUserService;
  private static final AgreementFileSigningMapper AGREEMENT_FILE_SIGNING_MAPPER = AgreementFileSigningMapper.INSTANCE;

  public void saveSigningResult(
    String agreementCode,
    String documentId,
    String username,
    String financingHdrCode,
    String fileTypeCode
  ) {
    String debtorName = financingHdrRepository.findDebtorNameByFinancingHdrCode(UUID.fromString(financingHdrCode));
    List<Debtor> signerList = debtorRepository.findActiveSignerByDebtorName(debtorName);

    if (signerList.isEmpty()) {
      log.error("Signer list is Empty");
      throw new BusinessException(HttpStatus.NOT_FOUND, AppConstants.CODE_NOT_FOUND,"Tidak ada data signer active dari financingHdr = " + financingHdrCode);
    }

    Debtor debtor = signerList.getFirst();
    List<AgreementFileSigning> existingList = agreementFileSigningRepository.findByAgreementCode(agreementCode);
    AgreementFileSigning entity;
    if (!existingList.isEmpty()) {
      log.info("Get Signer List");
      entity = existingList.getFirst();
      AgreementFileSigningAuditData before = toAuditData(entity);
      if (existingList.size() > 1) {
        log.info("Delete Signer list");
        agreementFileSigningRepository.deleteAll(existingList.subList(1, existingList.size()));
      }

      entity.setFileTypeCode(fileTypeCode);
      entity.setStamp("signed");
      entity.setVerifDate(LocalDateTime.now(DateTimeUtils.JAKARTA_ZONE));
      entity.setSigner(debtor.getKaryawanName());
      entity.setEmailSigner(debtor.getEmail());
      entity.setIdentityNo(debtor.getIdentityNo());
      entity.setDocumentId(documentId);
      entity.setFinancingHdrCode(financingHdrCode);
      entity.setUsrUpd(username);
      entity.setDtmUpd(LocalDateTime.now(DateTimeUtils.JAKARTA_ZONE));
      AgreementFileSigning saveDoc = agreementFileSigningRepository.save(entity);

      log.info(SAVE_AUDIT_TRAIL_MESSAGE);
      auditTrailService.record(AUDIT_AGREEMENT_SIGNING, AuditAction.UPDATE, "AgreementFileSigning", saveDoc.getAgreementFileId(), before, toAuditData(saveDoc));

      log.info("Update financing Step");
      updateFinancingStep(financingHdrCode);

      if(fileTypeCode.equals(FILE_TYPE_E_SIGN_DOC)) {
        log.info("Create signing notification !");
        createSigningNotification(financingHdrCode, username, debtor);
      }
      AGREEMENT_FILE_SIGNING_MAPPER.entityToDto(saveDoc);
      return;
    } else {
      entity = AgreementFileSigning.builder()
        .agreementCode(agreementCode)
        .fileTypeCode(fileTypeCode)
        .fileName("PERJANJIAN_1A_" + agreementCode + ".pdf")
        .usrCrt(username)
        .dtmCrt(LocalDateTime.now(DateTimeUtils.JAKARTA_ZONE))
        .build();
    }

    entity.setStamp(fileTypeCode.equals(FILE_TYPE_E_SIGN_DOC) ? "Not Signed" : "signed");
    entity.setVerifDate(fileTypeCode.equals(FILE_TYPE_E_SIGN_DOC) ? null : LocalDateTime.now(DateTimeUtils.JAKARTA_ZONE));
    entity.setSigner(debtor.getKaryawanName());
    entity.setEmailSigner(debtor.getEmail());
    entity.setIdentityNo(debtor.getIdentityNo());
    entity.setDocumentId(documentId);
    entity.setFinancingHdrCode(financingHdrCode);
    entity.setUsrUpd(username);
    entity.setSignProgress(fileTypeCode.equals(FILE_TYPE_E_SIGN_DOC) ? null:"1/1");
    entity.setDtmUpd(LocalDateTime.now(DateTimeUtils.JAKARTA_ZONE));
    AgreementFileSigning saveDoc = agreementFileSigningRepository.save(entity);

    log.info(SAVE_AUDIT_TRAIL_MESSAGE);
    auditTrailService.record(AUDIT_AGREEMENT_SIGNING, AuditAction.CREATE, "AgreementFileSigning", saveDoc.getAgreementFileId(), null, toAuditData(saveDoc));

    log.info("Update financing Step");
    updateFinancingStep(financingHdrCode);

    if(fileTypeCode.equals(FILE_TYPE_E_SIGN_DOC)){
      log.info("Create signing notification !");
      createSigningNotification(financingHdrCode, username, debtor);

    }
    AGREEMENT_FILE_SIGNING_MAPPER.entityToDto(saveDoc);
  }

  private void updateFinancingStep(String financingHdrCode) {
    financingHdrRepository.findByFinancingHdrCode(UUID.fromString(financingHdrCode))
      .ifPresent(finHdr -> {
        FinancingStepAuditData before = toFinancingStepAuditData(finHdr);
        finHdr.setFinancingStep(finHdr.getFinancingStep().equals("GOLIVE")?finHdr.getFinancingStep():"SIGNING");
        finHdr.setUsrUpd(currentUserService.usernameOrDefault(AppConstants.CREATOR));
        finHdr.setDtmUpd(LocalDateTime.now(DateTimeUtils.JAKARTA_ZONE));
        FinancingHdr saved = financingHdrRepository.save(finHdr);

        log.info(SAVE_AUDIT_TRAIL_MESSAGE);
        auditTrailService.record(AUDIT_AGREEMENT_SIGNING, AuditAction.UPDATE, "FinancingHdr", saved.getFinancingHdrCode(), before, toFinancingStepAuditData(saved));

      });
  }

  private void createSigningNotification(String financingHdrCode, String username, Debtor debtor) {
    String custCode = String.valueOf(financingHdrRepository.findByFinancingHdrCode(UUID.fromString(financingHdrCode))
      .map(finHdr -> finHdr.getCustomer().getCustCode())
      .orElseThrow(() -> new RuntimeException("FinancingHdr dengan code "
        + financingHdrCode + " tidak ditemukan")));

    notifDebtorRepository.save(NotifDebtor.builder()
      .notification("Permintaan Tanda Tangan Dokumen")
      .description("Dokumen yang memerlukan tanda tangan " + debtor.getKaryawanName() + ", telah tersedia. Mohon segera mendandatangani dokumen tersebut atau menghubungi pihak terkait.")
      .financingHdrCode(financingHdrCode)
      .custCode(custCode)
      .usrCrt(username)
      .dtmCrt(LocalDateTime.now(DateTimeUtils.JAKARTA_ZONE))
      .build());
  }

  private AgreementFileSigningAuditData toAuditData(AgreementFileSigning signing) {
    if (signing == null) {
      return null;
    }

    return new AgreementFileSigningAuditData(
      signing.getAgreementFileId(),
      signing.getAgreementCode(),
      signing.getFileTypeCode(),
      signing.getFileName(),
      signing.stamp(),
      signing.getDocumentId(),
      signing.getSigner(),
      signing.getEmailSigner(),
      signing.getIdentityNo(),
      signing.getFinancingHdrCode(),
      signing.getSignProgress(),
      signing.getVerifDate()
    );
  }

  private FinancingStepAuditData toFinancingStepAuditData(FinancingHdr financingHdr) {
    return new FinancingStepAuditData(
      financingHdr.getFinancingHdrCode(),
      financingHdr.getFinancingStatus(),
      financingHdr.getFinancingStep()
    );
  }

  private record AgreementFileSigningAuditData(
    Long agreementFileId,
    String agreementCode,
    String fileTypeCode,
    String fileName,
    String stamp,
    String documentId,
    String signer,
    String emailSigner,
    String identityNo,
    String financingHdrCode,
    String signProgress,
    LocalDateTime verifDate
  ) {
  }

  private record FinancingStepAuditData(UUID financingHdrCode, String financingStatus, String financingStep) {
  }
}
