package com.kmkbe.core.service;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class BaseRemoteService {
    public static final String BASE_URL_MST = "https://6mn45m67ybarmii47vg4cc22240ltnmj.lambda-url.ap-southeast-1.on.aws/v1";

    @Value("${csul.confins.fou.v1}")
    public String confinsFouBaseUrl;

    @Value("${csul.confins.mou.v1}")
    public String confinsMouBaseUrl;

    @Value("${csul.confins.adinskey}")
    public String adInsKey;

    @Value("${security.api.key}")
    public String apiKey;

    public BaseRemoteService() {
    }

    public BaseRemoteService(
            @Value("${csul.confins.fou.v1}")
            String confinsFouBaseUrl,
            @Value("${csul.confins.mou.v1}")
            String confinsMouBaseUrl,
            @Value("${csul.confins.adinskey}")
            String adInsKey
    ) {
        this.confinsFouBaseUrl = confinsFouBaseUrl;
        this.confinsMouBaseUrl = confinsMouBaseUrl;
        this.adInsKey = adInsKey;
    }

    public String CustObj_GetListKeyValueActiveByCode() {
        return confinsFouBaseUrl + "/CustObj/GetObjectByKeyAndValue";
    }

    public String RefMaster_GetListKeyValueActiveByCode() {
        return confinsFouBaseUrl + "/RefMaster/GetListKeyValueActiveByCode";
    }

    public String RefMaster_GetListActiveRefMasterWithMappingCodeAll() {
        return confinsFouBaseUrl + "/RefMaster/GetListActiveRefMasterWithMappingCodeAll";
    }

    public String Mou_Generic_GetPagingObjectBySQL() {
        return confinsMouBaseUrl + "/Generic/GetPagingObjectBySQL";
    }

    public String Los_Generic_GetPagingObjectBySQL() {
        return confinsMouBaseUrl + "/Generic/GetPagingObjectBySQL";
    }

    public String Fou_Generic_GetPagingObjectBySQL() {
        return confinsFouBaseUrl + "/Generic/GetPagingObjectBySQL";
    }

    public HttpHeaders adInsKeyHeaders() {
        return keyHeaders("AdInsKey", adInsKey);
    }

    public HttpHeaders apiKeyHeaders() {
        return keyHeaders("ApiKey", apiKey);
    }

    public HttpHeaders keyHeaders(String keyName, String keyValue) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(keyName, keyValue);
        return headers;
    }


    public <T> T performRequest(
            RestTemplate restTemplate,
            HttpHeaders headers,
            Object args,
            HttpMethod method,
            String url
    ) throws JsonProcessingException {
        try {
            final HttpEntity<String> requestArgs = new HttpEntity<>(
                    ObjectUtils.jsonToStr(args),
                    headers
            );

            final ResponseEntity<T> response = restTemplate.exchange(
                    url,
                    method,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("performRequest: {}", e.getMessage());
            throw e;
        }
    }


    public enum RefMasterTypeCode {
        TipePerusahaan("COMPANY_TYPE"),
        ModelDebitur("CUST_MODEL"),
        TipeID("ID_TYPE_COMPANY"),
        StatusKepemilikan("BUILDING_OWNERSHIP_COMPANY"),
        StatusPernikahan("MARITAL_STAT"),
        JenisKelamin("GENDER");

        @JsonValue
        public final String value;

        RefMasterTypeCode(String value) {
            this.value = value;
        }

        /*@Override
        public String toString() {
            return value;
        }*/
    }

    public enum GenericTypeCode {
        ZipCode
    }

}
