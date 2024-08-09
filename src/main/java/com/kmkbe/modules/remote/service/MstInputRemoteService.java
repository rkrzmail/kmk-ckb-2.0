package com.kmkbe.modules.remote.service;

import com.kmkbe.core.service.ConfinsUrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class MstInputRemoteService {
    private final RestTemplate restTemplate;
    private final ConfinsUrlService confinsUrlService;

    public void customerInput() {
        try {

        } catch (Exception e) {
            log.error("getCustomerInput: {}", e.getMessage());
            throw e;
        }
    }

    public void companyType() {
        try {

        } catch (Exception e) {
            log.error("getCompanyType: {}", e.getMessage());
            throw e;
        }
    }

    public void debiturModel() {
        try {

        } catch (Exception e) {
            log.error("getDebiturModel: {}", e.getMessage());
            throw e;
        }
    }

    public void idType() {
        try {

        } catch (Exception e) {
            log.error("idType: {}", e.getMessage());
            throw e;
        }
    }

    public void ownerShip() {
        try {

        } catch (Exception e) {
            log.error("ownerShip: {}", e.getMessage());
            throw e;
        }
    }

    public void zipCode() {
        try {

        } catch (Exception e) {
            log.error("zipCode: {}", e.getMessage());
            throw e;
        }
    }
}
