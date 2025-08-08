package com.kmkbe.core.service;

import com.kmkbe.core.domain.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class ExternalApiService {
    private final RestTemplate restTemplate;

//    @Value("${csul.confins.los.v1}")
//    private String losUrl;

    @Value("${csul.confins.los.getRekening}")
    public String getRekening;

    @Value("${csul.confins.los.getAppNo}")
    public String getAppNo;

    @Value("${csul.confins.corelos.v1}")
    private String coreLosUrl;

    @Value("${csul.confins.adinskey}")
    private String apiKey;

    public AppResponse getAppByAppNo(String applicationCode) {
        try {
            AppRequest request = new AppRequest();
            request.setTrxNo(applicationCode);
            request.setRequestDateTime(LocalDate.now().toString());

            HttpHeaders headers = new HttpHeaders();
            headers.set("AdInsKey", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            ResponseEntity<AppResponse> response = restTemplate.exchange(
                    getAppNo,
                    HttpMethod.POST,
                    new HttpEntity<>(request, headers),
                    AppResponse.class
            );
            if (response.getBody() == null || response.getBody().getHeaderObj() == null) {
                throw new RuntimeException("Invalid API response structure");
            }

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Confins API: " + e.getMessage());
        }
    }

    public FinancialDataResponse getFinancialData(String agreementCode) {
        try {
            // 1. Prepare Request
            FinancialDataRequest request = new FinancialDataRequest();
            request.setTrxNo(agreementCode);
            request.setRequestDateTime(LocalDate.now().toString());

            // 2. Set Headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("AdInsKey", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 3. Call API
            ResponseEntity<FinancialDataResponse> response = restTemplate.exchange(
                    coreLosUrl + "/AgrmntFinData/GetFinancialDataByAgrmntNoForView",
                    HttpMethod.POST,
                    new HttpEntity<>(request, headers),
                    FinancialDataResponse.class
            );

            // 4. Validate Response
            if (response.getBody() == null ||
                    response.getBody().getHeader() == null ||
                    !"200".equals(response.getBody().getHeader().getStatusCode())) {
                throw new RuntimeException("Invalid financial data response");
            }

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get financial data: " + e.getMessage());
        }
    }

    public AppFactoringResponse getAppFactoringData(Integer appId) {
        try {
            // 1. Prepare Request
            AppFactoringRequest request = new AppFactoringRequest();
            request.setId(appId); // AppId dari API pertama
            request.setRequestDateTime(LocalDate.now().toString());

            // 2. Set Headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("AdInsKey", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 3. Call API
            ResponseEntity<AppFactoringResponse> response = restTemplate.exchange(
                    coreLosUrl + "/AppFctr/GetAppFctrByAppId",
                    HttpMethod.POST,
                    new HttpEntity<>(request, headers),
                    AppFactoringResponse.class
            );

            // 4. Validate Response
            if (response.getStatusCode() != HttpStatus.OK ||
                    response.getBody() == null ||
                    !"200".equals(response.getBody().getHeader().getStatusCode())) {
                throw new RuntimeException("Invalid response from AppFctr API");
            }

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get AppFctr data for AppId: " + appId, e);
        }
    }

    public RekDebiturResponse getRekDebitur(String applicationCode) {
        try {
            RekDebiturRequest request = new RekDebiturRequest();
            request.setTrxNo(applicationCode);
            request.setRequestDateTime(LocalDate.now().toString());

            HttpHeaders headers = new HttpHeaders();
            headers.set("AdInsKey", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            ResponseEntity<RekDebiturResponse> response = restTemplate.exchange(
                    getRekening,
                    HttpMethod.POST,
                    new HttpEntity<>(request, headers),
                    RekDebiturResponse.class
            );
            if (response.getBody() == null || response.getBody().getHeader() == null) {
                throw new RuntimeException("Invalid API response structure");
            }

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Confins API: " + e.getMessage());
        }
    }

}
