package com.kmkbe.modules.external.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MSTLoanService {

    private final RestTemplate restTemplate;

    public MSTLoanService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Object fetchVendor() {
        return new Object();
    }

    public List<Object> fetchLegalDocument() {
        return new ArrayList<>();
    }

    public List<Object> fetchListOfPostedInvoice() {
        return new ArrayList<>();
    }
}
