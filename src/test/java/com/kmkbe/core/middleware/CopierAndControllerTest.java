package com.kmkbe.core.middleware;

import jakarta.servlet.ReadListener;
import jakarta.servlet.WriteListener;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

class CopierAndControllerTest {
  @Test void requestCopierCopiesAndParsesJson() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("POST", "/x");
    req.setContent("{\"a\":1}".getBytes(StandardCharsets.UTF_8));
    HttpServletRequestCopier copier = new HttpServletRequestCopier(req);
    assertThat(new String(copier.getContentAsByteArray(), StandardCharsets.UTF_8)).isEqualTo("{\"a\":1}");
    assertThat(copier.getInputStream().isReady()).isTrue();
    assertThat(copier.getInputStream().isFinished()).isFalse();
    copier.getInputStream().setReadListener((ReadListener) null);
  }

  @Test void responseCopierOutputStreamAndWriter() throws Exception {
    MockHttpServletResponse res = new MockHttpServletResponse();
    HttpServletResponseCopier copier = new HttpServletResponseCopier(res);
    var os = copier.getOutputStream();
    assertThat(os.isReady()).isTrue();
    os.setWriteListener((WriteListener) null);
    os.write('a');
    copier.flushBuffer();
    assertThat(new String(copier.getContentAsByteArray(), StandardCharsets.UTF_8)).contains("a");
    assertThatThrownBy(copier::getWriter).isInstanceOf(IllegalStateException.class);
  }

  @Test void responseCopierWriterBranch() throws Exception {
    MockHttpServletResponse res = new MockHttpServletResponse();
    HttpServletResponseCopier copier = new HttpServletResponseCopier(res);
    var writer = copier.getWriter();
    writer.write("hello");
    copier.flushBuffer();
    assertThat(new String(copier.getContentAsByteArray(), StandardCharsets.UTF_8)).contains("hello");
    assertThatThrownBy(copier::getOutputStream).isInstanceOf(IllegalStateException.class);
  }

  @Test void responseCopierNoContent() throws Exception {
    HttpServletResponseCopier copier = new HttpServletResponseCopier(new MockHttpServletResponse());
    assertThat(copier.getContentAsByteArray()).isEmpty();
  }

  @Test void errorAuthController() {
    ErrorAuthController c = new ErrorAuthController();
    assertThat(c.handleUnauthorized().getStatusCode().value()).isEqualTo(401);
    assertThat(c.handleUnauthorized().getBody().get("scope")).isEqualTo("");
    assertThat(c.handleInternalUnauthorized().getBody().get("scope")).isEqualTo("internal");
  }
}
