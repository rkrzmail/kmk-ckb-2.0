package com.kmkbe.feign.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.service.ExternalApiService;
import com.kmkbe.feign.config.ConfinsR3AuthInterceptor;
import com.kmkbe.feign.config.ConfinsR3ErrorDecoder;
import com.kmkbe.modules.remote.request.*;
import com.kmkbe.modules.remote.service.*;
import com.sun.net.httpserver.HttpServer;
import feign.Feign;
import feign.Retryer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfinsR3HttpRegressionTest {
    @org.springframework.context.annotation.Configuration(proxyBeanMethods = false)
    @org.springframework.cloud.openfeign.EnableFeignClients(clients = ConfinsR3FeignClient.class)
    static class FeignContext {
        @org.springframework.context.annotation.Bean
        com.kmkbe.feign.config.CsulTokenManager tokenManager() {
            return mock(com.kmkbe.feign.config.CsulTokenManager.class);
        }

        @org.springframework.context.annotation.Bean
        com.kmkbe.feign.config.CsulAuthInterceptor csulAuthInterceptor(
                com.kmkbe.feign.config.CsulTokenManager tokenManager) {
            return new com.kmkbe.feign.config.CsulAuthInterceptor(tokenManager);
        }

        @org.springframework.context.annotation.Bean
        com.kmkbe.feign.config.FeignErrorDecoder csulErrorDecoder(
                com.kmkbe.feign.config.CsulTokenManager tokenManager) {
            return new com.kmkbe.feign.config.FeignErrorDecoder(tokenManager);
        }
    }

    @Test
    void springRegisteredClientUsesConfinsConfigurationDespiteGlobalCsulBeans() {
        new org.springframework.boot.test.context.runner.ApplicationContextRunner()
            .withConfiguration(org.springframework.boot.autoconfigure.AutoConfigurations.of(
                org.springframework.cloud.openfeign.FeignAutoConfiguration.class,
                org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration.class))
            .withUserConfiguration(FeignContext.class, com.kmkbe.config.ObjectMapperConfig.class)
            .withPropertyValues("feign.confins.url=" + baseUrl, "feign.confins.adins-key=test-key")
            .run(context -> {
                assertThat(context).hasNotFailed();
                var registeredClient = context.getBean(ConfinsR3FeignClient.class);
                var params = new SignerRequestDto("CUST001", "CWR001", LocalDate.now().toString());
                response = "{\"ReturnObject\":[{\"SignerName\":\"Signer Test\"}]," + HEADER + "}";
                assertThat(registeredClient.getSigners(params).getReturnObject().getFirst().getSignerName())
                    .isEqualTo("Signer Test");
                assertThat(apiKey).isEqualTo("test-key");
                assertThat(authorization).isNull();
                status = 401;
                response = "{\"message\":\"Unauthorized\"}";
                assertThatThrownBy(() -> registeredClient.getSigners(params))
                    .isInstanceOf(feign.FeignException.Unauthorized.class);
                verifyNoInteractions(context.getBean(com.kmkbe.feign.config.CsulTokenManager.class));
            });
    }

    private static final String HEADER = "\"HeaderObj\":{\"StatusCode\":\"200\",\"Message\":\"Success\"}";
    private final ObjectMapper mapper = new com.kmkbe.config.ObjectMapperConfig().objectMapper();
    private HttpServer server;
    private ConfinsR3FeignClient client;
    private ExternalApiService service;
    private RestTemplate esign;
    private String baseUrl;
    private volatile int status = 200;
    private volatile String response;
    private volatile String path;
    private volatile String method;
    private volatile String apiKey;
    private volatile String authorization;
    private volatile JsonNode request;

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            path = exchange.getRequestURI().getPath();
            method = exchange.getRequestMethod();
            apiKey = exchange.getRequestHeaders().getFirst("AdInsKey");
            authorization = exchange.getRequestHeaders().getFirst("Authorization");
            request = mapper.readTree(exchange.getRequestBody());
            byte[] body = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, body.length);
            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        var converters = new HttpMessageConverters(new MappingJackson2HttpMessageConverter(mapper));
        var auth = new ConfinsR3AuthInterceptor();
        ReflectionTestUtils.setField(auth, "adinsKey", "test-key");
        client = Feign.builder().contract(new SpringMvcContract())
            .encoder(new SpringEncoder(() -> converters))
            .decoder(new SpringDecoder(() -> converters))
            .errorDecoder(new ConfinsR3ErrorDecoder())
            .retryer(Retryer.NEVER_RETRY).requestInterceptor(auth)
            .target(ConfinsR3FeignClient.class, baseUrl);
        esign = mock(RestTemplate.class);
        service = new ExternalApiService(esign, client);
    }

    @AfterEach
    void stopServer() {
        if (server != null) server.stop(0);
    }

    private record Endpoint(String name, String path, Object[] arguments, Class<?> responseType,
                            String payload, Map<String, Object> requestFields) {
        @Override public String toString() { return name; }
        Object invoke(ExternalApiService service) throws Exception {
            Class<?>[] types = Arrays.stream(arguments).map(Object::getClass).toArray(Class<?>[]::new);
            try {
                return ExternalApiService.class.getMethod(name, types).invoke(service, arguments);
            } catch (java.lang.reflect.InvocationTargetException exception) {
                if (exception.getCause() instanceof RuntimeException cause) throw cause;
                throw exception;
            }
        }
    }

    static Stream<Endpoint> endpoints() {
        return Stream.of(
            new Endpoint("getAppByAppNo", "/api/los/v1/Application/GetAppByAppNo",
                new Object[]{"APP001"}, AppResponse.class,
                "{\"AppId\":42,\"AppNo\":\"APP001\",\"Tenor\":\"3\"," + HEADER + "}",
                Map.of("TrxNo", "APP001")),
            new Endpoint("getRekDebitur", "/api/los/v1/DisbInfo/GetDisburseFctrByAppNo",
                new Object[]{"APP001"}, RekDebiturResponse.class,
                "{\"ReturnObject\":[{\"BankName\":\"Bank Test\",\"AccNo\":\"123\",\"AccName\":\"Debtor\"}]," + HEADER + "}",
                Map.of("TrxNo", "APP001")),
            new Endpoint("getAppFactoringData", "/api/corelos/v1/AppFctr/GetAppFctrByAppId",
                new Object[]{42}, AppFactoringResponse.class,
                "{\"TotalInvcAmt\":\"1000000.00\",\"TotalRetentionAmt\":\"50000.00\",\"DiskontoAmt\":\"10000.00\"," + HEADER + "}",
                Map.of("Id", 42)),
            new Endpoint("getFinancialData", "/api/corelos/v1/AgrmntFinData/GetFinancialDataByAgrmntNoForView",
                new Object[]{"AGR001"}, FinancialDataResponse.class,
                "{\"AgrmntFinDataObj\":{\"NtfAmt\":\"950000.00\",\"TotalRetentionAmt\":\"50000.00\"},\"AgrmntFeeObjs\":[{\"FeeTypeName\":\"Admin\",\"AppFeeAmt\":\"10000.00\"}]," + HEADER + "}",
                Map.of("TrxNo", "AGR001")),
            new Endpoint("getCwrBwhr", "/api/mou/v1/CwrBouwheer/GetListCwrBouwheerByCwrNo",
                new Object[]{"CWR001"}, CwrBwhrResponse.class,
                "{\"ReturnObject\":[{\"CwrBouwheerCustNo\":\"BW001\"}]," + HEADER + "}",
                Map.of("TrxNo", "CWR001")),
            new Endpoint("getListCwrBwhr", "/api/mou/v1/CwrBouwheerProject/GetListCwrBouwheerProjectByCwrNoAndCwrBouwheerCustNo",
                new Object[]{"CWR001", "BW001"}, CwrListBwhrResponse.class,
                "{\"ReturnObject\":[{\"CooperationAgreementNo\":\"PROJECT001\",\"StartPeriod\":\"2026-01-01\"}]," + HEADER + "}",
                Map.of("CwrNo", "CWR001", "CwrBouwheerCustNo", "BW001")),
            new Endpoint("getKaryawan", "/api/mou/v1/CwrSigner/GetListCwrSignerForUpdatebyCustNoAndCwrNo",
                new Object[]{"CWR001", "CUST001"}, SignerApiResponse.class,
                "{\"ReturnObject\":[{\"SignerName\":\"Signer Test\",\"SignerPosition\":\"Director\"}]," + HEADER + "}",
                Map.of("cwrNo", "CWR001", "custNo", "CUST001"))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void serviceUsesExpectedHttpContractAndPreservesRestTemplateResult(Endpoint endpoint) throws Exception {
        response = endpoint.payload;
        Object result = endpoint.invoke(service);
        assertThat(path).isEqualTo(endpoint.path);
        assertThat(method).isEqualTo("POST");
        assertThat(apiKey).isEqualTo("test-key");
        assertThat(authorization).isNull();
        Map<String, Object> expected = new HashMap<>(endpoint.requestFields);
        expected.put("RequestDateTime", LocalDate.now().toString());
        assertThat(request).isEqualTo(mapper.valueToTree(expected));
        var headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        Object previousTransportResult = new RestTemplate().postForObject(baseUrl + endpoint.path,
            new org.springframework.http.HttpEntity<>(mapper.writeValueAsString(expected), headers), endpoint.responseType);
        assertThat(result).usingRecursiveComparison().isEqualTo(previousTransportResult);
        verifyNoInteractions(esign);
    }

    static Stream<Arguments> httpFailures() {
        return endpoints().flatMap(endpoint -> Stream.of(400, 401, 404, 500)
            .map(code -> Arguments.of(endpoint, code)));
    }

    @ParameterizedTest(name = "{0}: HTTP {1}")
    @MethodSource("httpFailures")
    void upstreamHttpFailureDoesNotBecomeSuccessfulServiceResult(Endpoint endpoint, int code) {
        status = code;
        response = "{\"message\":\"Confins test failure\"}";
        assertThatThrownBy(() -> endpoint.invoke(service)).isInstanceOf(RuntimeException.class);
        verifyNoInteractions(esign);
    }

    @ParameterizedTest(name = "{0}: missing header")
    @MethodSource("endpoints")
    void missingResponseHeaderIsRejected(Endpoint endpoint) {
        response = "{}";
        assertThatThrownBy(() -> endpoint.invoke(service)).isInstanceOf(RuntimeException.class)
            .hasStackTraceContaining("Invalid");
    }

    @Test
    void businessFailureInHttp200RemainsRejectedForFinancialDataAndFactoring() {
        response = "{\"HeaderObj\":{\"StatusCode\":\"400\",\"Message\":\"Not found\"}}";
        assertThatThrownBy(() -> service.getFinancialData("AGR001"))
            .hasMessageContaining("Invalid financial data");
        assertThatThrownBy(() -> service.getAppFactoringData(42))
            .hasStackTraceContaining("Invalid response");
    }

    @Test
    void inquiryServicesPreserveTypedRowsAndCriteria() throws Exception {
        var remote = new CwrRemoteService(mapper, client);
        ReflectionTestUtils.setField(remote, "confinsUrl", baseUrl);
        response = "{\"Data\":[{\"CwrNo\":\"CWR001\",\"PlafondAmt\":1000000}],\"Count\":1," + HEADER + "}";
        var cwr = remote.inquiryCwr(InquiryCwrRemoteRequest.builder().cwrNo("CWR001").build());
        assertThat(cwr.getData()).hasSize(1);
        assertThat(cwr.getData().getFirst().getCwrNo()).isEqualTo("CWR001");
        assertThat(cwr.getData().getFirst().getPlafondAmt()).isEqualTo(1000000);
        assertThat(path).isEqualTo("/api/mou/v1/Generic/GetPagingObjectBySQL");
        assertThat(request.get("RequestDateTime").asText()).matches("\\d{4}-\\d{2}-\\d{2}");
        assertThat(request.at("/criteria/0/value").asText()).isEqualTo("CWR001");

        response = "{\"Data\":[{\"AppId\":42,\"AgrmntNo\":\"AGR001\",\"NtfAmt\":950000}],\"Count\":1," + HEADER + "}";
        var agreement = remote.inquiryAgreementByNoAgreement(
            InquiryAgreementRemoteRequest.builder().agreementNo("AGR001").build());
        assertThat(agreement.getData().getFirst().getAgrmntNo()).isEqualTo("AGR001");
        assertThat(agreement.getData().getFirst().getNtfAmt()).isEqualTo(950000);
        assertThat(path).isEqualTo("/api/los/v1/Generic/GetPagingObjectBySQL");
        assertThat(request.get("RequestDateTime").asText()).matches("\\d{4}-\\d{2}-\\d{2}");
        assertThat(request.at("/criteria/0/value").asText()).isEqualTo("AGR001");

        response = "{\"ReturnObject\":[{\"AgrmntNo\":\"AGR001\",\"AppNo\":\"APP001\"}]," + HEADER + "}";
        var byCwr = remote.inquiryAgreementByNoCwr("CWR001");
        assertThat(byCwr.getData().getFirst().getAppNo()).isEqualTo("APP001");
        assertThat(path).isEqualTo("/api/los/v1/Agrmnt/GetListAgrmntDetailForCwrByCwrNo");
        assertThat(request.get("trxNo").asText()).isEqualTo("CWR001");
    }

    @Test
    void customerZipcodeAndSignerDecodeNonEmptyData() throws Exception {
        response = "{\"CustNo\":\"CUST001\",\"CustName\":\"Customer Test\"}";
        var customerRemote = new CustomerRemoteService(esign, mock(com.kmkbe.core.service.BaseRemoteService.class), mapper, client);
        ReflectionTestUtils.setField(customerRemote, "confinsUrl", baseUrl);
        var customer = customerRemote.validateExisting(ExistingCustomerRequest.builder()
            .args(ExistingCustomerRequest.Args.builder().key(ExistingCustomerRequest.KeyType.ktp).value("123").build())
            .requestDateTime(LocalDate.now().toString()).includeProperties(List.of()).build());
        assertThat(customer.getCustNo()).isEqualTo("CUST001");
        assertThat(customer.getCustName()).isEqualTo("Customer Test");
        assertThat(path).isEqualTo("/api/fou/v1/CustObj/GetObjectByKeyAndValue");
        assertThat(request.at("/KeyAndValueObj/Value").asText()).isEqualTo("123");

        response = "{\"Data\":[{\"City\":\"JAKARTA\",\"Zipcode\":\"12345\"}],\"Count\":1," + HEADER + "}";
        var mst = new MstRemoteService(mapper, client, esign, mock(com.kmkbe.core.service.BaseRemoteService.class));
        var area = mst.areaQuery(2, 5, List.of());
        assertThat(area.getData().getFirst().getZipcode()).isEqualTo("12345");
        assertThat(path).isEqualTo("/api/fou/v1/Generic/GetPagingObjectBySQL");
        assertThat(request.get("RequestDateTime").asText()).matches("\\d{4}-\\d{2}-\\d{2}");
        assertThat(request.get("pageNo").asInt()).isEqualTo(2);
        assertThat(request.get("rowPerPage").asInt()).isEqualTo(5);

        response = "{\"ReturnObject\":[{\"CwrSignerId\":7,\"SignerName\":\"Signer Test\",\"SignerPosition\":\"Director\"}]," + HEADER + "}";
        var signer = client.getSigners(new SignerRequestDto("CUST001", "CWR001", LocalDate.now().toString()));
        assertThat(signer.getReturnObject().getFirst().getCwrSignerId()).isEqualTo(7);
        assertThat(signer.getReturnObject().getFirst().getSignerName()).isEqualTo("Signer Test");
        verifyNoInteractions(esign);
    }
}
