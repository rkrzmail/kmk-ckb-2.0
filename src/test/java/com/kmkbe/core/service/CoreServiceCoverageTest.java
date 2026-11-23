package com.kmkbe.core.service;

import com.kmkbe.core.domain.model.HttpLoggerPayload;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CoreServiceCoverageTest {

  @Test void apiKeyAuthenticationService() {
    ApiKeyAuthenticationService unauth = new ApiKeyAuthenticationService("key");
    assertThat(unauth.getPrincipal()).isEqualTo("key");
    assertThat(unauth.getCredentials()).isEqualTo("key");
    assertThat(unauth.isAuthenticated()).isFalse();
    unauth.eraseCredentials();

    ApiKeyAuthenticationService auth = new ApiKeyAuthenticationService(
      List.of(new SimpleGrantedAuthority("ROLE_USER")), "key");
    assertThat(auth.isAuthenticated()).isTrue();
    auth.eraseCredentials();
  }

  @Test void ldapUrlService() {
    LdapUrlService svc = new LdapUrlService("http://sisca", "u", "p");
    assertThat(svc.auth_requestJwt()).isEqualTo("http://sisca/auth/req-jwt");
    assertThat(svc.activeDirectory_validation()).isEqualTo("http://sisca/activedirectory/validation");
    assertThat(svc.email_ao()).isEqualTo("http://sisca/user-management/get-ao-email");
    LdapUrlService empty = new LdapUrlService();
    assertThat(empty.auth_requestJwt()).isEqualTo("null/auth/req-jwt");
  }

  @Test void baseRemoteService() {
    BaseRemoteService svc = new BaseRemoteService("fou", "mou", "ad");
    ReflectionTestUtils.setField(svc, "BASE_URL_MST", "mst");
    ReflectionTestUtils.setField(svc, "confinsArBaseUrl", "ar");
    ReflectionTestUtils.setField(svc, "confinsAPBaseUrl", "ap");
    ReflectionTestUtils.setField(svc, "confinsPaymentBaseUrl", "pay");
    ReflectionTestUtils.setField(svc, "apiKey", "api");
    assertThat(svc.getBaseMst()).isEqualTo("mst");
    assertThat(svc.RefMaster_GetListKeyValueActiveByCode()).isEqualTo("fou/RefMaster/GetListKeyValueActiveByCode");
    assertThat(svc.RefMaster_GetListActiveRefMasterWithMappingCodeAll()).isEqualTo("fou/RefMaster/GetListActiveRefMasterWithMappingCodeAll");
    assertThat(svc.Agrmnt_GetAgrmntByAgrmntNoL()).isEqualTo("ar/Agrmnt/GetAgrmntByAgrmntNoL");
    assertThat(svc.Agrmnt_GetAgrmntByAgrmntNo()).isEqualTo("ar/Agrmnt/GetAgrmntByAgrmntNo");
    assertThat(svc.Agrmnt_GetNewInfoByAgrmntNo()).isEqualTo("ar/Agrmnt/GetNewInfoByAgrmntNo");
    assertThat(svc.OnlinePayment_GetOutstandingBillDetailByAgrmntNo()).isEqualTo("ar/OnlinePayment/GetOutstandingBillDetailByAgrmntNo");
    assertThat(svc.PrepaidAlloc_GetRcvAmtValueByParamPriorityCodeV2()).isEqualTo("ar/PrepaidAlloc/GetRcvAmtValueByParamPriorityCodeV2");
    assertThat(svc.PaymentReceive_SubmitPaymentReceiveFromApi()).isEqualTo("pay/PaymentReceive/SubmitPaymentReceiveFromApi");
    assertThat(svc.inquiry_Disburse()).isEqualTo("ap/BasePaging/searchAPInquiry");
    assertThat(svc.adInsKeyHeaders().getFirst("AdInsKey")).isEqualTo("ad");
    assertThat(svc.apiKeyHeaders().getFirst("ApiKey")).isEqualTo("api");
    assertThat(svc.keyHeaders("X","v").getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    assertThat(new BaseRemoteService()).isNotNull();
  }

  @Test void loggingService() {
    @SuppressWarnings("unchecked") MongoCollection<Document> collection = mock(MongoCollection.class);
    LoggingService svc = new LoggingService(collection);
    svc.create(HttpLoggerPayload.builder().uri("/x").build());
    verify(collection).insertOne(any(Document.class));
  }

  @Test void keySecretServices(@TempDir Path tmp) throws Exception {
    KeySecretServices svc = new KeySecretServices();
    ReflectionTestUtils.setField(svc, "secretKey", "secret");
    String payload = "{\"BouwheerCode\":\"B\"}";
    String b64 = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    String token = b64 + "." + com.kmkbe.core.utils.MD5Utils.hash(b64 + ".secret");

    assertThat(svc.splitter(token, 0)).isEqualTo(b64);
    assertThat(svc.splitter(token, 1)).hasSize(32);
    assertThat(svc.splitter(token, 2)).isNull();
    assertThat(new String(svc.decodePayload(token), StandardCharsets.UTF_8)).isEqualTo(payload);
    assertThat(svc.decodePayloadAsString(token)).isEqualTo(payload);
    assertThat(svc.validate(token)).isTrue();
    assertThat(svc.validate(b64 + ".WRONG")).isFalse();

    String payload2 = "{\"TimeStamps\":\"2026-11-23 10:00:00\",\"BouwheerCode\":\"B\"}";
    String b642 = Base64.getUrlEncoder().withoutPadding().encodeToString(payload2.getBytes(StandardCharsets.UTF_8));
    String token2 = b642 + "." + com.kmkbe.core.utils.MD5Utils.hash(b642 + ".secret");
    assertThat(svc.validateWithTimeStamps(token2)).isTrue();
    String noTs = "{\"BouwheerCode\":\"B\"}";
    String b643 = Base64.getUrlEncoder().withoutPadding().encodeToString(noTs.getBytes(StandardCharsets.UTF_8));
    assertThat(svc.validateWithTimeStamps(b643 + "." + com.kmkbe.core.utils.MD5Utils.hash(b643 + ".secret"))).isFalse();
    String badJson = Base64.getUrlEncoder().withoutPadding().encodeToString("bad".getBytes());
    assertThat(svc.validateWithTimeStamps(badJson + ".x")).isFalse();
  }

  @Test void jwtLoanSubmissionService() throws Exception {
    KeySecretServices keys = mock(KeySecretServices.class);
    JwtLoanSubmissionService svc = new JwtLoanSubmissionService(keys);
    assertThat(svc.extractToken(null)).isNull();
    assertThat(svc.extractToken("")).isNull();
    when(keys.decodePayloadAsString("tok")).thenReturn("{\"BouwheerCode\":\"B\",\"VendorCode\":\"V\"}");
    when(keys.validate("tok")).thenReturn(true);
    var model = svc.extractToken("tok");
    assertThat(model).isNotNull();
    assertThat(model.getBouwheerCode()).isEqualTo("B");
    assertThat(model.getVendorCode()).isEqualTo("V");
    assertThat(model.getSignature()).isNull();
    assertThat(model.getCreatedDateString()).isNull();

    when(keys.decodePayloadAsString("full")).thenReturn("{\"BouwheerCode\":\"B\",\"VendorCode\":\"V\",\"Signature\":\"S\",\"CreatedDate\":\"D\"}");
    when(keys.validate("full")).thenReturn(true);
    var full = svc.extractToken("full");
    assertThat(full.getSignature()).isEqualTo("S");
    assertThat(full.getCreatedDateString()).isEqualTo("D");

    when(keys.validate("bad")).thenReturn(false);
    assertThat(svc.extractToken("bad")).isNull();
    when(keys.validate("err")).thenThrow(new RuntimeException());
    assertThat(svc.extractToken("err")).isNull();

    when(keys.decodePayloadAsString("nulljson")).thenReturn("bad");
    when(keys.validate("nulljson")).thenReturn(true);
    assertThat(svc.extractToken("nulljson")).isNull();
  }

  @Test void fileStorageServiceUnderscore(@TempDir Path tmp) throws Exception {
    FileStorageService_ svc = new FileStorageService_(null, tmp.toString());
    MockMultipartFile file = new MockMultipartFile("f", "a.pdf", "application/pdf", "x".getBytes());
    String stored = svc.save(file, "doc", "NAME.PDF");
    assertThat(stored).isEqualTo("/uploads/doc/NAME.pdf");
    assertThat(svc.load("doc/NAME.pdf").exists()).isTrue();
    jakarta.servlet.ServletContext servletContext = mock(jakarta.servlet.ServletContext.class);
    when(servletContext.getMimeType(anyString())).thenReturn("application/pdf");
    MockHttpServletRequest req = new MockHttpServletRequest(servletContext);
    var res = svc.downloadUploadFile(req, "/uploads/doc", "NAME.pdf");
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(svc.delete("/uploads/doc/NAME.pdf", "x")).isTrue();
    assertThatThrownBy(() -> svc.save(null, "doc", "x.pdf")).isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> svc.save(new MockMultipartFile("f", "a.txt", "text/plain", new byte[0]), "doc", "x.txt")).isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> svc.load("missing.pdf")).isInstanceOf(RuntimeException.class);
  }
}
