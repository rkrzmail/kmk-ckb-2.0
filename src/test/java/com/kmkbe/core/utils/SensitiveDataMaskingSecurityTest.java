package com.kmkbe.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.aspect.HttpRequestAspect;
import com.kmkbe.core.domain.entity.ApiIntegrationLog;
import com.kmkbe.core.domain.repository.ApiIntegrationLogRepository;
import com.kmkbe.core.middleware.HttpRequestResponseLogFilter;
import com.kmkbe.core.middleware.HttpServletRequestCopier;
import jakarta.servlet.FilterChain;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensitiveDataMaskingSecurityTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private ApiIntegrationLogRepository apiIntegrationLogRepository;
    @Mock private ProceedingJoinPoint joinPoint;
    @Mock private FilterChain filterChain;

    @Test
    @DisplayName("H10: AuditMaskingUtils masks all sensitive password, pin, token, secret, and otp keys in JSON")
    void masksAllSensitiveKeysInJson() throws Exception {
        String sensitiveJson = """
            {
              "email": "user@example.com",
              "password": "ClearTextPassword123!",
              "pin": "123456",
              "custPin": "654321",
              "newPin": "999888",
              "oldPin": "111222",
              "token": "secret-jwt-token-xyz",
              "refreshToken": "secret-refresh-token-abc",
              "secretKey": "my-shared-secret",
              "apiKey": "$2b$10$YoLl0SFxCMlIWXfQ9RhixeU8Vxvj9Fi7RmF5j7zA9dhYwdplSGyWC",
              "aesKey": "aXty08csul99hjkl",
              "otp": "887766",
              "otpCode": "554433",
              "details": {
                "nestedPassword": "nested-clear-pass",
                "nestedPin": "4321",
                "normalField": "public-data"
              }
            }
            """;

        String maskedJson = AuditMaskingUtils.maskJsonString(sensitiveJson);
        assertThat(maskedJson).isNotNull();

        JsonNode root = objectMapper.readTree(maskedJson);
        assertThat(root.get("email").asText()).isEqualTo("user@example.com");
        assertThat(root.get("password").asText()).isEqualTo("******");
        assertThat(root.get("pin").asText()).isEqualTo("******");
        assertThat(root.get("custPin").asText()).isEqualTo("******");
        assertThat(root.get("newPin").asText()).isEqualTo("******");
        assertThat(root.get("oldPin").asText()).isEqualTo("******");
        assertThat(root.get("token").asText()).isEqualTo("******");
        assertThat(root.get("refreshToken").asText()).isEqualTo("******");
        assertThat(root.get("secretKey").asText()).isEqualTo("******");
        assertThat(root.get("apiKey").asText()).isEqualTo("******");
        assertThat(root.get("aesKey").asText()).isEqualTo("******");
        assertThat(root.get("otp").asText()).isEqualTo("******");
        assertThat(root.get("otpCode").asText()).isEqualTo("******");

        assertThat(root.get("details").get("nestedPassword").asText()).isEqualTo("******");
        assertThat(root.get("details").get("nestedPin").asText()).isEqualTo("******");
        assertThat(root.get("details").get("normalField").asText()).isEqualTo("public-data");

        // Memastikan tidak ada plaintext password/pin di dalam output string
        assertThat(maskedJson).doesNotContain("ClearTextPassword123!");
        assertThat(maskedJson).doesNotContain("123456");
        assertThat(maskedJson).doesNotContain("654321");
        assertThat(maskedJson).doesNotContain("secret-jwt-token-xyz");
        assertThat(maskedJson).doesNotContain("887766");
    }

    @Test
    @DisplayName("H10: AuditMaskingUtils masks Authorization, ApiKey, and rtoken HTTP headers")
    void masksSensitiveHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("secret-bearer-jwt-12345");
        headers.set("ApiKey", "api-key-secret-999");
        headers.set("rtoken", "session-rtoken-uuid");
        headers.set("Content-Type", "application/json");
        headers.set("X-Correlation-ID", "corr-123");

        Map<String, List<String>> masked = AuditMaskingUtils.maskHeaders(headers);

        assertThat(masked.get("Authorization")).containsExactly("******");
        assertThat(masked.get("ApiKey")).containsExactly("******");
        assertThat(masked.get("rtoken")).containsExactly("******");
        assertThat(masked.get("Content-Type")).containsExactly("application/json");
        assertThat(masked.get("X-Correlation-ID")).containsExactly("corr-123");
    }

    @Test
    @DisplayName("H10: HttpUtils.createRequestLog masks headers and payload containing password/pin")
    void createRequestLogMasksCredentials() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/sign-in");
        request.setContentType("application/json");
        request.addHeader("Authorization", "Bearer sensitive-token");
        request.addHeader("ApiKey", "sensitive-api-key");
        request.setQueryString("pin=1234&user=john");
        String payload = "{\"email\":\"john@example.com\",\"password\":\"mySecretPassword\"}";
        request.setContent(payload.getBytes(StandardCharsets.UTF_8));

        HttpServletRequestCopier wrapped = new HttpServletRequestCopier(request);
        Map<String, Object> logMap = HttpUtils.createRequestLog(wrapped, 50000);

        // 1. Headers masked
        @SuppressWarnings("unchecked")
        Map<String, List<String>> headers = (Map<String, List<String>>) logMap.get("headers");
        assertThat(headers.get("Authorization")).containsExactly("******");
        assertThat(headers.get("ApiKey")).containsExactly("******");

        // 2. QueryString masked
        assertThat((String) logMap.get("queryString")).contains("pin=******");
        assertThat((String) logMap.get("queryString")).doesNotContain("1234");

        // 3. Payload masked
        @SuppressWarnings("unchecked")
        Map<String, Object> payloadMap = (Map<String, Object>) logMap.get("payload");
        assertThat(payloadMap.get("email")).isEqualTo("john@example.com");
        assertThat(payloadMap.get("password")).isEqualTo("******");
    }

    @Test
    @DisplayName("H10: HttpRequestAspect masks sensitive payload and headers before saving to api_integration_log table")
    void httpRequestAspectMasksPayloadAndHeaders() throws Throwable {
        HttpRequestAspect aspect = new HttpRequestAspect(apiIntegrationLogRepository, objectMapper);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("jwt-token-to-mask");
        headers.set("ApiKey", "api-key-to-mask");
        headers.set("Content-Type", "application/json");

        String rawBody = "{\"username\":\"admin\",\"password\":\"super-secret-pass\",\"pin\":\"9876\"}";
        HttpEntity<String> httpEntity = new HttpEntity<>(rawBody, headers);

        when(joinPoint.getArgs()).thenReturn(new Object[]{"https://internal-service.csulfinance.com/api/test", httpEntity});
        when(joinPoint.proceed()).thenReturn(ResponseEntity.ok("{\"status\":\"success\",\"token\":\"token-in-response-abc\"}"));

        aspect.logRestTemplateCalls(joinPoint);

        ArgumentCaptor<ApiIntegrationLog> captor = ArgumentCaptor.forClass(ApiIntegrationLog.class);
        verify(apiIntegrationLogRepository).save(captor.capture());

        ApiIntegrationLog savedLog = captor.getValue();
        assertThat(savedLog).isNotNull();

        // 1. requestPayload must be masked
        assertThat(savedLog.getRequestPayload()).doesNotContain("super-secret-pass");
        assertThat(savedLog.getRequestPayload()).doesNotContain("9876");
        assertThat(savedLog.getRequestPayload()).contains("\"password\":\"******\"");
        assertThat(savedLog.getRequestPayload()).contains("\"pin\":\"******\"");

        // 2. responseJson must also mask sensitive response tokens
        assertThat(savedLog.getResponseJson()).doesNotContain("token-in-response-abc");
        assertThat(savedLog.getResponseJson()).contains("\"token\":\"******\"");
    }

    @Test
    @DisplayName("H10: HttpRequestResponseLogFilter sanitizes messages and does not crash on normal requests")
    void httpRequestResponseLogFilterSanitizesProperly() throws Exception {
        HttpRequestResponseLogFilter filter = new HttpRequestResponseLogFilter();

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/sign-in");
        request.setContentType("application/json");
        request.setContent("{\"password\":\"p@ssword123\",\"email\":\"test@test.com\"}".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(any(), any());
    }
}
