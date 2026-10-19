package com.kmkbe.modules.loan_submission.controller;

import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.exception.IllegalApiKeyException;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.modules.loan_submission.service.FinancingDtlService;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.loan_submission.service.FinancingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancingControllerSecurityTest {

    @Mock private FinancingHdrService financingHdrService;
    @Mock private FinancingService financingService;
    @Mock private FinancingDtlService financingDtlService;
    @Mock private CurrentUserService currentUserService;

    private FinancingController controller;
    private final String validApiKey = "valid-secret-api-key-998877";

    @BeforeEach
    void setUp() {
        controller = new FinancingController(
                financingHdrService,
                financingService,
                financingDtlService,
                currentUserService
        );
        ReflectionTestUtils.setField(controller, "expectedApiKey", validApiKey);
    }

    @Test
    @DisplayName("Hardcoded backdoor '123' must be rejected with IllegalApiKeyException on /approvals/status")
    void backdoor123RejectedOnApprovalsStatus() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("apiKey", "123");

        assertThatThrownBy(() -> controller.updateApproval(request))
                .isInstanceOf(IllegalApiKeyException.class);

        verify(financingService, never()).recallApprovalStatus();
    }

    @Test
    @DisplayName("Hardcoded backdoor '123' must be rejected with IllegalApiKeyException on /sch/cwr/status")
    void backdoor123RejectedOnSchCwrStatus() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("apiKey", "123");

        assertThatThrownBy(() -> controller.schCWR(request))
                .isInstanceOf(IllegalApiKeyException.class);

        verify(financingService, never()).recallCWRStatus();
    }

    @Test
    @DisplayName("Valid configured API key via header ApiKey succeeds on /approvals/status")
    void validApiKeyViaHeaderSucceedsOnApprovalsStatus() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("ApiKey", validApiKey);

        CommonResult<Object> result = controller.updateApproval(request);

        assertThat(result.isSuccess()).isTrue();
        verify(financingService).recallApprovalStatus();
    }

    @Test
    @DisplayName("Valid configured API key via header API-Key succeeds on /sch/cwr/status")
    void validApiKeyViaAlternateHeaderSucceedsOnSchCwrStatus() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("API-Key", validApiKey);

        CommonResult<Object> result = controller.schCWR(request);

        assertThat(result.isSuccess()).isTrue();
        verify(financingService).recallCWRStatus();
    }

    @Test
    @DisplayName("Missing API key entirely throws IllegalApiKeyException")
    void missingApiKeyThrowsException() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThatThrownBy(() -> controller.updateApproval(request))
                .isInstanceOf(IllegalApiKeyException.class);

        verify(financingService, never()).recallApprovalStatus();
    }

    @Test
    @DisplayName("InvoicePaid endpoint rejects hardcoded string or wrong key and requires expectedApiKey")
    void invoicePaidRequiresConfiguredApiKey() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("ApiKey", "wrong-key");

        var paidRequest = new com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest();

        CommonResult<Object> result = controller.invoicePaid(request, paidRequest);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(500);
        verify(financingHdrService, never()).paidFinancing(any());
    }
}
