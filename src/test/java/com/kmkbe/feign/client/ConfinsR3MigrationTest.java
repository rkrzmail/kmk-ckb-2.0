package com.kmkbe.feign.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.feign.config.ConfinsR3AuthInterceptor;
import com.kmkbe.feign.config.ConfinsR3ErrorDecoder;
import com.kmkbe.modules.remote.request.*;
import feign.Feign;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

class ConfinsR3MigrationTest {
    @Test
    void confinsDoesNotRequestCsulToken() {
        var tokenManager = org.mockito.Mockito.mock(com.kmkbe.feign.config.CsulTokenManager.class);
        var template = new feign.RequestTemplate();
        template.feignTarget(new feign.Target.HardCodedTarget<>(ConfinsR3FeignClient.class,
            "confinsR3FouFeignClient", "https://confins.example"));
        new com.kmkbe.feign.config.CsulAuthInterceptor(tokenManager).apply(template);
        org.mockito.Mockito.verifyNoInteractions(tokenManager);
        assertThat(template.headers()).doesNotContainKey("Authorization");
    }

    @Test
    void directCallsEncodeRequestsAndDecodeConfinsResponses() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        HttpMessageConverters converters = new HttpMessageConverters(new MappingJackson2HttpMessageConverter());
        AtomicReference<Request> captured = new AtomicReference<>();
        ConfinsR3AuthInterceptor auth = new ConfinsR3AuthInterceptor();
        ReflectionTestUtils.setField(auth, "adinsKey", "test-key");
        ConfinsR3FeignClient client = Feign.builder()
            .contract(new SpringMvcContract())
            .encoder(new SpringEncoder(() -> converters))
            .decoder(new SpringDecoder(() -> converters))
            .requestInterceptor(auth)
            .client((request, options) -> {
                captured.set(request);
                return Response.builder().request(request).status(200).reason("OK")
                    .headers(Map.of("Content-Type", java.util.List.of("application/json")))
                    .body("{\"HeaderObj\":{\"StatusCode\":\"200\"},\"StatusCode\":\"200\",\"ReturnObject\":[],\"Data\":[]}", StandardCharsets.UTF_8)
                    .build();
            })
            .target(ConfinsR3FeignClient.class, "https://confins.example");

        AppRequest app = new AppRequest();
        app.setTrxNo("APP001");
        app.setRequestDateTime("2026-09-26");
        assertThat(client.getAppByAppNo(app).getHeaderObj()).isNotNull();
        assertThat(captured.get().url()).isEqualTo("https://confins.example/api/los/v1/Application/GetAppByAppNo");
        assertThat(captured.get().httpMethod()).isEqualTo(Request.HttpMethod.POST);
        assertThat(captured.get().headers().get("AdInsKey")).containsExactly("test-key");
        assertThat(mapper.readTree(captured.get().body()).get("TrxNo").asText()).isEqualTo("APP001");

        assertThat(client.getSigners(new SignerRequestDto("CUST001", "CWR001", "2026-09-26"))
            .getReturnObject()).isEmpty();
        assertThat(captured.get().url()).endsWith("/api/mou/v1/CwrSigner/GetListCwrSignerForUpdatebyCustNoAndCwrNo");
        var signerBody = mapper.readTree(captured.get().body());
        assertThat(signerBody.get("custNo").asText()).isEqualTo("CUST001");
        assertThat(signerBody.get("cwrNo").asText()).isEqualTo("CWR001");
        assertThat(signerBody.get("RequestDateTime").asText()).isEqualTo("2026-09-26");

        client.inquiryCwr(InquiryCwrCriteriaRemoteRequest.builder()
            .queryString(CriteriaGenericTypeRemoteRequest.QueryString.inquiryCwr())
            .criteria(java.util.List.of()).build());
        assertThat(captured.get().url()).endsWith("/api/mou/v1/Generic/GetPagingObjectBySQL");
        assertThat(mapper.readTree(captured.get().body()).get("includeData").asBoolean()).isTrue();

        client.inquiryAgreement(InquiryAgreementCriteriaRemoteRequest.builder()
            .queryString(CriteriaGenericTypeRemoteRequest.QueryString.inquiryAgreement())
            .criteria(java.util.List.of()).build());
        assertThat(captured.get().url()).endsWith("/api/los/v1/Generic/GetPagingObjectBySQL");
        client.areaQuery(AreaCriteriaRemoteRequest.builder()
            .queryString(CriteriaGenericTypeRemoteRequest.QueryString.zipCode())
            .criteria(java.util.List.of()).build());
        assertThat(captured.get().url()).endsWith("/api/fou/v1/Generic/GetPagingObjectBySQL");
        client.validateExisting(ExistingCustomerRequest.builder()
            .args(ExistingCustomerRequest.Args.builder().key(ExistingCustomerRequest.KeyType.ktp).value("123").build())
            .requestDateTime("2026-09-26").build());
        assertThat(captured.get().url()).endsWith("/api/fou/v1/CustObj/GetObjectByKeyAndValue");
        assertThat(mapper.readTree(captured.get().body()).get("KeyAndValueObj").get("Key").asText()).isEqualTo("IdNo");
    }

    @Test
    void unauthorizedConfinsResponseRetainsHttpStatus() {
        Request request = Request.create(Request.HttpMethod.POST, "https://confins.example/api/mou/v1",
            Map.of(), null, StandardCharsets.UTF_8, null);
        Response response = Response.builder().request(request).status(401).reason("Unauthorized")
            .headers(Map.of()).build();
        assertThat(new ConfinsR3ErrorDecoder().decode("getSigners", response))
            .isInstanceOf(feign.FeignException.Unauthorized.class);
    }
}
