package com.kmkbe.modules.remote.service;

import com.kmkbe.core.service.ConfinsUrlService;
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
    private final ConfinsUrlService confinsUrlService;

    /**
     * <p>current usecase CustomerSignUp</p>
     * <p>Map CustNo to CustNo of @Customer</p>
     */
    public CustomerRemoteDto validateExisting(ExistingCustomerRequest params) {
        try {
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("AdInsKey", "QXMwZjF0VWVOc1BOTER4T2xKcGZhbnkvSk5kS3dYR2M3NVI3WGJLTDlCOFNZUz0=");

            final HttpEntity<ExistingCustomerRequest> requestArgs = new HttpEntity<>(
                    params,
                    headers
            );

            final ResponseEntity<CustomerRemoteDto> response = restTemplate.exchange(
                    confinsUrlService.CustObj_GetListKeyValueActiveByCode(),
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
