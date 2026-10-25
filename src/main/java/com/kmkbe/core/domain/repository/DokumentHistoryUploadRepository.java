package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.DokumentHistoryUpload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DokumentHistoryUploadRepository extends JpaRepository<DokumentHistoryUpload, Long> {
  Optional<DokumentHistoryUpload> findTopByFinancingHdr_FinancingHdrCodeAndFileType_FileTypeCodeOrderByUploadedAtDescIdDesc(
    UUID financingHdrCode, String fileTypeCode);

  Optional<DokumentHistoryUpload> findByIdAndFinancingHdr_FinancingHdrCodeAndFileType_FileTypeCode(
    Long id, UUID financingHdrCode, String fileTypeCode);

  boolean existsByFileType_FileTypeCode(String fileTypeCode);
}
