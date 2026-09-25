package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.entity.AgreementFileSigning;
import com.kmkbe.core.domain.entity.Debtor;
import com.kmkbe.core.domain.repository.AgreementFileSigningRepository;
import com.kmkbe.core.domain.repository.DebtorRepository;
import com.kmkbe.modules.branch_admin.request.CallbackRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CallbackServiceTest {

    @Mock
    private DebtorRepository debtorRepository;

    @Mock
    private AgreementFileSigningRepository agreementFileSigningRepository;

    private CallbackService service;

    @BeforeEach
    void setUp() {
        service = new CallbackService(debtorRepository, agreementFileSigningRepository);
    }

    @Test
    void processCallbackActivatesDebtorWhenActivationIsComplete() {
        Debtor debtor = Debtor.builder().email("debtor@example.com").build();
        when(debtorRepository.findByEmail("debtor@example.com")).thenReturn(Optional.of(debtor));

        service.processCallback(callback("ACTIVATION_COMPLETE", "debtor@example.com", null, null));

        assertThat(debtor.getSignhubStatus()).isEqualTo("active");
        verify(debtorRepository).save(debtor);
    }

    @Test
    void processCallbackThrowsWhenActivationDebtorIsMissing() {
        when(debtorRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processCallback(callback("ACTIVATION_COMPLETE", "missing@example.com", null, null)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Debtor not found with email: missing@example.com");
    }

    @Test
    void processCallbackMarksDocumentSignedWhenDocumentSignIsComplete() {
        AgreementFileSigning signing = AgreementFileSigning.builder().documentId("DOC001").build();
        when(agreementFileSigningRepository.findByDocumentId("DOC001")).thenReturn(Optional.of(signing));

        service.processCallback(callback("DOCUMENT_SIGN_COMPLETE", null, "DOC001", null));

        assertThat(signing.stamp()).isEqualTo("signed");
        verify(agreementFileSigningRepository).save(signing);
    }

    @Test
    void processCallbackDoesNothingWhenDocumentSignIsCompleteButDocumentIsMissing() {
        when(agreementFileSigningRepository.findByDocumentId("DOC001")).thenReturn(Optional.empty());

        service.processCallback(callback("DOCUMENT_SIGN_COMPLETE", null, "DOC001", null));
    }

    @Test
    void processCallbackMarksAllDocumentsSignedWhenAllDocumentsSignComplete() {
        AgreementFileSigning first = AgreementFileSigning.builder().agreementCode("AGR001").build();
        AgreementFileSigning second = AgreementFileSigning.builder().agreementCode("AGR001").build();
        List<AgreementFileSigning> files = List.of(first, second);
        when(agreementFileSigningRepository.findByAgreementCode("AGR001")).thenReturn(files);

        service.processCallback(callback("ALL_DOCUMENT_SIGN_COMPLETE", null, null, "AGR001"));

        assertThat(first.stamp()).isEqualTo("signed");
        assertThat(second.stamp()).isEqualTo("signed");
        verify(agreementFileSigningRepository).saveAll(files);
    }

    @Test
    void processCallbackReturnsWhenAllDocumentsSignCompleteButFilesAreEmpty() {
        when(agreementFileSigningRepository.findByAgreementCode("AGR001")).thenReturn(List.of());

        service.processCallback(callback("ALL_DOCUMENT_SIGN_COMPLETE", null, null, "AGR001"));
    }

    @Test
    void processCallbackMarksSignerDocumentSignedWhenSigningComplete() {
        AgreementFileSigning signing = AgreementFileSigning.builder()
                .documentId("DOC001")
                .emailSigner("debtor@example.com")
                .build();
        when(agreementFileSigningRepository.findByDocumentIdAndEmailSigner("DOC001", "debtor@example.com"))
                .thenReturn(Optional.of(signing));

        service.processCallback(callback("SIGNING_COMPLETE", "debtor@example.com", "DOC001", null));

        assertThat(signing.stamp()).isEqualTo("signed");
        verify(agreementFileSigningRepository).save(signing);
    }

    @Test
    void processCallbackDoesNothingWhenSigningCompleteButSignerDocumentIsMissing() {
        when(agreementFileSigningRepository.findByDocumentIdAndEmailSigner("DOC001", "debtor@example.com"))
                .thenReturn(Optional.empty());

        service.processCallback(callback("SIGNING_COMPLETE", "debtor@example.com", "DOC001", null));
    }

    @Test
    void processCallbackThrowsWhenCallbackTypeIsUnknown() {
        CallbackRequest request = callback("UNKNOWN", null, null, null);

        assertThatThrownBy(() -> service.processCallback(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown callback type: UNKNOWN");
        verifyNoInteractions(debtorRepository, agreementFileSigningRepository);
    }

    private static CallbackRequest callback(String type, String email, String documentId, String refNo) {
        CallbackRequest request = new CallbackRequest();
        request.setCallbackType(type);
        CallbackRequest.CallbackData data = new CallbackRequest.CallbackData();
        data.setEmail(email);
        data.setDocumentId(documentId);
        data.setRefNo(refNo);
        request.setData(data);
        return request;
    }
}
