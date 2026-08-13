package com.kmkbe.modules.api_sbu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.repository.ErrorLogRepository;
import com.kmkbe.core.middleware.HttpRequestResponseLogFilter;
import com.kmkbe.core.middleware.JwtAuthenticationFilter;
import com.kmkbe.core.service.JwtLoanSubmissionService;
import com.kmkbe.core.service.JwtService;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.helpers.utils.PageableUtil;
import com.kmkbe.modules.api_sbu.model.entity.ApiSbu;
import com.kmkbe.modules.api_sbu.model.request.ApiSbuRequest;
import com.kmkbe.modules.api_sbu.model.response.ApiSbuResponse;
import com.kmkbe.modules.api_sbu.model.response.PageApiSbuResponse;
import com.kmkbe.modules.api_sbu.service.ApiSbuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class ApiSbuControllerTest {

  @InjectMocks
  private ApiSbuController apiSbuController;


  private MockMvc mockMvc;

  @Mock
  private ApiSbuService apiSbuService;

  @MockBean
  private ErrorLogRepository errorLogRepository;

  @MockBean
  private HttpRequestResponseLogFilter httpRequestResponseLogFilter;
  // Test setup can be added here if needed, but @WebMvcTest handles most of it.

  @MockBean
  private JwtService jwtService;

  @MockBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @MockBean
  private JwtLoanSubmissionService jwtLoanSubmissionService;

  private Long mockID = 1l;

  @BeforeEach
  void setup() {
    mockMvc = standaloneSetup(apiSbuController).build();

  }

  @Test
  void getAllApiSbuShouldReturnAllData() throws Exception {
    // Given
    when(apiSbuService.all()).thenReturn(new BaseResponseBuilder<>(
      true,
      AppConstants.CODE_OK,
      AppConstants.PROCESS_SUCCESSFULLY,
      Collections.singletonList(ApiSbuResponse.builder()
        .sesId(mockID)
        .build())
    ));

    // When & Then
    mockMvc.perform(get("/api/v1/api-sbu")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andDo(print())
      .andReturn();
  }

  @Test
  void getPageApiSbuShouldReturnPaginatedData() throws Exception {
    // 1. Create a real Page object instead of a mock to prevent nested stubbing issues
    List<ApiSbu> content = Collections.emptyList();
    Pageable pageable = PageRequest.of(0, 10);
    Page<ApiSbu> page = new PageImpl<>(content, pageable, 0);

    // 2. Perform the stubbing safely
    when(apiSbuService.pages(any(BasePaginationRequest.class)))
      .thenReturn(new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, PageApiSbuResponse.builder()
        .content(Collections.singletonList(ApiSbuResponse.builder()
          .sesId(1L)
          .build()))
        .pagination(PageableUtil.pageToPagination(page)) // Now safe to use
        .build()
      ));

    // When & Then
    mockMvc.perform(get("/api/v1/api-sbu/pages?pageSize=10&pageNo=1&sortBy=appName&sortType=asc&searchBy=appName&searchValue=")
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andDo(print())
      .andReturn();
  }

  @Test
  void getPageApiSbuByBowheerCodeShouldReturnPaginatedData() throws Exception {
    // Given
    String bowheerCode = "TESTCODE";
    // 1. Create a real Page object instead of a mock to prevent nested stubbing issues
    List<ApiSbu> content = Collections.emptyList();
    Pageable pageable = PageRequest.of(0, 10);
    Page<ApiSbu> page = new PageImpl<>(content, pageable, 0);

    // 2. Perform the stubbing safely
    when(apiSbuService.pagesByBowheerCode(anyString(), any(BasePaginationRequest.class)))
      .thenReturn(new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, PageApiSbuResponse.builder()
        .content(Collections.singletonList(ApiSbuResponse.builder()
          .sesId(1L)
          .build()))
        .pagination(PageableUtil.pageToPagination(page)) // Now safe to use
        .build()
      ));

    // When & Then
    mockMvc.perform(get("/api/v1/api-sbu/pages/" + bowheerCode + "?pageSize=10&pageNo=1&sortBy=appName&sortType=asc&searchBy=appName&searchValue=")
        .accept(MediaType.APPLICATION_JSON_VALUE)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andDo(print())
      .andReturn();
  }

  @Test
  void postApiSbuShouldCreateData() throws Exception {
    // Given
    when(apiSbuService.create(any(ApiSbuRequest.class)))
      .thenReturn(new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, ApiSbuResponse.builder()
        .sesId(mockID)
        .build()));

    // When & Then
    mockMvc.perform(post("/api/v1/api-sbu")
        .contentType(MediaType.APPLICATION_JSON)
        .content(new ObjectMapper().writeValueAsString(ApiSbuRequest.builder()
          .bouwheerCode("TEST")
          .expiredDate(new Date())
          .sesStatus("ACTIVE")
          .appPath("/TEST")
          .appName("/TEST")
          .build())))
      .andExpect(status().isOk())
      .andDo(print())
      .andReturn();
  }

  @Test
  void putApiSbuShouldUpdateData() throws Exception {
    // Given
    when(apiSbuService.update(anyString(), any(ApiSbuRequest.class)))
      .thenReturn(new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, ApiSbuResponse.builder()
        .sesId(mockID)
        .build()));

    // When & Then
    mockMvc.perform(put("/api/v1/api-sbu/" + mockID)
        .contentType(MediaType.APPLICATION_JSON)
        .content(new ObjectMapper().writeValueAsString(ApiSbuRequest.builder()
          .bouwheerCode("TEST")
          .expiredDate(new Date())
          .sesStatus("ACTIVE")
          .appPath("/TEST")
          .appName("/TEST")
          .build())))
      .andExpect(status().isOk())
      .andDo(print())
      .andReturn();
  }

  @Test
  void getApiSbuByIdShouldFindData() throws Exception {
    // Given
    when(apiSbuService.findById(anyString()))
      .thenReturn(new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY, ApiSbuResponse.builder()
        .sesId(mockID)
        .build()));

    // When & Then
    mockMvc.perform(get("/api/v1/api-sbu/" + mockID)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andDo(print())
      .andReturn();
  }

  @Test
  void deleteApiSbuShouldDeleteData() throws Exception {
    // Given
    BaseResponse expectedResponse = new BaseResponse();

    when(apiSbuService.delete(anyString())).thenReturn(expectedResponse);

    // When & Then
    mockMvc.perform(delete("/api/v1/api-sbu/" + mockID)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andDo(print())
      .andReturn();
  }
}
