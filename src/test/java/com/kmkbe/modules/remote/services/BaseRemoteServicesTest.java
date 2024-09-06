package com.kmkbe.modules.remote.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.kmkbe.core.converter.DateDeserializer;
import com.kmkbe.core.converter.ObjectToUrlEncodedConverter;
import com.kmkbe.core.converter.TemporalDateTimeSerializer;
import com.kmkbe.core.factory.CustomClientHttpRequestFactory;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.core.service.LdapUrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.temporal.TemporalAccessor;
import java.util.Date;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
abstract class BaseRemoteServicesTest {
    protected RestTemplate restTemplate;
    protected RestTemplate restTemplateByPassSSL;
    protected BaseRemoteService baseRemoteService;
    protected LdapUrlService ldapUrlService;
    protected ObjectMapper objectMapper;

    @BeforeEach
    public void setupBeforeEach() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(TemporalAccessor.class, new TemporalDateTimeSerializer());
        simpleModule.addDeserializer(Date.class, new DateDeserializer());

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(simpleModule);

        restTemplate = new RestTemplate();
        restTemplateByPassSSL = new RestTemplate(new CustomClientHttpRequestFactory());

        baseRemoteService = new BaseRemoteService(
                "https://confins.csulfinance.com/api/fou/v1",
                "https://confins.csulfinance.com/api/mou/v1",
                "QXMwZjF0VWVOc1BOTER4T2xKcGZhbnkvSk5kS3dYR2M3NVI3WGJLTDlCOFNZUz0="
        );
        ldapUrlService = new LdapUrlService(
                "http://10.30.15.146:8185/SISCAConnect/api/v1",
                "KMKDigital",
                "CSUljkt#2024"
        );

        restTemplate.getMessageConverters().add(new ObjectToUrlEncodedConverter(objectMapper));
    }
}
