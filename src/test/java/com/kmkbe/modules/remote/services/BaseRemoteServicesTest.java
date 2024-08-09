package com.kmkbe.modules.remote.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.converter.ObjectToUrlEncodedConverter;
import com.kmkbe.core.factory.CustomClientHttpRequestFactory;
import com.kmkbe.core.service.ConfinsUrlService;
import com.kmkbe.core.service.LdapUrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
abstract class BaseRemoteServicesTest {
    protected RestTemplate restTemplate;
    protected RestTemplate restTemplateByPassSSL;
    protected ConfinsUrlService confinsUrlService;
    protected LdapUrlService ldapUrlService;
    protected ObjectMapper objectMapper;

    @BeforeEach
    public void setupBeforeEach() {
        objectMapper = new ObjectMapper();
        restTemplate = new RestTemplate();
        restTemplateByPassSSL = new RestTemplate(new CustomClientHttpRequestFactory());
        confinsUrlService = new ConfinsUrlService(
                "https://confins.csulfinance.com/api/fou/v1",
                "https://confins.csulfinance.com/api/mou/v1"
        );
        ldapUrlService = new LdapUrlService(
                "http://10.30.15.146:8185/SISCAConnect/api/v1",
                "KMKDigital",
                "CSUljkt#2024"
        );

        restTemplate.getMessageConverters().add(new ObjectToUrlEncodedConverter(objectMapper));
    }
}
