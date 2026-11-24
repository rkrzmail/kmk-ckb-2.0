package com.kmkbe.modules.remote.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.BaseMstRemoteResponseDto;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.feign.client.ConfinsR3FeignClient;
import com.kmkbe.modules.remote.request.AreaRemoteRequest;
import com.kmkbe.modules.remote.request.PropCriteriaGenericTypeRequest;
import com.kmkbe.modules.remote.request.RefMasterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RemoteMstServicesCoverageTest {
  @Mock RestTemplate restTemplate;
  @Mock BaseRemoteService baseRemoteService;
  @Mock ConfinsR3FeignClient confinsClient;

  @BeforeEach
  void setUp() {
    doReturn(new org.springframework.http.HttpHeaders()).when(baseRemoteService).adInsKeyHeaders();
    doReturn("http://mst").when(baseRemoteService).RefMaster_GetListKeyValueActiveByCode();
    doReturn("http://mst/mapping").when(baseRemoteService).RefMaster_GetListActiveRefMasterWithMappingCodeAll();
    doReturn("http://mst/area").when(baseRemoteService).Agrmnt_GetAgrmntByAgrmntNoL();
  }

  @Test
  void inquiryDataAgreementBranches() throws Exception {
    InquiryDataAgreementService service = new InquiryDataAgreementService(new ObjectMapper(), restTemplate,
      baseRemoteService);
    BaseMstRemoteResponseDto response = mock(BaseMstRemoteResponseDto.class);
    doReturn(ResponseEntity.ok(response)).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));

    assertThat(service.refMasterInputOption(BaseRemoteService.RefMasterTypeCode.TipeID, null)).isSameAs(response);
    assertThat(service.refMasterInputOption(BaseRemoteService.RefMasterTypeCode.ModelDebitur,
      RefMasterRequest.ModelDebiturMappingCode.COMPANY)).isSameAs(response);
    doThrow(new IllegalStateException("boom")).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThatThrownBy(() -> service.refMasterInputOption(BaseRemoteService.RefMasterTypeCode.TipeID, null))
      .isInstanceOf(IllegalStateException.class);

    List<AreaRemoteRequest> requests = List.of(
      AreaRemoteRequest.builder().type(PropCriteriaGenericTypeRequest.AreaPropName.zipCode).value("123").build(),
      AreaRemoteRequest.builder().type(null).value("ignored").build(),
      AreaRemoteRequest.builder().type(PropCriteriaGenericTypeRequest.AreaPropName.kota).value(null).build());
    doReturn(ResponseEntity.ok(response)).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThat(service.areaQuery(null, null, requests)).isSameAs(response);
    assertThat(service.areaQuery(2, 20, requests)).isSameAs(response);
    doThrow(new IllegalStateException("boom")).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThat(service.areaQuery(1, 10, requests)).isNull();
  }

  @Test
  void mstRemoteBranches() throws Exception {
    MstRemoteService service = new MstRemoteService(new ObjectMapper(), confinsClient, restTemplate, baseRemoteService);
    BaseMstRemoteResponseDto response = mock(BaseMstRemoteResponseDto.class);
    doReturn(ResponseEntity.ok(response)).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));

    assertThat(service.refMasterInputOption(BaseRemoteService.RefMasterTypeCode.TipeID, null)).isSameAs(response);
    assertThat(service.refMasterInputOption(BaseRemoteService.RefMasterTypeCode.ModelDebitur,
      RefMasterRequest.ModelDebiturMappingCode.PERSONAL)).isSameAs(response);
    doThrow(new IllegalStateException("boom")).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThatThrownBy(() -> service.refMasterInputOption(BaseRemoteService.RefMasterTypeCode.TipeID, null))
      .isInstanceOf(IllegalStateException.class);

    List<AreaRemoteRequest> requests = List.of(
      AreaRemoteRequest.builder().type(PropCriteriaGenericTypeRequest.AreaPropName.zipCode).value("123").build(),
      AreaRemoteRequest.builder().type(null).value("ignored").build(),
      AreaRemoteRequest.builder().type(PropCriteriaGenericTypeRequest.AreaPropName.kota).value(null).build());
    doReturn(response).when(confinsClient).areaQuery(any());
    assertThat(service.areaQuery(null, null, requests)).isSameAs(response);
    doThrow(new IllegalStateException("boom")).when(confinsClient).areaQuery(any());
    assertThat(service.areaQuery(1, 10, requests)).isNull();
    assertThat(service.dummyZipCode()).isNotNull();
  }
}
