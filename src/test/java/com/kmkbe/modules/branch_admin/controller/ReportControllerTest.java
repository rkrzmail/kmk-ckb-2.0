package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.modules.branch_admin.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {
  @Mock ReportService reportService;
  @Mock CurrentUserService currentUserService;
  MockMvc mvc;

  @BeforeEach
  void setUp() {
    mvc = MockMvcBuilders.standaloneSetup(new ReportController(reportService, currentUserService))
      .setMessageConverters(new MappingJackson2HttpMessageConverter(), new org.springframework.http.converter.ByteArrayHttpMessageConverter()).build();
  }

  @Test
  void visitor() throws Exception {
    when(reportService.getVisitorReport(any())).thenReturn(PaginationResult.empty(1));
    mvc.perform(get("/api/v1/report/visitor").param("pageNo","1").param("pageSize","10"))
      .andExpect(status().isOk()).andExpect(jsonPath("$.isSuccess").value(true));
    verify(currentUserService).authenticatedInternalUser();
  }

  @Test
  void proyeksiParsesDates() throws Exception {
    when(reportService.getProyeksiReport(any(), any(), any())).thenReturn(PaginationResult.empty(1));
    mvc.perform(get("/api/v1/report/proyeksi").param("startDate","01/01/2025").param("endDate","01022025"))
      .andExpect(status().isOk());
    verify(reportService).getProyeksiReport(any(BasePaginationRequest.class), any(), any());
  }

  @Test
  void proyeksiInvalidDate() {
    ReportController c = new ReportController(reportService, currentUserService);
    assertThatThrownBy(() -> c.getlistProyeksi(new BasePaginationRequest(), "bad", null))
      .isInstanceOf(BusinessException.class).satisfies(e -> assertThat(((BusinessException)e).getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
  }

  @Test
  void summaries() throws Exception {
    when(reportService.getSummaryByBranch(any())).thenReturn(PaginationResult.empty(1));
    when(reportService.getAllReportBranchByAO(any())).thenReturn(PaginationResult.empty(1));
    when(reportService.getSummaryDetail(any())).thenReturn(PaginationResult.empty(1));
    when(reportService.getDueDateDetail(any())).thenReturn(PaginationResult.empty(1));
    mvc.perform(get("/api/v1/report/summary/branch")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/report/summary/ao")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/report/summary/detail")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/report/duedate")).andExpect(status().isOk());
  }

  @Test
  void previewOkAndErrors() throws Exception {
    when(reportService.generateReport("F","A","BM","ASM")).thenReturn(new byte[]{1,2});
    mvc.perform(get("/api/v1/report/preview/F/A/BM/ASM")).andExpect(status().isOk())
      .andExpect(header().string("Content-Type","application/pdf"));
    doThrow(new NoSuchElementException("nf")).when(reportService).generateReport("F","A","BM","ASM");
    mvc.perform(get("/api/v1/report/preview/F/A/BM/ASM")).andExpect(status().isNotFound());
    doThrow(new IllegalArgumentException("bad")).when(reportService).generateReport("F","A","BM","ASM");
    mvc.perform(get("/api/v1/report/preview/F/A/BM/ASM")).andExpect(status().isBadRequest());
    doThrow(new RuntimeException("boom")).when(reportService).generateReport("F","A","BM","ASM");
    mvc.perform(get("/api/v1/report/preview/F/A/BM/ASM")).andExpect(status().isInternalServerError());
  }

  @Test
  void downloadPdf() throws Exception {
    when(reportService.generateReport("F","A","BM","ASM")).thenReturn("pdf".getBytes());
    MockHttpServletResponse res = mvc.perform(get("/api/v1/report/download-pdf/F/A/BM/ASM")).andReturn().getResponse();
    assertThat(res.getStatus()).isEqualTo(200);
    assertThat(res.getContentType()).isEqualTo("application/pdf");
    doThrow(new RuntimeException("x")).when(reportService).generateReport("F","A","BM","ASM");
    MockHttpServletResponse err = mvc.perform(get("/api/v1/report/download-pdf/F/A/BM/ASM")).andReturn().getResponse();
    assertThat(err.getStatus()).isEqualTo(500);
  }

  @Test
  void sendDoc() throws Exception {
    when(currentUserService.internalUsername()).thenReturn("u");
    when(reportService.sendDocumentForSigning("F","A","BM","ASM","u")).thenReturn(SigningResponse.builder().success(true).build());
    mvc.perform(post("/api/v1/report/send-doc/F/A/BM/ASM")).andExpect(status().isOk());
  }
}
