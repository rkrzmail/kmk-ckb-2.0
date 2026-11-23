package com.kmkbe.core.callback;

import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.repository.ErrorLogRepository;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.exception.IllegalApiKeyException;
import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.PropertyValueException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;
import java.security.SignatureException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExceptionAdviceTest {
  @Mock ErrorLogRepository errorLogRepository;
  @Mock WebRequest webRequest;
  private ExceptionAdvice advice;

  @BeforeEach void setUp() { advice = new ExceptionAdvice(errorLogRepository); when(webRequest.getParameterMap()).thenReturn(Collections.emptyMap()); when(webRequest.getDescription(false)).thenReturn("uri=/test"); }

  @Test void handleCommonInvalid() {
    var ex = CommonInvalidException.invalidEmail();
    var res = advice.handleCommonInvalid(ex);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(res.getBody().getMessage()).isEqualTo("Email is invalid");
  }

  @Test void handleGenericSavesLog() {
    when(errorLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    var res = advice.handle(new RuntimeException("boom"), webRequest);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    verify(errorLogRepository).save(any());
  }

  @Test void handleGenericNullRequest() {
    when(errorLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    var res = advice.handle(new RuntimeException("boom"), null);
    assertThat(res).isNotNull();
  }

  @Test void delegateHandlers() {
    assertThat(advice.handleBadCredentials(new BadCredentialsException("b"), webRequest).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(advice.handleAccountStatus(new AccountExpiredException("a"), webRequest).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(advice.handleAccessDenied(new AccessDeniedException("a"), webRequest).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(advice.handleSignature(new SignatureException("s"), webRequest).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(advice.handleExpiredJwt(new ExpiredJwtException(null, null, "e"), webRequest).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(advice.handleJwt(new JwtException("j"), webRequest).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(advice.handleEntityNotFound(new EntityNotFoundException("n"), webRequest).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(advice.handleIllegalState(new IllegalStateException("i"), webRequest).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(advice.handleIllegalArgument(new IllegalArgumentException("i"), webRequest).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(advice.handleHttpServerError(new HttpServerErrorException(HttpStatus.BAD_GATEWAY), webRequest).getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(advice.handleNoHandlerFound(new NoHandlerFoundException("GET","/x",null), webRequest).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(advice.handleHttpClientError(new HttpClientErrorException(HttpStatus.BAD_REQUEST), webRequest).getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(advice.handleMessaging(new MessagingException("m"), webRequest).getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test void handlePropertyValue() {
    var ex = new PropertyValueException("msg","ent","prop");
    assertThat(advice.handlePropertyValue(ex, webRequest).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test void handleMethodArgumentNotValid() throws Exception {
    var target = new Object();
    var br = new BeanPropertyBindingResult(target, "obj");
    br.addError(new FieldError("obj","field","must not be blank"));
    var ex = new MethodArgumentNotValidException(null, br);
    var res = advice.handleMethodArgumentNotValid(ex, webRequest);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(res.getBody().getMessage()).isEqualTo("Validation Failed");
  }

  @Test void handleIllegalApiAndFeign() {
    assertThat(advice.handleIllegalApi(new IllegalApiKeyException(), webRequest).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    Request req = Request.create(Request.HttpMethod.GET, "http://x", Collections.emptyMap(), null, java.nio.charset.StandardCharsets.UTF_8, null);
    when(errorLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    assertThat(advice.handleRetryable(new RetryableException(500,"m",Request.HttpMethod.GET,(Throwable)null,1L,req), webRequest).getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
    assertThat(advice.handleFeign(new FeignException.BadRequest("b",req,null,null), webRequest).getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
  }
}
