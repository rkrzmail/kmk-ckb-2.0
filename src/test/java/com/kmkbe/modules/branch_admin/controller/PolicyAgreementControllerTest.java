package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.PolicyAgreementDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.modules.branch_admin.service.PolicyAgreementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PolicyAgreementControllerTest {
  @Mock PolicyAgreementService service;
  MockMvc mvc;

  @BeforeEach void setUp() {
    mvc = MockMvcBuilders.standaloneSetup(new PolicyAgreementController() {{ 
      try { var f = PolicyAgreementController.class.getDeclaredField("policyAgreementService"); f.setAccessible(true); f.set(this, service); } catch (Exception e) { throw new RuntimeException(e); }
    }}).setMessageConverters(new MappingJackson2HttpMessageConverter()).build();
  }

  @Test
  void endpoints() throws Exception {
    PolicyAgreementDto dto = new PolicyAgreementDto();
    when(service.createPolicyAgreement(any())).thenReturn(new CommonResult<PolicyAgreementDto>().success(dto));
    when(service.getPolicyAgreementList()).thenReturn(List.of(dto));
    when(service.getPolicyAgreementById(1L)).thenReturn(new CommonResult<PolicyAgreementDto>().success(dto));
    when(service.getPolicyAgreementById(2L)).thenReturn(new CommonResult<PolicyAgreementDto>().fail(404,"nf"));
    when(service.getPolicyAgreementHistoryByCode("P")).thenReturn(new CommonResult<PolicyAgreementDto>().success(dto));
    when(service.getPolicyAgreementHistoryByCode("X")).thenReturn(new CommonResult<PolicyAgreementDto>().fail(404,"nf"));
    when(service.updatePolicyAgreement(eq(1L), any())).thenReturn(new CommonResult<PolicyAgreementDto>().success(dto));
    when(service.updatePolicyAgreement(eq(2L), any())).thenReturn(new CommonResult<PolicyAgreementDto>().fail(400,"bad"));

    mvc.perform(post("/api/v1/policy-agreements/create").contentType("application/json").content("{}")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/policy-agreements/list")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/policy-agreements/1")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/policy-agreements/2")).andExpect(status().isNotFound());
    mvc.perform(get("/api/v1/policy-agreements/code/P")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/policy-agreements/code/X")).andExpect(status().isNotFound());
    mvc.perform(put("/api/v1/policy-agreements/1").contentType("application/json").content("{}")).andExpect(status().isOk());
    mvc.perform(put("/api/v1/policy-agreements/2").contentType("application/json").content("{}")).andExpect(status().isBadRequest());
  }
}
