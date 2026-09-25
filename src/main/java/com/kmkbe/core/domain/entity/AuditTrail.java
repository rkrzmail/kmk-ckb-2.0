package com.kmkbe.core.domain.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.kmkbe.core.domain.constant.AuditAction;
import com.kmkbe.core.domain.constant.AuditActorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "audit_trail")
public class AuditTrail {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "audit_trail_id", nullable = false)
  private Long auditTrailId;

  @Column(name = "trace_id", length = 100)
  private String traceId;

  @Enumerated(EnumType.STRING)
  @Column(name = "actor_type", length = 30)
  private AuditActorType actorType;

  @Column(name = "actor_username", length = 100)
  private String actorUsername;

  @Column(name = "actor_id", length = 100)
  private String actorId;

  @Column(name = "source_ip", length = 100)
  private String sourceIp;

  @Column(name = "user_agent")
  private String userAgent;

  @Column(name = "module_name", length = 100)
  private String moduleName;

  @Enumerated(EnumType.STRING)
  @Column(name = "action", length = 50)
  private AuditAction action;

  @Column(name = "entity_name", length = 100)
  private String entityName;

  @Column(name = "entity_id", length = 100)
  private String entityId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "before_data", columnDefinition = "jsonb")
  private JsonNode beforeData;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "after_data", columnDefinition = "jsonb")
  private JsonNode afterData;

  @Column(name = "request_path", length = 300)
  private String requestPath;

  @Column(name = "http_method", length = 20)
  private String httpMethod;

  @Column(name = "response_status")
  private Integer responseStatus;

  @Column(name = "success")
  private Boolean success;

  @Column(name = "error_message")
  private String errorMessage;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
