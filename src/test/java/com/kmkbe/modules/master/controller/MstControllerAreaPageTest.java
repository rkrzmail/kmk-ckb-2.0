package com.kmkbe.modules.master.controller;

import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.modules.confinsr3.model.response.GetZipCodeResponse;
import com.kmkbe.modules.confinsr3.service.ConfinsR3Service;
import com.kmkbe.modules.master.request.AreaPageRequest;
import com.kmkbe.modules.master.service.MasterService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class MstControllerAreaPageTest {
  @Test
  void legacyAreaPageParametersBindWithoutOptionalSortAndSearch() throws Exception {
    ConfinsR3Service confinsService = mock(ConfinsR3Service.class);
    when(confinsService.pageZipcode(any())).thenReturn(new BaseResponseBuilder<>(true, 200, "Success",
      PaginationResult.<GetZipCodeResponse>builder()
        .currentPage(1).totalPage(0).totalData(0L).list(List.of()).build()));
    MockMvc mvc = MockMvcBuilders.standaloneSetup(new MstController(mock(MasterService.class), confinsService))
      .build();

    mvc.perform(get("/api/v1/mst/areas/page")
        .param("pageNo", "1")
        .param("pageSize", "10")
        .param("criteria[0].propName", "RZ.CITY")
        .param("criteria[0].value", "Jakarta"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.currentPage").value(1))
      .andExpect(jsonPath("$.data.totalData").value(0))
      .andExpect(jsonPath("$.data.list").isArray());

    ArgumentCaptor<AreaPageRequest> captor = ArgumentCaptor.forClass(AreaPageRequest.class);
    verify(confinsService).pageZipcode(captor.capture());
    assertThat(captor.getValue().getPageNo()).isEqualTo(1);
    assertThat(captor.getValue().getPageSize()).isEqualTo(10);
    assertThat(captor.getValue().getCriteria()).hasSize(1);
    assertThat(captor.getValue().getCriteria().getFirst().getPropName()).isEqualTo("RZ.CITY");
    assertThat(captor.getValue().getCriteria().getFirst().getValue()).isEqualTo("Jakarta");
  }
}
