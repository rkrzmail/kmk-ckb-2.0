package com.kmkbe.modules.confinsr3.controller;

import com.kmkbe.core.domain.dto.AppResponse;
import com.kmkbe.core.domain.dto.CustomerRemoteDto;
import com.kmkbe.feign.client.ConfinsR3FeignClient;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ConfinsR3TestControllerTest {
    private MockMvc mvc;
    private ConfinsR3FeignClient client;
    private final AtomicReference<String> calledMethod = new AtomicReference<>();

    @BeforeEach
    void setup() {
        client = mock(ConfinsR3FeignClient.class, invocation -> {
            calledMethod.set(invocation.getMethod().getName());
            if (invocation.getMethod().getReturnType() == CustomerRemoteDto.class) {
                return CustomerRemoteDto.builder().custNo("CUST001").build();
            }
            Object result = invocation.getMethod().getReturnType().getDeclaredConstructor().newInstance();
            if (result instanceof AppResponse app) app.setAppId(42);
            return result;
        });
        mvc = MockMvcBuilders.standaloneSetup(new ConfinsR3TestController(client)).build();
    }

    static Stream<Arguments> routes() {
        return Stream.of(
            Arguments.of("application", "getAppByAppNo", Map.of("appNo", "APP001")),
            Arguments.of("bank-accounts", "getRekDebitur", Map.of("appNo", "APP001")),
            Arguments.of("factoring", "getAppFactoringData", Map.of("appId", "42")),
            Arguments.of("financial-data", "getFinancialData", Map.of("agrmntNo", "AGR001")),
            Arguments.of("cwr-bouwheers", "getCwrBwhr", Map.of("cwrNo", "CWR001")),
            Arguments.of("cwr-projects", "getListCwrBwhr", Map.of("cwrNo", "CWR001", "bouwheerCustNo", "BW001")),
            Arguments.of("employees", "getKaryawan", Map.of("cwrNo", "CWR001", "custNo", "CUST001")),
            Arguments.of("signers", "getSigners", Map.of("cwrNo", "CWR001", "custNo", "CUST001")),
            Arguments.of("cwr-inquiry", "inquiryCwr", Map.of("searchBy", "cwrNo", "searchValue", "CWR001")),
            Arguments.of("agreement-inquiry", "inquiryAgreement", Map.of("agrmntNo", "AGR001")),
            Arguments.of("agreements-by-cwr", "inquiryAgreementByCwr", Map.of("cwrNo", "CWR001")),
            Arguments.of("zipcodes", "areaQuery", Map.of("searchBy", "kota", "searchValue", "JAKARTA")),
            Arguments.of("customer-lookup", "validateExisting", Map.of("searchBy", "ktp", "searchValue", "123"))
        );
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("routes")
    void routesReachTheirFeignMethod(String route, String expectedMethod, Map<String, String> params) throws Exception {
        var request = get("/api/v1/confins/test/" + route);
        params.forEach(request::param);
        mvc.perform(request).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("application/json"));
        assertThat(calledMethod.get()).isEqualTo(expectedMethod);
        assertThat(mockingDetails(client).getInvocations()).hasSize(1);
    }

    @Test
    void successfulResponseIsReturnedWithoutAnotherEnvelope() throws Exception {
        mvc.perform(get("/api/v1/confins/test/application").param("appNo", "APP001"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.AppId").value(42));
    }

    @Test
    void missingRequiredReferenceDoesNotCallConfins() throws Exception {
        mvc.perform(get("/api/v1/confins/test/application")).andExpect(status().isBadRequest());
        verifyNoInteractions(client);
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 404, 500})
    void upstreamErrorIsVisibleToCaller(int code) throws Exception {
        Request request = Request.create(Request.HttpMethod.POST, "http://confins.example",
            Map.of(), null, StandardCharsets.UTF_8, null);
        var response = Response.builder().request(request).status(code).reason("Failure").headers(Map.of())
            .body("{\"Message\":\"Test upstream failure\"}", StandardCharsets.UTF_8).build();
        doThrow(FeignException.errorStatus("getAppByAppNo", response)).when(client).getAppByAppNo(any());
        mvc.perform(get("/api/v1/confins/test/application").param("appNo", "APP001"))
            .andExpect(status().is(code)).andExpect(jsonPath("$.isSuccess").value(false))
            .andExpect(jsonPath("$.data.upstreamStatus").value(code))
            .andExpect(jsonPath("$.data.upstreamBody").value("{\"Message\":\"Test upstream failure\"}"));
    }
}
