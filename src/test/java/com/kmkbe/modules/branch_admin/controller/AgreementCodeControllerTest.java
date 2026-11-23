package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.SitDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.modules.branch_admin.service.AgreementCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AgreementCodeControllerTest {
  @Test
  void getAgreements() throws Exception {
    AgreementCodeService svc = mock(AgreementCodeService.class);
    AgreementCodeController c = new AgreementCodeController();
    var f = AgreementCodeController.class.getDeclaredField("agreementCodeService"); f.setAccessible(true); f.set(c, svc);
    UUID code = UUID.randomUUID();
    SitDto dto = SitDto.builder().agreementCode("A").build();
    when(svc.getAgreementsByFinancingHdrCode(code)).thenReturn(new CommonResult<SitDto>().success(dto));
    MockMvc mvc = MockMvcBuilders.standaloneSetup(c).setMessageConverters(new MappingJackson2HttpMessageConverter()).build();
    mvc.perform(get("/api/v1/sit/agreement/{c}", code)).andExpect(status().isOk()).andExpect(jsonPath("$.isSuccess").value(true));
  }
}
