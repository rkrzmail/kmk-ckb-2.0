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
public class MstRemoteService {
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

            String url = baseRemoteService.Fou_Generic_GetPagingObjectBySQL();
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
            return dummyZipCode();
        }
    }

    private BaseMstRemoteResponseDto<List<AreaRemoteDto>> dummyZipCode() throws JsonProcessingException {
        String r = "{\"Data\":[{\"AreaCode1\":\"2 X 11 ENAM LINGKUANG\",\"AreaCode2\":\"SICINCIN\",\"Zipcode\":\"25584\",\"City\":\"KABUPATEN PADANG PARIAMAN\",\"Province\":\"SUMATERA BARAT\",\"RefZipcodeId\":213025,\"IsActive\":true,\"SubZipcode\":\"\",\"PhnArea\":\"\"},{\"AreaCode1\":\"2 X 11 ENAM LINGKUANG\",\"AreaCode2\":\"LUBUK PANDAN\",\"Zipcode\":\"25584\",\"City\":\"KABUPATEN PADANG PARIAMAN\",\"Province\":\"SUMATERA BARAT\",\"RefZipcodeId\":213026,\"IsActive\":true,\"SubZipcode\":\"\",\"PhnArea\":\"\"},{\"AreaCode1\":\"2 X 11 ENAM LINGKUANG\",\"AreaCode2\":\"SUNGAI ASAM\",\"Zipcode\":\"25584\",\"City\":\"KABUPATEN PADANG PARIAMAN\",\"Province\":\"SUMATERA BARAT\",\"RefZipcodeId\":213027,\"IsActive\":true,\"SubZipcode\":\"\",\"PhnArea\":\"\"},{\"AreaCode1\":\"2 X 11 KAYU TANAM\",\"AreaCode2\":\"GUGUAK\",\"Zipcode\":\"25585\",\"City\":\"KABUPATEN PADANG PARIAMAN\",\"Province\":\"SUMATERA BARAT\",\"RefZipcodeId\":213088,\"IsActive\":true,\"SubZipcode\":\"\",\"PhnArea\":\"\"},{\"AreaCode1\":\"2 X 11 KAYU TANAM\",\"AreaCode2\":\"KAYU TANAM\",\"Zipcode\":\"25585\",\"City\":\"KABUPATEN PADANG PARIAMAN\",\"Province\":\"SUMATERA BARAT\",\"RefZipcodeId\":213087,\"IsActive\":true,\"SubZipcode\":\"\",\"PhnArea\":\"\"},{\"AreaCode1\":\"2 X 11 KAYU TANAM\",\"AreaCode2\":\"ANDURIANG\",\"Zipcode\":\"25585\",\"City\":\"KABUPATEN PADANG PARIAMAN\",\"Province\":\"SUMATERA BARAT\",\"RefZipcodeId\":213089,\"IsActive\":true,\"SubZipcode\":\"\",\"PhnArea\":\"\"},{\"AreaCode1\":\"2 X 11 KAYU TANAM\",\"AreaCode2\":\"KAPALO HILALANG\",\"Zipcode\":\"25585\",\"City\":\"KABUPATEN PADANG PARIAMAN\",\"Province\":\"SUMATERA BARAT\",\"RefZipcodeId\":213090,\"IsActive\":true,\"SubZipcode\":\"\",\"PhnArea\":\"\"},{\"AreaCode1\":\"ABAB\",\"AreaCode2\":\"PRAMBATAN\",\"Zipcode\":\"31315\",\"City\":\"KABUPATEN PENUKAL ABAB LEMATANG ILIR\",\"Province\":\"SUMATERA SELATAN\",\"RefZipcodeId\":220205,\"IsActive\":true,\"SubZipcode\":\"\",\"PhnArea\":\"\"},{\"AreaCode1\":\"ABAB\",\"AreaCode2\":\"TANJUNG KURUNG\",\"Zipcode\":\"31315\",\"City\":\"KABUPATEN PENUKAL ABAB LEMATANG ILIR\",\"Province\":\"SUMATERA SELATAN\",\"RefZipcodeId\":220203,\"IsActive\":true,\"SubZipcode\":\"\",\"PhnArea\":\"\"},{\"AreaCode1\":\"ABAB\",\"AreaCode2\":\"PENGABUAN\",\"Zipcode\":\"31315\",\"City\":\"KABUPATEN PENUKAL ABAB LEMATANG ILIR\",\"Province\":\"SUMATERA SELATAN\",\"RefZipcodeId\":220204,\"IsActive\":true,\"SubZipcode\":\"\",\"PhnArea\":\"\"}],\"Count\":83744,\"HeaderObj\":{\"ResponseTime\":\"68 ms\",\"StatusCode\":\"200\",\"Message\":\"Success\",\"ErrorMessages\":null},\"StatusCode\":\"200\",\"Message\":\"Success\",\"ErrorMessages\":null,\"RowVersion\":null}";
        return objectMapper.readValue(r, new TypeReference<>() {
        });
    }
}
