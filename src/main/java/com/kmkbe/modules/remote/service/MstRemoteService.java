package com.kmkbe.modules.remote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.modules.remote.dto.AreaRemoteDto;
import com.kmkbe.modules.remote.dto.BaseMstRemoteResponseDto;
import com.kmkbe.modules.remote.dto.InputOptionsRemoteDto;
import com.kmkbe.modules.remote.request.*;
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
public class MstRemoteService {
    private final RestTemplate restTemplate;
    private final BaseRemoteService baseRemoteService;

    /**
     * <p>Currently is for retrieve input option with references from @ConfinsUrlService.RefMasterTypeCode</p>
     */
    public BaseMstRemoteResponseDto<InputOptionsRemoteDto> refMasterInputOption(
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

            final ResponseEntity<BaseMstRemoteResponseDto<InputOptionsRemoteDto>> response = restTemplate.exchange(
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

    public BaseMstRemoteResponseDto<AreaRemoteDto> areaByCriteria(
            AreaRequest request
    ) throws JsonProcessingException {
        try {
            PropCriteriaGenericTypeRequest propCriteria = PropCriteriaGenericTypeRequest.builder()
                    .zipCodeProp(request.getArea())
                    .value(request.getValue())
                    .build();

            CriteriaGenericTypeRequest<PropCriteriaGenericTypeRequest> criteria = CriteriaGenericTypeRequest.<PropCriteriaGenericTypeRequest>builder()
                    .queryString(CriteriaGenericTypeRequest.QueryString.builder().name("lookupZipcode").build())
                    .criteria(List.of(propCriteria))
                    .build();

            AreaCriteriaRequest criteriaRequest = AreaCriteriaRequest.builder()
                    .queryString(criteria.getQueryString())
                    .criteria(List.of(criteria))
                    .build();

            final HttpHeaders headers = baseRemoteService.adInsKeyHeaders();
            final HttpEntity<String> requestArgs = new HttpEntity<>(
                    ObjectUtils.jsonToStr(criteriaRequest),
                    headers
            );

            final ResponseEntity<BaseMstRemoteResponseDto<AreaRemoteDto>> response = restTemplate.exchange(
                    baseRemoteService.Generic_GetPagingObjectBySQL(),
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("mstGenericInput: {}", e.getMessage());
            throw e;
        }
    }
}
