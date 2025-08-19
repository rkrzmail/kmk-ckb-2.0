package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.entity.AgreementFileSigning;
import com.kmkbe.core.domain.entity.Debtor;
import com.kmkbe.core.domain.repository.AgreementFileSigningRepository;
import com.kmkbe.core.domain.repository.DebtorRepository;
import com.kmkbe.modules.branch_admin.request.CallbackRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallbackService {
    private final DebtorRepository debtorRepository;
    private final AgreementFileSigningRepository agreementFileSigningRepository;

    @Transactional
    public void processCallback(CallbackRequest request) {
        switch (request.getCallbackType()) {
            case "ACTIVATION_COMPLETE":
                handleActivationCallback(request);
                break;
            case "DOCUMENT_SIGN_COMPLETE":
                handleDocumentSignComplete(request);
                break;
            case "ALL_DOCUMENT_SIGN_COMPLETE":
                handleAllDocumentSignComplete(request);
                break;
            case "SIGNING_COMPLETE":
                handleSigningComplete(request);
                break;
            default:
                throw new IllegalArgumentException("Unknown callback type: " + request.getCallbackType());
        }
    }


    private void handleActivationCallback(CallbackRequest request) {
        String email = request.getData().getEmail();
        Debtor debtor = debtorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Debtor not found with email: " + email));

        debtor.setSignhubStatus("active");
//        debtor.setSignerStatus("active");
        debtorRepository.save(debtor);
    }

    private void handleDocumentSignComplete(CallbackRequest request) {
        String documentId = request.getData().getDocumentId();
        agreementFileSigningRepository.findByDocumentId(documentId).ifPresent(agreementFileSigning -> {
            agreementFileSigning.setStamp("signed");
            agreementFileSigningRepository.save(agreementFileSigning);
        });

    }

    private void handleAllDocumentSignComplete(CallbackRequest request) {
        String agreementCode = request.getData().getRefNo();
        log.info("All documents signed for agreementCode: {}", agreementCode);

        List<AgreementFileSigning> files = agreementFileSigningRepository.findByAgreementCode(agreementCode);

        if (files.isEmpty()) {
            log.warn("No documents found for agreementCode: {}", agreementCode);
            return;
        }

        files.forEach(file -> file.setStamp("signed"));
        agreementFileSigningRepository.saveAll(files);

    }

    private void handleSigningComplete(CallbackRequest request) {
        String email = request.getData().getEmail();
        String documentId = request.getData().getDocumentId();
        agreementFileSigningRepository.findByDocumentIdAndEmailSigner(documentId, email).ifPresent(agreementFileSigning -> {
            agreementFileSigning.setStamp("signed");
            agreementFileSigningRepository.save(agreementFileSigning);
        });
    }
}