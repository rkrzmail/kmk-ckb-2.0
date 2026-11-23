package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.modules.branch_admin.service.CsulSignerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CsulSignerControllerTest {
  @Mock CsulSignerService service;
  @Mock CurrentUserService currentUserService;
  MockMvc mvc;

  @BeforeEach void setUp() {
    mvc = MockMvcBuilders.standaloneSetup(new CsulSignerController(service, currentUserService))
      .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();
  }

  @Test
  void listAndDetail() throws Exception {
    when(currentUserService.internalUsername()).thenReturn("u");
    when(service.signerCsulListPage(eq("u"), any())).thenReturn(PaginationResult.empty(1));
    when(service.detailSigner(1L)).thenReturn(new SignerCsulDto());
    mvc.perform(get("/api/v1/csul-signer/list")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/csul-signer/list/1")).andExpect(status().isOk());
  }

  @Test
  void createUpdateCheck() throws Exception {
    when(currentUserService.internalUsername()).thenReturn("u");
    SignerCsulRequest req = new SignerCsulRequest(); req.setRegistrationMessage("ok");
    when(service.createSigner(any(), eq("u"))).thenReturn(req);
    when(service.updateSigner(eq(1L), any(), eq("u"))).thenReturn(req);
    when(service.updateSignerStatus("123", "u")).thenReturn("updated");
    mvc.perform(post("/api/v1/csul-signer/signer").contentType("application/json").content("{}"))
      .andExpect(status().isOk()).andExpect(jsonPath("$.data").value("ok"));
    mvc.perform(put("/api/v1/csul-signer/signer/1").contentType("application/json").content("{}"))
      .andExpect(status().isOk());
    mvc.perform(put("/api/v1/csul-signer/check/123")).andExpect(status().isOk());
  }

  @Test
  void createErrors() throws Exception {
    when(currentUserService.internalUsername()).thenReturn("u");
    when(service.createSigner(any(), eq("u"))).thenThrow(new RuntimeException("NIK sudah terdaftar"));
    mvc.perform(post("/api/v1/csul-signer/signer").contentType("application/json").content("{}"))
      .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(409));
    when(service.createSigner(any(), eq("u"))).thenThrow(new RuntimeException("bad"));
    mvc.perform(post("/api/v1/csul-signer/signer").contentType("application/json").content("{}"))
      .andExpect(jsonPath("$.code").value(400));
    when(service.updateSigner(eq(1L), any(), eq("u"))).thenThrow(new RuntimeException("x"));
    mvc.perform(put("/api/v1/csul-signer/signer/1").contentType("application/json").content("{}"))
      .andExpect(jsonPath("$.code").value(400));
    when(service.updateSignerStatus("1", "u")).thenThrow(new RuntimeException("x"));
    mvc.perform(put("/api/v1/csul-signer/check/1")).andExpect(jsonPath("$.code").value(400));
  }

  @Test
  void grouped() throws Exception {
    when(currentUserService.internalUsername()).thenReturn("u");
    when(service.getSignersGrouped("u")).thenReturn(Map.of("a",1));
    when(service.getSignersGrouped2()).thenReturn(Map.of("b",2));
    mvc.perform(get("/api/v1/csul-signer/get-list")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/csul-signer/get-list/sign")).andExpect(status().isOk());
    verify(currentUserService).authenticatedInternalUser();
  }
}
