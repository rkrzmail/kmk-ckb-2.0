package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.modules.branch_admin.service.SitTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SitTemplateControllerTest {
  @Mock SitTemplateService service;
  @Mock CurrentUserService currentUserService;
  MockMvc mvc;
  UUID code = UUID.randomUUID();

  @BeforeEach void setUp() {
    mvc = MockMvcBuilders.standaloneSetup(new SitTemplateController(service, currentUserService))
      .setMessageConverters(new MappingJackson2HttpMessageConverter(), new org.springframework.http.converter.ByteArrayHttpMessageConverter()).build();
  }

  @Test
  void agreementsAndDownload() throws Exception {
    when(service.agreementCodes(code)).thenReturn(List.of("A1"));
    when(service.download(code, "A1")).thenReturn("pdf".getBytes());
    mvc.perform(get("/api/v1/report/sit/{c}/agreements", code)).andExpect(status().isOk())
      .andExpect(jsonPath("$.data[0]").value("A1"));
    mvc.perform(get("/api/v1/report/sit/{c}/download", code).param("agreementCode","A1"))
      .andExpect(status().isOk()).andExpect(header().exists("Content-Disposition"));
    verify(currentUserService, times(2)).authenticatedInternalUser();
  }
}
