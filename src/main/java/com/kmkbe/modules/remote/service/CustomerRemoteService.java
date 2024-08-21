package com.kmkbe.modules.remote.service;

import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.modules.remote.dto.CustomerRemoteDto;
import com.kmkbe.modules.remote.request.ExistingCustomerRequest;
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
}
