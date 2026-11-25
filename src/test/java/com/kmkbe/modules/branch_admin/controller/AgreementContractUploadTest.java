package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.callback.ExceptionAdvice;
import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.repository.ErrorLogRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.exception.GlobalExceptionHandler;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.branch_admin.service.AgreementFileSigningService;
import com.kmkbe.modules.branch_admin.service.AgreementService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.remote.service.FinancingRemoteService;
import com.kmkbe.modules.user.entity.MstUser;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AgreementContractUploadTest {
  private static final UUID FINANCING_CODE = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID BOUWHEER_CODE = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final String UPLOAD_PATH = "/api/v1/cwr/agreement/upload/contract";

  private final AgreementService agreementService = mock(AgreementService.class);
  private final FinancingHdrService financingHdrService = mock(FinancingHdrService.class);
  private final FinancingHdrRepository financingHdrRepository = mock(FinancingHdrRepository.class);
  private final CurrentUserService currentUserService = mock(CurrentUserService.class);
  private final FinancingRemoteService financingRemoteService = mock(FinancingRemoteService.class);
  private final AgreementFileSigningService agreementFileSigningService = mock(AgreementFileSigningService.class);
  private MockMvc mockMvc;
  private FinancingHdr financingHdr;

  @BeforeEach
  void setUp() {
    AgreementController controller = new AgreementController(agreementService, financingRemoteService,
      financingHdrService, financingHdrRepository, currentUserService, agreementFileSigningService);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
      .setControllerAdvice(new ExceptionAdvice(mock(ErrorLogRepository.class)), new GlobalExceptionHandler())
      .setMessageConverters(new MappingJackson2HttpMessageConverter())
      .build();
    financingHdr = new FinancingHdr();
    financingHdr.setFinancingHdrCode(FINANCING_CODE);
    financingHdr.setFinancingStatus("INPROCESS");
    financingHdr.setFinancingStep("INPROCESS");
    when(financingHdrService.findByCode(FINANCING_CODE.toString())).thenReturn(financingHdr);
  }

  @Test
  void missingInvoiceRejectsUploadBeforeAnySideEffects() throws Exception {
    doThrow(new BusinessException(HttpStatus.CONFLICT, 409,
      "Data invoice pengajuan tidak lengkap. Kontrak belum dapat diunggah. Hubungi administrator."))
      .when(agreementService).validateInvoicesForContractUpload(FINANCING_CODE);

    performUpload()
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.code").value(409))
      .andExpect(jsonPath("$.message").value("Data invoice pengajuan tidak lengkap. Kontrak belum dapat diunggah. Hubungi administrator."));

    verify(agreementService).validateInvoicesForContractUpload(FINANCING_CODE);
    verify(agreementService, never()).findByFinancingHdr(any());
    verifyNoUploadSideEffects();
  }

  @Test
  void validInvoicesStillUploadAndSendNotifications() throws Exception {
    givenExistingAgreement("AGR-TEST");
    when(currentUserService.internalUsername()).thenReturn("tester");

    performUpload()
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.isSuccess").value(true));

    var order = inOrder(agreementService, agreementFileSigningService, financingHdrRepository);
    order.verify(agreementService).validateInvoicesForContractUpload(FINANCING_CODE);
    order.verify(agreementService).upload(any(), any(), eq("AGR-TEST"), eq(BOUWHEER_CODE.toString()));
    order.verify(agreementFileSigningService).saveSigningResult("AGR-TEST", null, "tester",
      FINANCING_CODE.toString(), "SIGN_DOC");
    order.verify(financingHdrRepository).save(financingHdr);
    order.verify(agreementService).sendBouwheerPaymentNotification(financingHdr);
    order.verify(agreementService).sendDebtorDisbursementNotification(financingHdr);
    assertThat(financingHdr.getFinancingStep()).isEqualTo("SIGNED");
  }

  @Test
  void documentConflictRejectsUploadWithoutChangingStatusOrSendingEmail() throws Exception {
    givenExistingAgreement("AGR-TEST");
    when(agreementService.upload(any(), any(), eq("AGR-TEST"), eq(BOUWHEER_CODE.toString())))
      .thenThrow(new DataIntegrityViolationException("duplicate agreement_code",
        new ConstraintViolationException("duplicate key", new SQLException("duplicate key", "23505"),
          "agreement_file_agreement_code_idx")));

    performUpload()
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.message").value(allOf(
        containsString("AGR-TEST"),
        containsString("ID dokumen yang duplikat"),
        containsString("email notifikasi tidak diproses"))));

    assertThat(financingHdr.getFinancingStep()).isEqualTo("INPROCESS");
    verify(agreementFileSigningService, never()).saveSigningResult(any(), any(), any(), any(), any());
    verify(financingHdrRepository, never()).save(any());
    verify(agreementService, never()).sendBouwheerPaymentNotification(any());
    verify(agreementService, never()).sendDebtorDisbursementNotification(any());
  }

  @Test
  void unrelatedDocumentConflictDoesNotClaimDuplicateDocumentId() throws Exception {
    givenExistingAgreement("AGR-TEST");
    when(agreementService.upload(any(), any(), eq("AGR-TEST"), eq(BOUWHEER_CODE.toString())))
      .thenThrow(new DataIntegrityViolationException("another integrity error"));

    performUpload()
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.message").value(not(containsString("ID dokumen yang duplikat"))));
  }

  @Test
  void liveFinancingStatusRejectsUploadBeforeValidation() throws Exception {
    financingHdr.setFinancingStatus("LIVE");

    performUpload()
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.message").value("Status sudah GOLIVE"));

    verify(agreementService, never()).validateInvoicesForContractUpload(any());
    verify(agreementService, never()).findByFinancingHdr(any());
    verifyNoUploadSideEffects();
  }

  @Test
  void goLiveFinancingStepRejectsUploadBeforeValidation() throws Exception {
    financingHdr.setFinancingStep("GOLIVE");

    performUpload()
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.message").value("Status sudah GOLIVE"));

    verify(agreementService, never()).validateInvoicesForContractUpload(any());
    verify(agreementService, never()).findByFinancingHdr(any());
    verifyNoUploadSideEffects();
  }

  @Test
  void missingAgreementRejectsUpload() throws Exception {
    performUpload()
      .andExpect(status().isBadRequest());

    verify(agreementService).validateInvoicesForContractUpload(FINANCING_CODE);
    verify(agreementService, never()).upload(any(), any(), any(), any());
    verifyNoUploadSideEffects();
  }

  private org.springframework.test.web.servlet.ResultActions performUpload() throws Exception {
    return mockMvc.perform(multipart(UPLOAD_PATH)
      .file(new MockMultipartFile("file", "contract.pdf", "application/pdf", new byte[] {1}))
      .param("financingHdrCode", FINANCING_CODE.toString()));
  }

  private void givenExistingAgreement(String agreementCode) throws Exception {
    Agreement agreement = new Agreement();
    agreement.setAgreementCode(agreementCode);
    financingHdr.setBouwheer(Bouwheer.builder().bouwheerCode(BOUWHEER_CODE).build());
    financingHdr.setCustomer(new Customer());
    when(agreementService.findByFinancingHdr(financingHdr)).thenReturn(agreement);
    when(currentUserService.internalUser()).thenReturn(mock(MstUser.class));
  }

  private void verifyNoUploadSideEffects() throws Exception {
    verify(agreementService, never()).upload(any(), any(), any(), any());
    verify(agreementFileSigningService, never()).saveSigningResult(any(), any(), any(), any(), any());
    verify(financingHdrRepository, never()).save(any());
    verify(agreementService, never()).sendBouwheerPaymentNotification(any());
    verify(agreementService, never()).sendDebtorDisbursementNotification(any());
  }
}
