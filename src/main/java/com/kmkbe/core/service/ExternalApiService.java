package com.kmkbe.core.service;

import com.kmkbe.core.domain.dto.AppRequest;
import com.kmkbe.core.domain.dto.AppResponse;
import com.kmkbe.core.domain.dto.FinancialDataRequest;
import com.kmkbe.core.domain.dto.FinancialDataResponse;
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

    @Value("${csul.confins.los.v1}")
    private String apiUrl;

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
                    apiUrl + "/Application/GetAppByAppNo",
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

}
