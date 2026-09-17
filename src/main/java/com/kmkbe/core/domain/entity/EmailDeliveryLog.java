package com.kmkbe.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "email_delivery_log", schema = "public")
public class EmailDeliveryLog {
  public enum Status { PENDING, SENT, FAILED }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "email_delivery_id")
  private Long emailDeliveryId;

  @Column(name = "customer_code", nullable = false)
  private UUID customerCode;

  @Column(name = "template_code", nullable = false, length = 50)
  private String templateCode;

  @Column(name = "recipient", nullable = false, length = 1000)
  private String recipient;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private Status status;

  @Column(name = "attempt_count", nullable = false)
  private int attemptCount;

  @Column(name = "error_message", length = 2000)
  private String errorMessage;

  @Column(name = "requested_at", nullable = false)
  private LocalDateTime requestedAt;

  @Column(name = "sent_at")
  private LocalDateTime sentAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
