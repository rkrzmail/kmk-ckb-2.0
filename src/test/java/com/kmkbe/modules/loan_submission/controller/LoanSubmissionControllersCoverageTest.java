package com.kmkbe.modules.loan_submission.controller;

import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.loan_submission.request.CreateLoanApplicationRequest;
import com.kmkbe.modules.loan_submission.request.CreateSessionLoanSubmissionRequest;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaginationRequest;
import com.kmkbe.modules.loan_submission.request.SaveImportantNotesRequest;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import com.kmkbe.modules.loan_submission.service.FinancingDtlService;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.loan_submission.service.FinancingService;
import com.kmkbe.modules.loan_submission.service.LoanSubmissionService;
import com.kmkbe.modules.loan_submission.service.ProductService;
import com.kmkbe.modules.loan_submission.service.SessionLoanSubmissionService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanSubmissionControllersCoverageTest {
  @Mock LoanSubmissionService loanSubmissionService;
  @Mock DocumentService documentService;
  @Mock SessionLoanSubmissionService sessionService;
  @Mock CurrentUserService currentUserService;
  @Mock ProductService productService;
  @Mock FinancingHdrService financingHdrService;
  @Mock FinancingService financingService;
  @Mock FinancingDtlService financingDtlService;

  LoanSubmissionController loanController;
  DocumentController documentController;
  ProductController productController;
  FinancingController financingController;

  @BeforeEach
  void setUp() {
    loanController = new LoanSubmissionController(loanSubmissionService, documentService,
      sessionService, currentUserService);
    documentController = new DocumentController(documentService);
    productController = new ProductController(productService);
    financingController = new FinancingController(financingHdrService, financingService,
      financingDtlService, currentUserService);
    ReflectionTestUtils.setField(financingController, "triggerApiKey", "secret");
  }

  @Test
  void loanSubmissionControllerDelegatesAllEndpoints() throws Exception {
    Customer customer = customer();
    when(currentUserService.customer()).thenReturn(customer);
    when(currentUserService.customerOrNull()).thenReturn(customer);
    when(loanSubmissionService.fetchActiveInvoice(any(), any(), any())).thenReturn(PaginationResult.empty(1));
    when(sessionService.findLastByCust(customer)).thenReturn(Optional.empty());

    assertThat(loanController.getExternalIntegration("").isSuccess()).isTrue();
    assertThat(loanController.getExternalIntegration("token").isSuccess()).isTrue();
    assertThat(loanController.getImportantNotes().isSuccess()).isTrue();
    assertThat(loanController.saveImportantNotes(new SaveImportantNotesRequest()).isSuccess()).isTrue();
    assertThat(loanController.submitLoanSubmission(new CreateLoanApplicationRequest()).isSuccess()).isTrue();
    assertThat(loanController.getActiveInvoices("token", new BasePaginationRequest()).isSuccess()).isTrue();
    assertThat(loanController.getPercentage("B1").isSuccess()).isTrue();
    assertThat(loanController.getCalculateDisburse(null).isSuccess()).isTrue();
    assertThat(loanController.getReCalculateDisburse(mock(HttpServletRequest.class)).isSuccess()).isTrue();
    assertThat(loanController.getViewCalculateDisburse("F1", "H1").isSuccess()).isTrue();
    assertThat(loanController.getViewCalculateDisburse("F1").isSuccess()).isTrue();
    assertThat(loanController.updateSimulation(mock(HttpServletRequest.class)).isSuccess()).isTrue();
    assertThat(loanController.createSimulation(null).isSuccess()).isTrue();
    assertThat(loanController.getSimulationHistory().isSuccess()).isTrue();
    assertThat(loanController.getSimulationSession().isSuccess()).isTrue();
    CreateSessionLoanSubmissionRequest sessionRequest = new CreateSessionLoanSubmissionRequest();
    sessionRequest.setLastStep(1);
    sessionRequest.setSession(new Object());
    assertThat(loanController.createSimulationSession(sessionRequest).isSuccess()).isTrue();
    assertThat(loanController.getDocumentTemplateFinancing().isSuccess()).isTrue();
    assertThat(loanController.uploadDocument(mock(HttpServletRequest.class), mock(MultipartFile.class), "DOC001").isSuccess()).isTrue();
    assertThat(loanController.getDocumentRequirement(mock(HttpServletRequest.class),
      new PaginationRequest(), true).isSuccess()).isTrue();
    assertThat(loanController.getDocumentDebitur(mock(HttpServletRequest.class),
      new PaginationRequest(), "F1").isSuccess()).isTrue();
    assertThat(loanController.deleteDocument(1L).isSuccess()).isTrue();

    verify(documentService).delete(1L);
  }

  @Test
  void documentAndProductControllersDelegate() {
    ResponseEntity<Resource> response = ResponseEntity.ok(mock(Resource.class));
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(documentService.documentByLegalFileId(request, 1L)).thenReturn(response);
    when(documentService.agreementDocByAgreementId(request, 2L)).thenReturn(response);
    when(productService.fetchAll(any())).thenReturn(PaginationResult.empty(1));
    when(productService.fetchAllActive()).thenReturn(java.util.List.of());

    assertThat(documentController.download("1", request)).isSameAs(response);
    assertThat(documentController.downloadAgreement("2", request)).isSameAs(response);
    assertThat(productController.getAll(new PaginationRequest()).isSuccess()).isTrue();
    assertThat(productController.getAllActive().isSuccess()).isTrue();
  }

  @Test
  void financingControllerDelegatesAndValidatesApiKey() throws Exception {
    when(financingHdrService.listPaidUnpaidInvoice(any(FinancingInvoicePaginationRequest.class))).thenReturn(PaginationResult.empty(1));
    when(financingHdrService.paidInvoiceNew(any())).thenReturn(PaginationResult.empty(1));
    when(financingHdrService.listdisburseAggrement(any(FinancingInvoicePaginationRequest.class))).thenReturn(PaginationResult.empty(1));

    assertThat(financingController.invoicePaid(new FinancingInvoicePaidRequest()).isSuccess()).isTrue();
    assertThat(financingController.getInvoicePaid2(new FinancingInvoicePaginationRequest()).isSuccess()).isTrue();
    assertThat(financingController.getInvoicePaid(new PaginationRequest()).isSuccess()).isTrue();
    assertThat(financingController.getDisbursement(new FinancingInvoicePaginationRequest()).isSuccess()).isTrue();
    assertThat(financingController.updateApproval("secret").isSuccess()).isTrue();
    assertThat(financingController.schCWR("secret").isSuccess()).isTrue();

    assertThatThrownBy(() -> financingController.updateApproval("wrong")).isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> financingController.schCWR(null)).isInstanceOf(RuntimeException.class);
  }

  private static Customer customer() {
    Customer customer = new Customer();
    customer.setCustCode(UUID.randomUUID());
    return customer;
  }
}
