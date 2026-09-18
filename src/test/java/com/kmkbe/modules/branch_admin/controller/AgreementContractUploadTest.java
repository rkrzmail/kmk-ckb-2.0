package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.entity.Agreement;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.ErrorLogRepository;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.core.callback.ExceptionAdvice;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.exception.GlobalExceptionHandler;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.branch_admin.service.AgreementService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.remote.service.FinancingRemoteService;
import com.kmkbe.modules.user.entity.MstUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AgreementContractUploadTest {
  private static final UUID FINANCING_CODE = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private final AgreementService agreementService = mock(AgreementService.class);
  private final FinancingHdrService financingHdrService = mock(FinancingHdrService.class);
  private final FinancingHdrRepository financingHdrRepository = mock(FinancingHdrRepository.class);
  private final CurrentUserService currentUserService = mock(CurrentUserService.class);
  private final FinancingRemoteService financingRemoteService = mock(FinancingRemoteService.class);
  private MockMvc mockMvc;
  private FinancingHdr financingHdr;

  @BeforeEach
  void setUp() {
    var controller = new AgreementController(agreementService, financingRemoteService,
      financingHdrService, financingHdrRepository, currentUserService);
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

    mockMvc.perform(multipart("/api/v1/cwr/agreement/upload/contract")
        .file(new MockMultipartFile("file", "contract.pdf", "application/pdf", new byte[] {1}))
        .param("financingHdrCode", FINANCING_CODE.toString()))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.code").value(409))
      .andExpect(jsonPath("$.message").value("Data invoice pengajuan tidak lengkap. Kontrak belum dapat diunggah. Hubungi administrator."));

    verify(agreementService).validateInvoicesForContractUpload(FINANCING_CODE);
    verify(agreementService, never()).findByFinancingHdr(any());
    verify(agreementService, never()).upload(any(), any(), any(), any());
    verify(financingHdrRepository, never()).save(any());
    verify(agreementService, never()).sendBouwheerPaymentNotification(any());
    verify(agreementService, never()).sendDebtorDisbursementNotification(any());
  }

  @Test
  void validInvoicesStillUploadAndSendNotifications() throws Exception {
    var agreement = new Agreement();
    agreement.setAgreementCode("AGR-TEST");
    var bouwheer = new Bouwheer();
    bouwheer.setBouwheerCode(UUID.fromString("22222222-2222-2222-2222-222222222222"));
    financingHdr.setBouwheer(bouwheer);
    financingHdr.setCustomer(new Customer());
    when(agreementService.findByFinancingHdr(financingHdr)).thenReturn(agreement);
    when(currentUserService.internalUser()).thenReturn(mock(MstUser.class));

    mockMvc.perform(multipart("/api/v1/cwr/agreement/upload/contract")
        .file(new MockMultipartFile("file", "contract.pdf", "application/pdf", new byte[] {1}))
        .param("financingHdrCode", FINANCING_CODE.toString()))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.isSuccess").value(true));

    var order = inOrder(agreementService, financingHdrRepository);
    order.verify(agreementService).validateInvoicesForContractUpload(FINANCING_CODE);
    order.verify(agreementService).upload(any(), any(), eq("AGR-TEST"), eq(bouwheer.getBouwheerCode().toString()));
    order.verify(financingHdrRepository).save(financingHdr);
    order.verify(agreementService).sendBouwheerPaymentNotification(financingHdr);
    order.verify(agreementService).sendDebtorDisbursementNotification(financingHdr);
    org.assertj.core.api.Assertions.assertThat(financingHdr.getFinancingStep()).isEqualTo("SIGNED");
  }
}
