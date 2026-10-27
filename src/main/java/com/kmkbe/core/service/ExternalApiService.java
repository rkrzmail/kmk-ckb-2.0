package com.kmkbe.core.service;

import com.kmkbe.core.domain.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.kmkbe.feign.client.ConfinsR3FeignClient;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalApiService {
    private final RestTemplate restTemplate;

    private final ConfinsR3FeignClient confinsR3FeignClient;

    private String xapiKey = "YiByHB@CSUL_DEV";

    public AppResponse getAppByAppNo(String applicationCode) {
        try {
            AppRequest request = new AppRequest();
            request.setTrxNo(applicationCode);
            request.setRequestDateTime(LocalDate.now().toString());

            AppResponse response = confinsR3FeignClient.getAppByAppNo(request);
            if (response == null || response.getHeaderObj() == null) {
                throw new RuntimeException("Invalid API response structure");
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Confins API: " + e.getMessage());
        }
    }

    public FinancialDataResponse getFinancialData(String agreementCode) {
        try {
            FinancialDataRequest request = new FinancialDataRequest();
            request.setTrxNo(agreementCode);
            request.setRequestDateTime(LocalDate.now().toString());

            FinancialDataResponse response = confinsR3FeignClient.getFinancialData(request);

            if (response == null ||
                    response.getHeader() == null ||
                    !"200".equals(response.getHeader().getStatusCode())) {
                throw new RuntimeException("Invalid financial data response");
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get financial data: " + e.getMessage());
        }
    }

    public AppFactoringResponse getAppFactoringData(Integer appId) {
        try {
            AppFactoringRequest request = new AppFactoringRequest();
            request.setId(appId);
            request.setRequestDateTime(LocalDate.now().toString());

            AppFactoringResponse response = confinsR3FeignClient.getAppFactoringData(request);

            if (response == null ||
                    response.getHeader() == null ||
                    !"200".equals(response.getHeader().getStatusCode())) {
                throw new RuntimeException("Invalid response from AppFctr API");
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get AppFctr data for AppId: " + appId, e);
        }
    }

    public RekDebiturResponse getRekDebitur(String applicationCode) {
        try {
            RekDebiturRequest request = new RekDebiturRequest();
            request.setTrxNo(applicationCode);
            request.setRequestDateTime(LocalDate.now().toString());

            RekDebiturResponse response = confinsR3FeignClient.getRekDebitur(request);
            if (response == null || response.getHeader() == null) {
                throw new RuntimeException("Invalid API response structure");
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Confins API: " + e.getMessage());
        }
    }

    public CwrBwhrResponse getCwrBwhr(String CwrCode) {
        try {
            CwrBwhrRequest request = new CwrBwhrRequest();
            request.setTrxNo(CwrCode);
            request.setRequestDateTime(LocalDate.now().toString());

            CwrBwhrResponse response = confinsR3FeignClient.getCwrBwhr(request);
            if (response == null || response.getHeader() == null) {
                throw new RuntimeException("Invalid API response structure");
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Confins API: " + e.getMessage());
        }
    }

    public CwrListBwhrResponse getListCwrBwhr(String CwrCode, String CwrBouwheerCustNo) {
        try {
            CwrListBwhrRequest request = new CwrListBwhrRequest();
            request.setCwrNo(CwrCode);
            request.setCwrBouwheerCustNo(CwrBouwheerCustNo);
            request.setRequestDateTime(LocalDate.now().toString());

            CwrListBwhrResponse response = confinsR3FeignClient.getListCwrBwhr(request);
            if (response == null || response.getHeader() == null) {
                throw new RuntimeException("Invalid API response structure");
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Confins API: " + e.getMessage());
        }
    }

    public SignerApiResponse getKaryawan(String CwrCode, String CustNo) {
        try {
            SignerRequestDto request = new SignerRequestDto();
            request.setCwrNo(CwrCode);
            request.setCustNo(CustNo);
            request.setRequestDateTime(LocalDate.now().toString());

            SignerApiResponse response = confinsR3FeignClient.getKaryawan(request);
            if (response == null || response.getHeader() == null) {
                throw new RuntimeException("Invalid API response structure");
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Confins API: " + e.getMessage());
        }
    }

    public ExternalSigningResponse callEsignApi(ExternalSigningRequest request) {
        String esignUrl = "https://gdkwebserver.ad-ins.com/adimobile/demo/esign/services/external/document/sendDocumentSigning";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", xapiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ExternalSigningRequest> httpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<ExternalSigningResponse> response = restTemplate.exchange(
                esignUrl,
                HttpMethod.POST,
                httpEntity,
                ExternalSigningResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to call e-sign API: " + response.getStatusCode());
        }

        return response.getBody();
    }

}
