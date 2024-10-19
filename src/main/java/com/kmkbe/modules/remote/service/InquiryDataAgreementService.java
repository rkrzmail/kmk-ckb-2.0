package com.kmkbe.modules.remote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.AreaRemoteDto;
import com.kmkbe.core.domain.dto.BaseMstRemoteResponseDto;
import com.kmkbe.core.domain.dto.InputOptionsRemoteDto;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.modules.remote.request.*;
import io.netty.util.internal.StringUtil;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class InquiryDataAgreementService {
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final BaseRemoteService baseRemoteService;
    /**
     * <p>Currently is for retrieve input option with references from @ConfinsUrlService.RefMasterTypeCode</p>
     */
    public BaseMstRemoteResponseDto<List<InputOptionsRemoteDto>> refMasterInputOption(
            BaseRemoteService.RefMasterTypeCode type,
            @Nullable RefMasterRequest.ModelDebiturMappingCode modelDebitur
    ) {
        try {
            final HttpHeaders headers = baseRemoteService.adInsKeyHeaders();
            final HttpEntity<RefMasterRequest> requestArgs = new HttpEntity<>(
                    RefMasterRequest.builder()
                            .refMasterTypeCode(type)
                            .modelDebitur(modelDebitur)
                            .build(),
                    headers
            );

            final ResponseEntity<BaseMstRemoteResponseDto<List<InputOptionsRemoteDto>>> response = restTemplate.exchange(
                    modelDebitur != null
                            ? baseRemoteService.RefMaster_GetListActiveRefMasterWithMappingCodeAll()
                            : baseRemoteService.RefMaster_GetListKeyValueActiveByCode(),
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("mstRefMasterInput: {}", e.getMessage());
            throw e;
        }
    }

    public BaseMstRemoteResponseDto<List<AreaRemoteDto>> areaQuery(
            Integer pageNo,
            Integer rowPerPage,
            List<AreaRemoteRequest> requests
    ) throws JsonProcessingException {
        try {
            List<PropCriteriaGenericTypeRequest> props = requests
                    .stream()
                    .filter(request -> request.getType() != null && !StringUtil.isNullOrEmpty(request.getValue()))
                    .map(request -> PropCriteriaGenericTypeRequest.builder()
                            .propName(request.getType())
                            .value("%" + request.getValue().toUpperCase() + "%")
                            .build())
                    .toList();

            AreaCriteriaRemoteRequest criteriaRequest = AreaCriteriaRemoteRequest.builder()
                    .queryString(CriteriaGenericTypeRemoteRequest.QueryString.zipCode())
                    .criteria(props)
                    .pageNo(pageNo != null ? pageNo : 1)
                    .rowPerPage(rowPerPage != null ? rowPerPage : 10)
                    .build();

            final HttpHeaders headers = baseRemoteService.adInsKeyHeaders();
            final HttpEntity<String> requestArgs = new HttpEntity<>(
                    ObjectUtils.jsonToStr(criteriaRequest, false),
                    headers
            );

            String url = baseRemoteService.Agrmnt_GetAgrmntByAgrmntNoL();
            final ResponseEntity<BaseMstRemoteResponseDto<List<AreaRemoteDto>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("areaQuery: {}", e.getMessage());
            return null;
        }
    }


}
