package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.modules.branch_admin.service.*;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.loan_submission.service.InvoiceService;
import com.kmkbe.modules.major_account.controller.DistributionSubmissionController;
import com.kmkbe.modules.major_account.service.DistributionSubmissionService;
import com.kmkbe.modules.remote.service.FinancingRemoteService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BasePaginationEndpointsTest {
  @ParameterizedTest
  @ValueSource(strings = {"distribution", "cwr", "invoice", "agreement", "assignment"})
  void bindsBasePaginationRequestAtEveryEndpoint(String endpoint) throws Exception {
    var auth = mock(CurrentUserService.class);
    var invoices = mock(InvoiceService.class);
    var financing = mock(FinancingHdrService.class);
    var cwr = mock(CwrService.class);
    var agreement = mock(AgreementService.class);
    var assignment = mock(AssignmentSubmissionService.class);
    var distribution = mock(DistributionSubmissionService.class);
    Object controller;
    String path;
    switch (endpoint) {
      case "distribution" -> {
        controller = new DistributionSubmissionController(invoices, financing, distribution, auth);
        path = "/api/v1/distribution-submission/list";
        when(distribution.submissionDistribution(any(BasePaginationRequest.class))).thenReturn(PaginationResult.empty(1));
      }
      case "agreement" -> {
        controller = new AgreementController(agreement, mock(FinancingRemoteService.class), financing,
          mock(FinancingHdrRepository.class), auth);
        path = "/api/v1/cwr/agreement/list/CWR/HEADER";
        when(agreement.list(eq("CWR"), eq("HEADER"), any(BasePaginationRequest.class))).thenReturn(PaginationResult.empty(1));
      }
      case "assignment" -> {
        controller = new AssignmentSubmissionController(assignment);
        path = "/api/v1/assignment-submission/list";
        when(assignment.assignmentList(any(), any(BasePaginationRequest.class))).thenReturn(PaginationResult.empty(1));
      }
      default -> {
        controller = new CwrController(cwr, invoices, financing, auth);
        path = endpoint.equals("cwr") ? "/api/v1/cwr/list/CUSTOMER" : "/api/v1/cwr/invoices/HEADER";
        if (endpoint.equals("cwr")) {
          when(cwr.list(eq("CUSTOMER"), any(BasePaginationRequest.class))).thenReturn(PaginationResult.empty(1));
        } else {
          when(financing.findByCode("HEADER")).thenReturn(new FinancingHdr());
          when(invoices.invoiceSubmissionByFinancingHdr(any(), any(BasePaginationRequest.class))).thenReturn(PaginationResult.empty(1));
        }
      }
    }
    MockMvcBuilders.standaloneSetup(controller).build().perform(get(path)
      .param("pageNo", "2").param("pageSize", "5").param("sortBy", "custName")
      .param("sortType", "desc").param("searchBy", "custName").param("searchValue", "Vendor"))
      .andExpect(status().isOk());
    var captor = ArgumentCaptor.forClass(BasePaginationRequest.class);
    switch (endpoint) {
      case "distribution" -> verify(distribution).submissionDistribution(captor.capture());
      case "agreement" -> verify(agreement).list(eq("CWR"), eq("HEADER"), captor.capture());
      case "assignment" -> verify(assignment).assignmentList(any(), captor.capture());
      case "cwr" -> verify(cwr).list(eq("CUSTOMER"), captor.capture());
      default -> verify(invoices).invoiceSubmissionByFinancingHdr(any(), captor.capture());
    }
    assertThat(captor.getValue().getPageNo()).isEqualTo(2);
    assertThat(captor.getValue().getPageSize()).isEqualTo(5);
    assertThat(captor.getValue().getSortBy()).isEqualTo("custName");
    assertThat(captor.getValue().getSortType()).isEqualTo("desc");
    assertThat(captor.getValue().getSearchBy()).isEqualTo("custName");
    assertThat(captor.getValue().getSearchValue()).isEqualTo("Vendor");
  }
}
