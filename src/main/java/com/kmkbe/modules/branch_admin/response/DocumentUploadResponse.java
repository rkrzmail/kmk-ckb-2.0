package com.kmkbe.modules.branch_admin.response;

import com.kmkbe.core.domain.entity.DokumentHistoryUpload;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentUploadResponse(
  Long id, UUID financingHdrCode, String fileTypeCode, String fileName,
  LocalDate expiredDate, String notes, String uploadedBy, LocalDateTime uploadedAt
) {
  public static DocumentUploadResponse from(DokumentHistoryUpload upload) {
    return new DocumentUploadResponse(upload.getId(), upload.getFinancingHdr().getFinancingHdrCode(),
      upload.getFileTypeCode(), upload.getFileName(), upload.getExpiredDate(),
      upload.getNotes(), upload.getUploadedBy(), upload.getUploadedAt());
  }
}
