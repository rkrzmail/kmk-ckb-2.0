package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.modules.branch_admin.request.CreateInquiryAgreementRequest;
import com.kmkbe.modules.branch_admin.request.CreateInquiryCwrRequest;
import com.kmkbe.modules.branch_admin.service.*;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.loan_submission.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MoreBranchAdminControllerTest {
  @Mock CurrentUserService currentUserService;
  @Mock CwrService cwrService;
  @Mock InvoiceService invoiceService;
  @Mock FinancingHdrService financingHdrService;
  @Mock AgreementService agreementService;
  @Mock SignerService signerService;
  @Mock AssignmentSubmissionService assignmentSubmissionService;
  @Mock SigningEligibilityService signingEligibilityService;
  @Mock CallbackService callbackService;

  MockMvc mvc(Object c) { return MockMvcBuilders.standaloneSetup(c).setMessageConverters(new MappingJackson2HttpMessageConverter()).build(); }

  @Test
  void cwrController() throws Exception {
    when(cwrService.list(eq("C"), any(BasePaginationRequest.class))).thenReturn(PaginationResult.empty(1));
    when(cwrService.detail("CWR","F")).thenReturn(new DetailCwrDto());
    when(cwrService.inquiryListAggr("CWR")).thenReturn(List.of("A1"));
    when(cwrService.inquiryCwr("CWR")).thenReturn(new InquiryCwrDto());
    when(financingHdrService.findByCode("F")).thenReturn(new FinancingHdr());
    when(invoiceService.invoiceSubmissionByFinancingHdr(any(FinancingHdr.class), any(BasePaginationRequest.class))).thenReturn(PaginationResult.empty(1));
    MockMvc m = mvc(new CwrController(cwrService, invoiceService, financingHdrService, currentUserService));
    m.perform(get("/api/v1/cwr/list/C")).andExpect(status().isOk());
    m.perform(get("/api/v1/cwr/detail/CWR/F")).andExpect(status().isOk());
    m.perform(get("/api/v1/cwr/list-agreement/CWR/F")).andExpect(status().isOk());
    m.perform(get("/api/v1/cwr/inquiry").param("cwrNo","CWR")).andExpect(status().isOk());
    m.perform(post("/api/v1/cwr/inquiry/create").contentType("application/json").content("{\"financingHdrCode\":\"F\",\"cwrNo\":\"CWR\"}")).andExpect(status().isOk());
    m.perform(get("/api/v1/cwr/invoices/F").param("pageNo","1")).andExpect(status().isOk());
    m.perform(get("/api/v1/cwr/invoices/NONE").param("pageNo","1")).andExpect(status().isOk());
  }

  @Test
  void agreementControllerListInquiry() throws Exception {
    when(agreementService.list(eq("CWR"), eq("F"), any(BasePaginationRequest.class))).thenReturn(PaginationResult.empty(1));
    when(agreementService.inquiryAgreementCwr("A")).thenReturn(new InquiryAgreementDto());
    MockMvc m = mvc(new AgreementController(agreementService, mock(com.kmkbe.modules.remote.service.FinancingRemoteService.class),
      financingHdrService, mock(com.kmkbe.core.domain.repository.FinancingHdrRepository.class), currentUserService, mock(AgreementFileSigningService.class)));
    m.perform(get("/api/v1/cwr/agreement/list/CWR/F")).andExpect(status().isOk());
    m.perform(get("/api/v1/cwr/agreement/inquiry").param("agreementNo","A").param("cwrCode","CWR")).andExpect(status().isOk());
    m.perform(post("/api/v1/cwr/agreement/inquiry/create").contentType("application/json").content("{\"cwrCode\":\"C\",\"financingHdrCode\":\"F\",\"agreementNo\":\"A\"}")).andExpect(status().isOk());
  }

  @Test
  void signerControllerExtra() throws Exception {
    SignerController c = new SignerController(signerService, assignmentSubmissionService, currentUserService, signingEligibilityService);
    when(signerService.assignmentListGroupByCustomer(any(), any(BasePaginationRequest.class))).thenReturn(PaginationResult.empty(1));
    when(signerService.signerPersonListPage(eq("F"), any(), any(BasePaginationRequest.class))).thenReturn(PaginationResult.empty(1));
    when(signerService.detailSigner(1L)).thenReturn(new CommonResult<DebtorDto>().success(new DebtorDto()));
    when(signerService.detailSigner(2L)).thenReturn(new CommonResult<DebtorDto>().fail(404,"nf"));
    when(signerService.signerAgreement("F")).thenReturn(List.of());
    when(signerService.signerDocListPage(eq("F"), any(), any(BasePaginationRequest.class))).thenReturn(PaginationResult.empty(1));
    when(currentUserService.internalUsername()).thenReturn("u");
    when(signerService.compareSigners("F","A")).thenReturn(SignerCheckResultDto.builder().unmatchedSigners(List.of()).build());
    when(signerService.checkSendDocument("F","A")).thenReturn(Map.of("ok",true));
    MockMvc m = mvc(c);
    m.perform(get("/api/v1/signer/list")).andExpect(status().isOk());
    m.perform(get("/api/v1/signer/person/list/F")).andExpect(status().isOk());
    m.perform(get("/api/v1/signer/person/1")).andExpect(status().isOk());
    m.perform(get("/api/v1/signer/person/2")).andExpect(status().isNotFound());
    m.perform(get("/api/v1/signer/signer-agreement/F")).andExpect(status().isOk());
    m.perform(get("/api/v1/signer/signer-doc/list/F")).andExpect(status().isOk());
    m.perform(get("/api/v1/signer/check-signer/F/A")).andExpect(status().isOk());
    m.perform(get("/api/v1/signer/check-send-document/F/A")).andExpect(status().isOk());
  }
}
