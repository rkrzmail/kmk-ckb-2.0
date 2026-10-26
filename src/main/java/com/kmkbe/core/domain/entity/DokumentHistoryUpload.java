package com.kmkbe.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "dokument_history_upload")
public class DokumentHistoryUpload {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "financing_hdr_code", nullable = false)
  private FinancingHdr financingHdr;

  @Column(name = "file_type_code", nullable = false, length = 20)
  private String fileTypeCode;

  @Column(name = "file_path", nullable = false, length = 8000)
  private String filePath;

  @Column(name = "file_name", nullable = false, length = 500)
  private String fileName;

  @Column(name = "content_type", nullable = false, length = 500)
  private String contentType;

  @Column(name = "expired_date")
  private LocalDate expiredDate;

  @Column(name = "notes")
  private String notes;

  @Column(name = "uploaded_by", nullable = false, length = 50)
  private String uploadedBy;

  @Column(name = "uploaded_at", nullable = false)
  private LocalDateTime uploadedAt;
}
