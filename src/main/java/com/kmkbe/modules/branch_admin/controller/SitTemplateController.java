package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.modules.branch_admin.service.SitTemplateService;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.SignatureException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/report/sit")
@RequiredArgsConstructor
public class SitTemplateController {
  private final SitTemplateService sitTemplateService;
  private final CurrentUserService currentUserService;

  @GetMapping("/{financingHdrCode}/agreements")
  public CommonResult<List<String>> agreements(@PathVariable UUID financingHdrCode) throws SignatureException {
    currentUserService.authenticatedInternalUser();
    return new CommonResult<List<String>>().success(sitTemplateService.agreementCodes(financingHdrCode));
  }

  @GetMapping("/{financingHdrCode}/download")
  public ResponseEntity<byte[]> download(@PathVariable UUID financingHdrCode,
      @RequestParam(required = false) String agreementCode) throws SignatureException, JRException, IOException {
    currentUserService.authenticatedInternalUser();
    byte[] pdf = sitTemplateService.download(financingHdrCode, agreementCode);
    return ResponseEntity.ok()
      .contentType(MediaType.APPLICATION_PDF)
      .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
        .filename("Surat-Instruksi-Transfer-" + financingHdrCode + ".pdf").build().toString())
      .body(pdf);
  }
}
