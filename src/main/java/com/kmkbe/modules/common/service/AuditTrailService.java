package com.kmkbe.modules.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.constant.AuditAction;
import com.kmkbe.core.domain.constant.AuditActorType;
import com.kmkbe.core.domain.event.AuditTrailEvent;
import com.kmkbe.core.utils.AuditMaskingUtils;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.user.entity.MstUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditTrailService {
  private static final String UNKNOWN = "UNKNOWN";

  private final ApplicationEventPublisher eventPublisher;
  private final ObjectMapper objectMapper;

  public void record(String moduleName,
                     AuditAction action,
                     String entityName,
                     Object entityId,
                     Object beforeData,
                     Object afterData) {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      Actor actor = resolveActor(authentication);
      HttpServletRequest request = currentRequest();

      eventPublisher.publishEvent(new AuditTrailEvent(
        traceId(request),
        actor.actorType(),
        actor.username(),
        actor.actorId(),
        sourceIp(request),
        header(request, "User-Agent"),
        moduleName,
        action,
        entityName,
        entityId != null ? String.valueOf(entityId) : null,
        toMaskedJson(beforeData),
        toMaskedJson(afterData),
        request != null ? request.getRequestURI() : null,
        request != null ? request.getMethod() : null,
        200,
        true,
        null,
        LocalDateTime.now()
      ));
    } catch (Exception e) {
      log.warn("Failed to publish audit trail for module={}, action={}, entityName={}, entityId={}: {}",
        moduleName, action, entityName, entityId, e.getMessage());
    }
  }

  public void recordAuthentication(String moduleName,
                                   AuditActorType actorType,
                                   String username,
                                   Object actorId,
                                   boolean success,
                                   String errorMessage) {
    try {
      HttpServletRequest request = currentRequest();
      String actorUsername = valueOrDefault(username);

      eventPublisher.publishEvent(new AuditTrailEvent(
        traceId(request),
        actorType != null ? actorType : AuditActorType.UNKNOWN,
        actorUsername,
        actorId != null ? String.valueOf(actorId) : null,
        sourceIp(request),
        header(request, "User-Agent"),
        moduleName,
        AuditAction.LOGIN,
        "Authentication",
        actorUsername,
        null,
        toMaskedJson(new AuthenticationAuditData(actorUsername, success)),
        request != null ? request.getRequestURI() : null,
        request != null ? request.getMethod() : null,
        success ? 200 : 401,
        success,
        errorMessage,
        LocalDateTime.now()
      ));
    } catch (Exception e) {
      log.warn("Failed to publish authentication audit trail for module={}, username={}: {}",
        moduleName, username, e.getMessage());
    }
  }

  private JsonNode toMaskedJson(Object data) {
    if (data == null) {
      return null;
    }

    return AuditMaskingUtils.mask(objectMapper.valueToTree(data));
  }

  private Actor resolveActor(Authentication authentication) {
    if (authentication == null
      || !authentication.isAuthenticated()
      || authentication instanceof AnonymousAuthenticationToken) {
      return new Actor(AuditActorType.UNKNOWN, UNKNOWN, null);
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof MstUser user) {
      return new Actor(
        AuditActorType.INTERNAL,
        valueOrDefault(user.getUsername()),
        user.getUserCode() != null ? user.getUserCode().toString() : null
      );
    }

    if (principal instanceof Customer customer) {
      return new Actor(
        AuditActorType.CUSTOMER,
        valueOrDefault(customer.getCustEmail()),
        customer.getCustCode() != null ? customer.getCustCode().toString() : null
      );
    }

    return new Actor(AuditActorType.UNKNOWN, valueOrDefault(authentication.getName()), null);
  }

  private String valueOrDefault(String value) {
    return value != null && !value.isBlank() ? value : UNKNOWN;
  }

  private HttpServletRequest currentRequest() {
    if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
      return attributes.getRequest();
    }

    return null;
  }

  private String traceId(HttpServletRequest request) {
    String traceId = header(request, "X-Request-ID");
    if (traceId == null || traceId.isBlank()) {
      traceId = header(request, "X-Correlation-ID");
    }
    return traceId != null && !traceId.isBlank() ? traceId : UUID.randomUUID().toString();
  }

  private String sourceIp(HttpServletRequest request) {
    String forwardedFor = header(request, "X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
      return forwardedFor.split(",")[0].trim();
    }
    return request != null ? request.getRemoteAddr() : null;
  }

  private String header(HttpServletRequest request, String name) {
    return request != null ? request.getHeader(name) : null;
  }

  private record Actor(AuditActorType actorType, String username, String actorId) {
  }

  private record AuthenticationAuditData(String username, boolean success) {
  }
}
