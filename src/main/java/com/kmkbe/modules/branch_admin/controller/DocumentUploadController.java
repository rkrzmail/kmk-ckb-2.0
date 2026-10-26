package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.modules.branch_admin.response.DocumentUploadResponse;
import com.kmkbe.modules.branch_admin.service.DocumentUploadService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.SignatureException;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/document-uploads/{financingHdrCode}/{fileTypeCode}")
@RequiredArgsConstructor
public class DocumentUploadController {
  private final DocumentUploadService service;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public CommonResult<DocumentUploadResponse> upload(@PathVariable UUID financingHdrCode,
      @PathVariable String fileTypeCode,
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "expiredDate", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiredDate,
      @RequestParam(value = "notes", required = false) String notes) throws Exception {
    return new CommonResult<DocumentUploadResponse>().success(
      service.upload(financingHdrCode, fileTypeCode, file, expiredDate, notes));
  }

  @GetMapping("/latest")
  public CommonResult<DocumentUploadResponse> latest(@PathVariable UUID financingHdrCode,
      @PathVariable String fileTypeCode) throws SignatureException {
    return new CommonResult<DocumentUploadResponse>().success(service.latest(financingHdrCode, fileTypeCode));
  }

  @GetMapping("/latest/download")
  public ResponseEntity<Resource> downloadLatest(@PathVariable UUID financingHdrCode,
      @PathVariable String fileTypeCode, HttpServletRequest request) throws SignatureException {
    return service.downloadLatest(financingHdrCode, fileTypeCode, request);
  }

  @GetMapping("/{id}/download")
  public ResponseEntity<Resource> download(@PathVariable UUID financingHdrCode,
      @PathVariable String fileTypeCode, @PathVariable Long id,
      HttpServletRequest request) throws SignatureException {
    return service.download(financingHdrCode, fileTypeCode, id, request);
  }
}
