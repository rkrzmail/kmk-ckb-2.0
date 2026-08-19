package com.kmkbe.core.domain.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.kmkbe.core.domain.constant.AuditAction;
import com.kmkbe.core.domain.constant.AuditActorType;

import java.time.LocalDateTime;

public record AuditTrailEvent(
  String traceId,
  AuditActorType actorType,
  String actorUsername,
  String actorId,
  String sourceIp,
  String userAgent,
  String moduleName,
  AuditAction action,
  String entityName,
  String entityId,
  JsonNode beforeData,
  JsonNode afterData,
  String requestPath,
  String httpMethod,
  Integer responseStatus,
  Boolean success,
  String errorMessage,
  LocalDateTime createdAt
) {
}
