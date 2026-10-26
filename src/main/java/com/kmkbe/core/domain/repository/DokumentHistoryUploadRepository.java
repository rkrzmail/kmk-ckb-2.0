package com.kmkbe.core.domain.repository;

import com.kmkbe.core.domain.entity.DokumentHistoryUpload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DokumentHistoryUploadRepository extends JpaRepository<DokumentHistoryUpload, Long> {
  Optional<DokumentHistoryUpload> findTopByFinancingHdr_FinancingHdrCodeAndFileTypeCodeOrderByUploadedAtDescIdDesc(
    UUID financingHdrCode, String fileTypeCode);

  Optional<DokumentHistoryUpload> findByIdAndFinancingHdr_FinancingHdrCodeAndFileTypeCode(
    Long id, UUID financingHdrCode, String fileTypeCode);

  List<DokumentHistoryUpload> findByFinancingHdr_FinancingHdrCodeAndFileTypeCodeOrderByUploadedAtDescIdDesc(
    UUID financingHdrCode, String fileTypeCode);

  boolean existsByFileTypeCode(String fileTypeCode);
}
