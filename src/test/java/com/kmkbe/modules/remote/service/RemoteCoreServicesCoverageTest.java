package com.kmkbe.modules.remote.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.BaseLdapRemoteResponseDto;
import com.kmkbe.core.domain.dto.BaseSimpleRemoteResponseDto;
import com.kmkbe.core.domain.dto.MailRemoteDto;
import com.kmkbe.core.domain.dto.CustomerRemoteDto;
import com.kmkbe.core.service.BaseRemoteService;
import com.kmkbe.core.service.LdapUrlService;
import com.kmkbe.feign.client.ConfinsR3FeignClient;
import com.kmkbe.modules.loan_submission.service.LoanSubmissionService;
import com.kmkbe.modules.remote.request.ActiveDirectoryRemoteRequest;
import com.kmkbe.modules.remote.request.FinancingSubmissionRequest;
import com.kmkbe.modules.remote.request.UpdateFinancingStatusRequest;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RemoteCoreServicesCoverageTest {
  @Mock RestTemplate restTemplate;
  @Mock BaseRemoteService baseRemoteService;
  @Mock LdapUrlService ldapUrlService;
  @Mock AuthRemoteService authRemoteService;
  @Mock ConfinsR3FeignClient confinsClient;
  final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    when(baseRemoteService.getBaseMst()).thenReturn("http://mst");
    when(baseRemoteService.apiKeyHeaders()).thenReturn(new HttpHeaders());
    when(baseRemoteService.adInsKeyHeaders()).thenReturn(new HttpHeaders());
    ldapUrlService.authHeaderUsername = "user";
    ldapUrlService.authHeaderPassword = "pass";
    when(ldapUrlService.auth_requestJwt()).thenReturn("http://auth/jwt");
    when(ldapUrlService.activeDirectory_validation()).thenReturn("http://auth/ad");
    when(ldapUrlService.email_ao()).thenReturn("http://auth/email");
  }

  @Test
  void customerRemoteBranches() throws Exception {
    CustomerRemoteService service = new CustomerRemoteService(restTemplate, baseRemoteService,
      objectMapper, confinsClient);
    ReflectionTestUtils.setField(service, "confinsUrl", "http://confins");

    doReturn(mock(CustomerRemoteDto.class)).when(confinsClient).validateExisting(any());
    assertThat(service.validateExisting(com.kmkbe.modules.remote.request.ExistingCustomerRequest.builder().build())).isNotNull();

    doThrow(feignError()).when(confinsClient).validateExisting(any());
    assertThatThrownBy(() -> service.validateExisting(
      com.kmkbe.modules.remote.request.ExistingCustomerRequest.builder().build())).isInstanceOf(RuntimeException.class);
    doThrow(feignError("{}")).when(confinsClient).validateExisting(any());
    assertThatThrownBy(() -> service.validateExisting(
      com.kmkbe.modules.remote.request.ExistingCustomerRequest.builder().build())).isInstanceOf(RuntimeException.class);
    doThrow(feignError("not-json")).when(confinsClient).validateExisting(any());
    assertThatThrownBy(() -> service.validateExisting(
      com.kmkbe.modules.remote.request.ExistingCustomerRequest.builder().build())).isInstanceOf(RuntimeException.class);

    doThrow(new IllegalStateException("boom")).when(confinsClient).validateExisting(any());
    assertThatThrownBy(() -> service.validateExisting(
      com.kmkbe.modules.remote.request.ExistingCustomerRequest.builder().build())).isInstanceOf(IllegalStateException.class);

    BaseSimpleRemoteResponseDto<Object> body = new BaseSimpleRemoteResponseDto<>();
    doReturn(ResponseEntity.ok(body)).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));

    LoanSubmissionService.VendorTokenExtractor extractor = mock(LoanSubmissionService.VendorTokenExtractor.class);
    when(extractor.getVendorCode()).thenReturn("V1");
    when(extractor.getBouwheerCode()).thenReturn(UUID.randomUUID());
    assertThat(service.inquiryVendorByBouwheer(extractor)).isSameAs(body);
    doThrow(badRequest()).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThatThrownBy(() -> service.inquiryVendorByBouwheer(extractor)).isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> service.inquiryVendor("V1")).isInstanceOf(RuntimeException.class);
    doThrow(new IllegalStateException("boom")).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThatThrownBy(() -> service.inquiryVendor("V1")).isInstanceOf(IllegalStateException.class);
    doThrow(badRequest("{}")).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThatThrownBy(() -> service.inquiryVendor("V1")).isInstanceOf(RuntimeException.class);
    doThrow(badRequest("not-json")).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThatThrownBy(() -> service.inquiryVendor("V1")).isInstanceOf(RuntimeException.class);
  }

  @Test
  void financingRemoteBranches() throws Exception {
    FinancingRemoteService service = new FinancingRemoteService(restTemplate, baseRemoteService, objectMapper);
    BaseSimpleRemoteResponseDto<Object> body = new BaseSimpleRemoteResponseDto<>();
    doReturn(ResponseEntity.ok(body)).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));

    assertThat(service.postedSubmission(FinancingSubmissionRequest.builder().build())).isSameAs(body);
    UpdateFinancingStatusRequest request = UpdateFinancingStatusRequest.builder()
      .status(UpdateFinancingStatusRequest.Status.Approved).build();
    assertThat(service.updateFinancingStatus(request)).isSameAs(body);

    doReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)).when(restTemplate)
      .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
        any(org.springframework.core.ParameterizedTypeReference.class));
    assertThatThrownBy(() -> service.updateFinancingStatus(request)).isInstanceOf(RuntimeException.class);

    doThrow(badRequest()).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThatThrownBy(() -> service.postedSubmission(FinancingSubmissionRequest.builder().build()))
      .isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> service.updateFinancingStatus(request)).isInstanceOf(RuntimeException.class);
    doThrow(new IllegalStateException("boom")).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThatThrownBy(() -> service.updateFinancingStatus(request)).isInstanceOf(IllegalStateException.class);
    doThrow(badRequest("{}")).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThatThrownBy(() -> service.postedSubmission(FinancingSubmissionRequest.builder().build()))
      .isInstanceOf(RuntimeException.class);
    doThrow(badRequest("not-json")).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThatThrownBy(() -> service.updateFinancingStatus(request)).isInstanceOf(RuntimeException.class);
  }

  @Test
  void configAndAuthBranches() throws Exception {
    ConfigRemoteService config = new ConfigRemoteService(restTemplate, authRemoteService);
    ReflectionTestUtils.setField(config, "siscaUrlWhiteList", "http://sisca");
    BaseLdapRemoteResponseDto<String> token = new BaseLdapRemoteResponseDto<>();
    token.setData("token");
    doReturn(token).when(authRemoteService).fetchAuthJwt();
    doReturn(ResponseEntity.ok(mock(MailRemoteDto.class))).when(restTemplate)
      .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(MailRemoteDto.class));
    assertThat(config.fetchEmailInfo()).isNotNull();
    doThrow(new IllegalStateException("boom")).when(authRemoteService).fetchAuthJwt();
    assertThatThrownBy(config::fetchEmailInfo).isInstanceOf(IllegalStateException.class);
    doReturn(token).when(authRemoteService).fetchAuthJwt();
    doReturn(ResponseEntity.ok(mock(com.kmkbe.core.domain.dto.email.MailPositionDto.class)))
      .when(restTemplate).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
        any(org.springframework.core.ParameterizedTypeReference.class));
    assertThat(config.getEmailByPosition("E1", "BR1", "AO")).isNotNull();
    doThrow(badRequest()).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThatThrownBy(() -> config.getEmailByPosition("E1", "BR1", "AO"))
      .isInstanceOf(HttpClientErrorException.class);

    AuthRemoteService auth = new AuthRemoteService(ldapUrlService, restTemplate, objectMapper);
    doReturn(ResponseEntity.ok("{\"data\":\"jwt\"}")).when(restTemplate)
      .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    assertThat(auth.fetchAuthJwt().getData()).isEqualTo("jwt");
    doReturn(ResponseEntity.ok("\"jwt\"")).when(restTemplate)
      .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    assertThat(auth.fetchAuthJwt().getData()).isEqualTo("jwt");
    doReturn(ResponseEntity.ok("")).when(restTemplate)
      .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    assertThatThrownBy(auth::fetchAuthJwt).isInstanceOf(RuntimeException.class);
    doReturn(ResponseEntity.ok("{\"data\":\"\",\"header\":{\"success\":{\"isSuccess\":false}}}"))
      .when(restTemplate).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    assertThatThrownBy(auth::fetchAuthJwt).isInstanceOf(RuntimeException.class);
    doThrow(timestampError()).doReturn(ResponseEntity.ok("plain-token")).when(restTemplate)
      .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    assertThat(auth.fetchAuthJwt().getData()).isEqualTo("plain-token");
    doThrow(badRequest(400)).when(restTemplate)
      .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    assertThatThrownBy(auth::fetchAuthJwt).isInstanceOf(RuntimeException.class);
    doReturn(ResponseEntity.ok((String) null)).when(restTemplate)
      .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    assertThatThrownBy(auth::fetchAuthJwt).isInstanceOf(RuntimeException.class);
    doReturn(ResponseEntity.ok("{\"data\":null}")).when(restTemplate)
      .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    assertThatThrownBy(auth::fetchAuthJwt).isInstanceOf(RuntimeException.class);
    doReturn(ResponseEntity.ok("{\"data\":\"jwt\",\"header\":{\"success\":{\"isSuccess\":null}}}"))
      .when(restTemplate).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    assertThat(auth.fetchAuthJwt().getData()).isEqualTo("jwt");
    doReturn(ResponseEntity.ok("{\"data\":\"jwt\",\"header\":{\"success\":{\"isSuccess\":true}}}"))
      .when(restTemplate).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    assertThat(auth.fetchAuthJwt().getData()).isEqualTo("jwt");
    doReturn(ResponseEntity.ok("{\"data\":\"  \"}")).when(restTemplate)
      .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    assertThatThrownBy(auth::fetchAuthJwt).isInstanceOf(RuntimeException.class);
    doReturn(ResponseEntity.ok("x")).when(restTemplate)
      .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    assertThat(auth.fetchAuthJwt().getData()).isEqualTo("x");
    doReturn(ResponseEntity.ok("\"token")).when(restTemplate)
      .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    assertThat(auth.fetchAuthJwt().getData()).isEqualTo("\"token");
    doReturn(ResponseEntity.ok("\"\"")).when(restTemplate)
      .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    assertThatThrownBy(auth::fetchAuthJwt).isInstanceOf(RuntimeException.class);
  }

  @Test
  void userRemoteEmailAndCurrencyBranches() throws Exception {
    UserInternalRemoteService userRemote = new UserInternalRemoteService(ldapUrlService, restTemplate,
      authRemoteService, objectMapper);
    BaseLdapRemoteResponseDto<String> token = new BaseLdapRemoteResponseDto<>();
    token.setData("token");
    doReturn(token).when(authRemoteService).fetchAuthJwt();
    doReturn(ResponseEntity.ok(mock(BaseLdapRemoteResponseDto.class))).when(restTemplate)
      .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
        any(org.springframework.core.ParameterizedTypeReference.class));
    assertThat(userRemote.validateActiveDirectory(ActiveDirectoryRemoteRequest.builder().build())).isNotNull();
    doThrow(badRequest()).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThatThrownBy(() -> userRemote.validateActiveDirectory(ActiveDirectoryRemoteRequest.builder().build()))
      .isInstanceOf(RuntimeException.class);

    EmailAo emailAo = new EmailAo(ldapUrlService, restTemplate);
    doReturn(ResponseEntity.ok(Map.of("data", List.of(Map.of("email", "ao@example.com")))))
      .when(restTemplate).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
        any(org.springframework.core.ParameterizedTypeReference.class));
    assertThat(emailAo.getEmailByPosition("BR1", "AO", "jwt")).hasSize(1);
    doReturn(ResponseEntity.ok(Map.of())).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThat(emailAo.getEmailByPosition("BR1", "AO", "jwt")).isEmpty();
    doReturn(ResponseEntity.ok(null)).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThat(emailAo.getEmailByPosition("BR1", "AO", "jwt")).isEmpty();
    doThrow(new IllegalStateException("boom")).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThat(emailAo.getEmailByPosition("BR1", "AO", "jwt")).isEmpty();

    CurrencyRemoteService currency = new CurrencyRemoteService(restTemplate);
    doReturn(ResponseEntity.ok(Map.of("usd", Map.of("idr", 16000.123)))).when(restTemplate)
      .exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
        any(org.springframework.core.ParameterizedTypeReference.class));
    assertThat(currency.fetchIdrFrom("USD")).isEqualTo(16000.13);
    doReturn(ResponseEntity.ok(Map.of())).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThat(currency.fetchIdrFrom("USD")).isEqualTo(15000.0);
    doThrow(new IllegalStateException("boom")).doReturn(ResponseEntity.ok(Map.of("usd", Map.of("idr", 15000))))
      .when(restTemplate).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
        any(org.springframework.core.ParameterizedTypeReference.class));
    assertThat(currency.fetchIdrFrom("USD")).isEqualTo(15000.0);
    doReturn(ResponseEntity.ok(null)).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThat(currency.fetchIdrFrom("USD")).isEqualTo(15000.0);
    doReturn(ResponseEntity.ok(Map.of("usd", Map.of()))).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThat(currency.fetchIdrFrom("USD")).isEqualTo(15000.0);
    doThrow(new IllegalStateException("boom")).when(restTemplate).exchange(anyString(), any(HttpMethod.class),
      any(HttpEntity.class), any(org.springframework.core.ParameterizedTypeReference.class));
    assertThatThrownBy(() -> currency.fetchIdrFrom("USD")).isInstanceOf(IllegalStateException.class);

    LoanSubmissionRemoteService loanSubmissionRemote = new LoanSubmissionRemoteService(restTemplate, baseRemoteService);
    assertThat(loanSubmissionRemote.fetchVendor()).isNotNull();
    assertThat(loanSubmissionRemote.fetchLegalDocument()).isEmpty();
  }

  private static HttpClientErrorException badRequest() {
    return badRequest(400);
  }

  private static HttpClientErrorException badRequest(int status) {
    return badRequest(status, "{\"message\":\"bad request\"}");
  }

  private static HttpClientErrorException badRequest(String body) {
    return badRequest(400, body);
  }

  private static HttpClientErrorException badRequest(int status, String body) {
    return new HttpClientErrorException(HttpStatus.resolve(status), "bad request", HttpHeaders.EMPTY,
      body.getBytes(), null);
  }

  private static HttpClientErrorException timestampError() {
    return new HttpClientErrorException(HttpStatus.BAD_REQUEST, "bad request", HttpHeaders.EMPTY,
      "{\"header\":{\"success\":{\"message\":\"Make sure the request timestamp is up to date.\"}}}".getBytes(), null);
  }

  private static FeignException feignError() {
    return feignError("{\"message\":\"bad request\"}");
  }

  private static FeignException feignError(String body) {
    return new FeignException.BadRequest("bad request", Request.create(Request.HttpMethod.POST,
      "http://confins", Map.of(), null, StandardCharsets.UTF_8, null),
      body.getBytes(StandardCharsets.UTF_8), Map.of());
  }
}
