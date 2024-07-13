package com.kmkbe.modules.external.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class MSTCustomerService {
    private final RestTemplate restTemplate;

    public MSTCustomerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Object validateExistingCustomer() {
        return new Object();
    }
}
