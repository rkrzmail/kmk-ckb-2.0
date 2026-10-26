package com.kmkbe.modules.branch_admin.service;

import com.kmkbe.core.domain.entity.DokumentHistoryUpload;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.entity.LegalFile;
import com.kmkbe.core.domain.entity.MstFileType;
import com.kmkbe.core.domain.repository.DokumentHistoryUploadRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.domain.repository.LegalFileRepository;
import com.kmkbe.core.domain.repository.MstFileTypeRepository;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.core.service.FileStorageService;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.modules.branch_admin.response.DocumentUploadResponse;
import com.kmkbe.modules.user.entity.MstUser;
import com.kmkbe.modules.user.repository.MstAppRoleFormUserRepository;
import com.kmkbe.modules.user.repository.MstUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.SignatureException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentUploadService {
  private static final String SIT_FILE_TYPE = "DOC006";

  private final CurrentUserService currentUserService;
  private final MstAppRoleFormUserRepository roleRepository;
  private final MstUserRepository userRepository;
  private final FinancingHdrRepository financingHdrRepository;
  private final MstFileTypeRepository fileTypeRepository;
  private final DokumentHistoryUploadRepository uploadRepository;
  private final LegalFileRepository legalFileRepository;
  private final FileStorageService fileStorageService;

  @Transactional
  public DocumentUploadResponse upload(UUID financingHdrCode, String fileTypeCode,
      MultipartFile file, LocalDate expiredDate, String notes)
    throws Exception {
    FinancingHdr hdr = authorizedHdr(financingHdrCode);
    validateFileTypeCode(fileTypeCode);
    MstFileType fileType = fileTypeRepository.findByFileTypeCodeAndBouwheerCode(
      fileTypeCode, hdr.getBouwheer().getBouwheerCode())
      .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Jenis dokumen tidak tersedia untuk pemberi kerja ini."));
    validate(file, fileType, expiredDate, notes);

    String originalName = file.getOriginalFilename().replace('\\', '/');
    String safeName = originalName.substring(originalName.lastIndexOf('/') + 1);
    String path = fileStorageService.save(file, hdr.getCustomer().getCustCode()
      + "/documents/" + fileTypeCode + "/" + financingHdrCode, safeName, "pdf");
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCompletion(int status) {
          if (status != STATUS_COMMITTED) {
            fileStorageService.delete(path, "");
          }
        }
      });
    }
    try {
      String username = currentUserService.internalUsername();
      LocalDateTime uploadedAt = LocalDateTime.now();
      if (SIT_FILE_TYPE.equals(fileTypeCode)) {
        LegalFile activeFile = legalFileRepository
          .findByFinancingHdr_FinancingHdrCodeAndFileTypeCode_FileTypeCode(financingHdrCode, fileTypeCode)
          .orElseGet(() -> {
            LegalFile created = new LegalFile();
            created.setCustCode(hdr.getCustomer());
            created.setFileTypeCode(fileType);
            created.setFinancingHdr(hdr);
            created.setUsrCrt(username);
            created.setDtmCrt(uploadedAt);
            return created;
          });
        activeFile.setFilePath(path);
        activeFile.setFileName(safeName);
        activeFile.setContentType("application/pdf");
        activeFile.setUsrUpd(username);
        activeFile.setDtmUpd(uploadedAt);
        legalFileRepository.saveAndFlush(activeFile);
      }
      DokumentHistoryUpload upload = new DokumentHistoryUpload();
      upload.setFinancingHdr(hdr);
      upload.setFileType(fileType);
      upload.setFilePath(path);
      upload.setFileName(safeName);
      upload.setContentType("application/pdf");
      upload.setExpiredDate(expiredDate);
      upload.setNotes(notes);
      upload.setUploadedBy(username);
      upload.setUploadedAt(uploadedAt);
      return DocumentUploadResponse.from(uploadRepository.saveAndFlush(upload));
    } catch (Exception exception) {
      if (!TransactionSynchronizationManager.isSynchronizationActive()) {
        fileStorageService.delete(path, "");
      }
      throw exception;
    }
  }

  @Transactional(readOnly = true)
  public DocumentUploadResponse latest(UUID financingHdrCode, String fileTypeCode) throws SignatureException {
    authorizedHdr(financingHdrCode);
    validateFileTypeCode(fileTypeCode);
    return uploadRepository
      .findTopByFinancingHdr_FinancingHdrCodeAndFileType_FileTypeCodeOrderByUploadedAtDescIdDesc(
        financingHdrCode, fileTypeCode)
      .map(DocumentUploadResponse::from).orElse(null);
  }

  @Transactional(readOnly = true)
  public List<DocumentUploadResponse> history(UUID financingHdrCode, String fileTypeCode) throws SignatureException {
    authorizedHdr(financingHdrCode);
    validateFileTypeCode(fileTypeCode);
    return uploadRepository
      .findByFinancingHdr_FinancingHdrCodeAndFileType_FileTypeCodeOrderByUploadedAtDescIdDesc(
        financingHdrCode, fileTypeCode)
      .stream().map(DocumentUploadResponse::from).toList();
  }

  @Transactional(readOnly = true)
  public ResponseEntity<Resource> download(UUID financingHdrCode, String fileTypeCode,
      Long id, HttpServletRequest request)
    throws SignatureException {
    authorizedHdr(financingHdrCode);
    validateFileTypeCode(fileTypeCode);
    DokumentHistoryUpload upload = uploadRepository
      .findByIdAndFinancingHdr_FinancingHdrCodeAndFileType_FileTypeCode(id, financingHdrCode, fileTypeCode)
      .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Dokumen tidak ditemukan."));
    return fileStorageService.downloadUploadFile(request, upload.getFilePath(), upload.getFileName(),
      request.getParameter("cd"));
  }

  @Transactional(readOnly = true)
  public ResponseEntity<Resource> downloadLatest(UUID financingHdrCode, String fileTypeCode,
      HttpServletRequest request) throws SignatureException {
    authorizedHdr(financingHdrCode);
    validateFileTypeCode(fileTypeCode);
    if (SIT_FILE_TYPE.equals(fileTypeCode)) {
      LegalFile active = legalFileRepository
        .findByFinancingHdr_FinancingHdrCodeAndFileTypeCode_FileTypeCode(financingHdrCode, fileTypeCode)
        .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Dokumen aktif tidak ditemukan."));
      return fileStorageService.downloadUploadFile(request, active.getFilePath(), active.getFileName(),
        request.getParameter("cd"));
    }
    DokumentHistoryUpload latest = uploadRepository
      .findTopByFinancingHdr_FinancingHdrCodeAndFileType_FileTypeCodeOrderByUploadedAtDescIdDesc(
        financingHdrCode, fileTypeCode)
      .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Dokumen tidak ditemukan."));
    return fileStorageService.downloadUploadFile(request, latest.getFilePath(), latest.getFileName(),
      request.getParameter("cd"));
  }

  private FinancingHdr authorizedHdr(UUID financingHdrCode) throws SignatureException {
    MstUser user = currentUserService.internalUser();
    if (!roleRepository.hasActiveBranchAdminRole(user.getUserCode())) {
      throw error(HttpStatus.FORBIDDEN, "Akses dokumen hanya untuk admin cabang.");
    }
    FinancingHdr hdr = financingHdrRepository.findByFinancingHdrCode(financingHdrCode)
      .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Leads ID tidak ditemukan."));
    String userBranch = userRepository.findBranchCodeByUserCode(user.getUserCode())
      .orElseThrow(() -> error(HttpStatus.FORBIDDEN, "Cabang pengguna tidak ditemukan."));
    if (!userBranch.equals(hdr.getMstBranch().getBranchCode())) {
      throw error(HttpStatus.FORBIDDEN, "Leads ID bukan milik cabang pengguna.");
    }
    return hdr;
  }

  private void validate(MultipartFile file, MstFileType fileType, LocalDate expiredDate, String notes)
    throws Exception {
    if (file == null || file.isEmpty() || file.getOriginalFilename() == null
      || !file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
      throw error(HttpStatus.BAD_REQUEST, "Lampiran dokumen harus berupa PDF.");
    }
    if (SIT_FILE_TYPE.equals(fileType.getFileTypeCode()) && expiredDate == null) {
      throw error(HttpStatus.BAD_REQUEST, "Expired Date wajib diisi untuk SIT.");
    }
    if (notes != null && notes.length() > 2000) {
      throw error(HttpStatus.BAD_REQUEST, "Notes maksimal 2000 karakter.");
    }
    if (fileType.getMaxSizeMb() != null && file.getSize() > fileType.getMaxSizeMb() * 1024L * 1024L) {
      throw error(HttpStatus.BAD_REQUEST, "Ukuran lampiran melebihi batas jenis dokumen.");
    }
    byte[] signature = new byte[5];
    try (InputStream stream = file.getInputStream()) {
      if (stream.read(signature) != 5 || signature[0] != '%' || signature[1] != 'P'
        || signature[2] != 'D' || signature[3] != 'F' || signature[4] != '-') {
        throw error(HttpStatus.BAD_REQUEST, "Isi lampiran bukan PDF.");
      }
    }
  }

  private void validateFileTypeCode(String fileTypeCode) {
    if (fileTypeCode == null || !fileTypeCode.matches("[A-Za-z0-9_-]{1,20}")) {
      throw error(HttpStatus.BAD_REQUEST, "fileTypeCode tidak valid.");
    }
  }

  private static BusinessException error(HttpStatus status, String message) {
    return new BusinessException(status, status.value(), message);
  }
}
