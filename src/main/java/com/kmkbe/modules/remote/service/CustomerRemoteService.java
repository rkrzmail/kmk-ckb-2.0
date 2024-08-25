package com.kmkbe.modules.remote.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.core.utils.ObjectUtils;
import com.kmkbe.core.domain.dto.BaseSimpleRemoteResponseDto;
import com.kmkbe.core.domain.dto.CustomerRemoteDto;
import com.kmkbe.core.domain.dto.InquiryVendorRemoteDto;
import com.kmkbe.modules.remote.request.ExistingCustomerRequest;
import com.kmkbe.modules.remote.request.InquiryRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerRemoteService {
    private final RestTemplate restTemplate;
    private final BaseRemoteService baseRemoteService;

    /**
     * <p>current usecase CustomerSignUp</p>
     * <p>Map CustNo to CustNo of @Customer</p>
     */
    public CustomerRemoteDto validateExisting(ExistingCustomerRequest params) {
        try {
            final HttpHeaders headers = baseRemoteService.adInsKeyHeaders();
            final HttpEntity<ExistingCustomerRequest> requestArgs = new HttpEntity<>(
                    params,
                    headers
            );

            final ResponseEntity<CustomerRemoteDto> response = restTemplate.exchange(
                    baseRemoteService.CustObj_GetListKeyValueActiveByCode(),
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("validateExistingCustomer, error {}", e.getMessage());
            throw e;
        }
    }

    public BaseSimpleRemoteResponseDto<InquiryVendorRemoteDto> inquiryVendor(
            String vendorCode
    ) throws JsonProcessingException {
        try {
            final HttpEntity<String> requestArgs = new HttpEntity<>(
                    ObjectUtils.jsonToStr(InquiryRequest.builder()
                            .vendorCode(vendorCode)
                            .build()),
                    baseRemoteService.apiKeyHeaders()
            );

            final ResponseEntity<BaseSimpleRemoteResponseDto<InquiryVendorRemoteDto>> response = restTemplate.exchange(
                    BaseRemoteService. BASE_URL_MST + "/vendor/byVendorId",
                    HttpMethod.POST,
                    requestArgs,
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("inquiryVendor, error {}", e.getMessage());
            throw e;
        }
    }
}
