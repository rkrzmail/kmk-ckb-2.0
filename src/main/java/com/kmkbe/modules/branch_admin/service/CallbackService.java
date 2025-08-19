package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.entity.Debtor;
import com.kmkbe.core.domain.repository.DebtorRepository;
import com.kmkbe.modules.branch_admin.request.CallbackRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallbackService {
    private final DebtorRepository debtorRepository;

    @Transactional
    public void processCallback(CallbackRequest request) {
        switch (request.getCallbackType()) {
            case "ACTIVATION_COMPLETE":
                handleActivationCallback(request);
                break;
            case "DOCUMENT_SIGN_COMPLETE":
                handleDocumentSignComplete(request);
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

        log.info("Updated debtor status to 'exist' for email: {}", email);
    }

    private void handleDocumentSignComplete(CallbackRequest request) {
        String documentId = request.getData().getDocumentId();
        log.info("Document {} has been fully signed", documentId);
    }
}