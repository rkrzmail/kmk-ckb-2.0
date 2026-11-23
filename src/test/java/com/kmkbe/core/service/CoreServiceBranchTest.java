package com.kmkbe.core.service;

import com.kmkbe.core.domain.dto.ExternalSigningRequest;
import com.kmkbe.core.domain.dto.ExternalSigningResponse;
import com.kmkbe.feign.client.ConfinsR3FeignClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CoreServiceBranchTest {

  @Test void esignApiSuccessAndFailure() {
    RestTemplate rest = mock(RestTemplate.class);
    ExternalApiService svc = new ExternalApiService(rest, mock(ConfinsR3FeignClient.class), "http://esign", "key");
    ExternalSigningResponse body = ExternalSigningResponse.builder().build();
    when(rest.exchange(eq("http://esign"), eq(HttpMethod.POST), any(HttpEntity.class), eq(ExternalSigningResponse.class)))
      .thenReturn(ResponseEntity.ok(body));
    assertThat(svc.callEsignApi(new ExternalSigningRequest())).isSameAs(body);

    when(rest.exchange(eq("http://esign"), eq(HttpMethod.POST), any(HttpEntity.class), eq(ExternalSigningResponse.class)))
      .thenReturn(new ResponseEntity<>(body, HttpStatus.BAD_GATEWAY));
    assertThatThrownBy(() -> svc.callEsignApi(new ExternalSigningRequest()))
      .isInstanceOf(RuntimeException.class).hasMessageContaining("Failed to call e-sign API");
  }

  @Test void fileStorageServiceErrorBranches(@TempDir Path tmp) throws Exception {
    FileStorageService svc = new FileStorageService(new MockServletContext(), tmp.toString());

    assertThatThrownBy(() -> svc.save(null, "d", "x.pdf")).isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> svc.save(new MockMultipartFile("f", null, null, new byte[0]), "d", "x.pdf"))
      .isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> svc.save(new MockMultipartFile("f", "noext", "text/plain", "x".getBytes()), "d", "noext"))
      .isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> svc.save(new MockMultipartFile("f", "a.txt", "text/plain", "x".getBytes()), "d", "a.txt"))
      .isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> svc.save(new MockMultipartFile("f", "a.pdf", "application/pdf", "notpdf".getBytes()), "d", "a.pdf"))
      .isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> svc.save(new MockMultipartFile("f", "a.pdf", "application/pdf", "x".getBytes()), "d", "a.pdf"))
      .isInstanceOf(RuntimeException.class);

    MockMultipartFile pdf = new MockMultipartFile("f", "ok.pdf", "application/pdf", "%PDF-1.4 test".getBytes(StandardCharsets.US_ASCII));
    String stored = svc.save(pdf, "newdir", "OK.PDF");
    assertThat(stored).contains("newdir/").endsWith(".pdf");
    String savedName = stored.substring(stored.lastIndexOf('/') + 1);

    assertThat(svc.load("/uploads/newdir/" + savedName).exists()).isTrue();
    assertThatThrownBy(() -> svc.load("missing.pdf")).isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> svc.load("")).isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> svc.load("../escape.pdf")).isInstanceOf(RuntimeException.class);
    assertThat(svc.delete("/uploads/newdir/" + savedName, "x")).isTrue();
    assertThat(svc.delete("newdir/missing.pdf", "x")).isFalse();

    jakarta.servlet.ServletContext ctx = mock(jakarta.servlet.ServletContext.class);
    when(ctx.getMimeType(anyString())).thenReturn(null);
    MockHttpServletRequest req = new MockHttpServletRequest(ctx);
    assertThatThrownBy(() -> svc.downloadUploadFile(req, "newdir", savedName, "inline"))
      .isInstanceOf(RuntimeException.class).hasMessageContaining("not found");

    MockMultipartFile pdf2 = new MockMultipartFile("f", "ok.pdf", "application/pdf", "%PDF-1.4 test".getBytes(StandardCharsets.US_ASCII));
    String stored2 = svc.save(pdf2, "newdir", "OK.pdf");
    String savedName2 = stored2.substring(stored2.lastIndexOf('/') + 1);
    MockHttpServletRequest okReq = new MockHttpServletRequest(ctx);
    okReq.setParameter("cd", "attachment");
    var res = svc.downloadUploadFile(okReq, "newdir/" + savedName2, savedName2, "attachment");
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(res.getHeaders().getFirst("Content-Disposition")).contains("attachment");
  }
}
