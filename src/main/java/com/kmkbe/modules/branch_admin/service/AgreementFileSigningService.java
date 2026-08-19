package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.constant.AuditAction;
import com.kmkbe.core.domain.dto.AgreementFileSigningDto;
import com.kmkbe.core.domain.entity.AgreementFileSigning;
import com.kmkbe.core.domain.entity.Debtor;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.NotifDebtor;
import com.kmkbe.core.domain.mapper.AgreementFileSigningMapper;
import com.kmkbe.core.domain.repository.AgreementFileSigningRepository;
import com.kmkbe.core.domain.repository.DebtorRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.NotifDebtorRepository;
import com.kmkbe.modules.common.service.AuditTrailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgreementFileSigningService {

    private final AgreementFileSigningRepository agreementFileSigningRepository;
    private final FinancingHdrRepository financingHdrRepository;
    private final DebtorRepository debtorRepository;
    private final NotifDebtorRepository notifDebtorRepository;
    private final AuditTrailService auditTrailService;

    private final AgreementFileSigningMapper agreementFileSigningMapper = AgreementFileSigningMapper.INSTANCE;

    public AgreementFileSigningDto saveSigningResult(
            String agreementCode,
            String documentId,
            String username,
            String financingHdrCode
    ) {
        String debtorName = financingHdrRepository.findDebtorNameByFinancingHdrCode(UUID.fromString(financingHdrCode));
        List<Debtor> signerList = debtorRepository.findActiveSignerByDebtorName(debtorName);

        Debtor debtor;
        if (signerList.isEmpty()) {
            throw new RuntimeException("Tidak ada data signer active dari financingHdr = " + financingHdrCode);
        } else {
            debtor = signerList.get(0);
        }

        List<AgreementFileSigning> existingList = agreementFileSigningRepository.findByAgreementCode(agreementCode);

        AgreementFileSigning entity;
        if (!existingList.isEmpty()) {
            entity = existingList.get(0);
            AgreementFileSigningAuditData before = toAuditData(entity);

            if (existingList.size() > 1) {
                agreementFileSigningRepository.deleteAll(existingList.subList(1, existingList.size()));
            }
            entity.setStamp("Not Signed");
            entity.setSigner(debtor.getKaryawanName());
            entity.setEmailSigner(debtor.getEmail());
            entity.setIdentityNo(debtor.getIdentityNo());
            entity.setDocumentId(documentId);
            entity.setFinancingHdrCode(financingHdrCode);
            entity.setUsrUpd(username);
            entity.setDtmUpd(LocalDateTime.now());

            AgreementFileSigning saveDoc = agreementFileSigningRepository.save(entity);
            auditTrailService.record("AGREEMENT_SIGNING", AuditAction.UPDATE, "AgreementFileSigning", saveDoc.getAgreementFileId(), before, toAuditData(saveDoc));
            updateFinancingStep(financingHdrCode);
            createSigningNotification(financingHdrCode, username, debtor);
            return agreementFileSigningMapper.entityToDto(saveDoc);
        } else {
            entity = AgreementFileSigning.builder()
                    .agreementCode(agreementCode)
                    .fileTypeCode("E_SIGN_DOC")
                    .fileName("PERJANJIAN_1A_" + agreementCode + ".pdf")
                    .usrCrt(username)
                    .dtmCrt(LocalDateTime.now())
                    .build();
        }

        entity.setStamp("Not Signed");
        entity.setSigner(debtor.getKaryawanName());
        entity.setEmailSigner(debtor.getEmail());
        entity.setIdentityNo(debtor.getIdentityNo());
        entity.setDocumentId(documentId);
        entity.setFinancingHdrCode(financingHdrCode);
        entity.setUsrUpd(username);
        entity.setDtmUpd(LocalDateTime.now());

        AgreementFileSigning saveDoc = agreementFileSigningRepository.save(entity);
        auditTrailService.record("AGREEMENT_SIGNING", AuditAction.CREATE, "AgreementFileSigning", saveDoc.getAgreementFileId(), null, toAuditData(saveDoc));

        updateFinancingStep(financingHdrCode);
        createSigningNotification(financingHdrCode, username, debtor);

        return agreementFileSigningMapper.entityToDto(saveDoc);
    }

    private void updateFinancingStep(String financingHdrCode) {
        financingHdrRepository.findByFinancingHdrCode(UUID.fromString(financingHdrCode))
                .ifPresent(finHdr -> {
                    FinancingStepAuditData before = toFinancingStepAuditData(finHdr);
                    finHdr.setFinancingStep("SIGNING");
                    FinancingHdr saved = financingHdrRepository.save(finHdr);
                    auditTrailService.record("AGREEMENT_SIGNING", AuditAction.UPDATE, "FinancingHdr", saved.getFinancingHdrCode(), before, toFinancingStepAuditData(saved));
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
                .dtmCrt(LocalDateTime.now())
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
