package com.kmkbe.modules.branch_admin.controller;

import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.modules.branch_admin.service.CsulSignerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.security.SignatureException;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/csul-signer")
@Tag(
  name = "List table data debtor untuk signer person",
  description = "Berisi endpoints data debtor"
)
@RequiredArgsConstructor
public class CsulSignerController {
  private final CsulSignerService csulSignerService;
  private final CurrentUserService currentUserService;

  @GetMapping("/list")
  public CommonResult<List<SignerCsulDto>> getSignerCsulList() throws SignatureException {
    List<SignerCsulDto> signerCsulList = csulSignerService.signerCsulList(
      currentUserService.internalUsername()
    );
    return new CommonResult<List<SignerCsulDto>>().success(signerCsulList);
  }

  @GetMapping("/list/{id}")
  public CommonResult<SignerCsulDto> getSignerDetail(
    @PathVariable("id") Long id
  ) {
    SignerCsulDto dto = csulSignerService.detailSigner(id);
    return new CommonResult<SignerCsulDto>().success(dto);
  }

  @PostMapping("/signer")
  public CommonResult<String> createSigner(
    @RequestBody SignerCsulRequest request
  ) {

    try {
      SignerCsulRequest savedSigner = csulSignerService.createSigner(
        request,
        currentUserService.internalUsername()
      );
      return new CommonResult<String>()
        .success(savedSigner.getRegistrationMessage());
    } catch (Exception e) {
      String errorMessage = e.getMessage();
      if (errorMessage.contains("NIK sudah terdaftar")) {
        return new CommonResult<String>()
          .fail(409, errorMessage);
      }
      return new CommonResult<String>()
        .fail(400, errorMessage);
    }
  }

  @PutMapping("/signer/{id}")
  public CommonResult<String> updateSigner(
    @PathVariable("id") Long id,
    @RequestBody SignerCsulRequest request
  ) {
    try {
      SignerCsulRequest updated = csulSignerService.updateSigner(
        id,
        request,
        currentUserService.internalUsername()
      );
      return new CommonResult<String>().success(updated.getRegistrationMessage());
    } catch (Exception e) {
      return new CommonResult<String>().fail(400, "Gagal update: " + e.getMessage());
    }
  }

  @GetMapping("/get-list")
  public CommonResult<Map<String, Object>> getSignersGrouped() throws SignatureException {
    Map<String, Object> responseData = csulSignerService.getSignersGrouped(
      currentUserService.internalUsername()
    );

    return new CommonResult<Map<String, Object>>().success(responseData);
  }

  @GetMapping("/get-list/sign")
  public CommonResult<Map<String, Object>> getSignersGroupedSign() throws SignatureException {
    currentUserService.authenticatedInternalUser();
    Map<String, Object> responseData = csulSignerService.getSignersGrouped2();
    return new CommonResult<Map<String, Object>>().success(responseData);
  }

  @PutMapping("/check/{identityNo}")
  public CommonResult<String> updateSignerStatus(
    @PathVariable String identityNo
  ) {

    try {
      String message = csulSignerService.updateSignerStatus(
        identityNo,
        currentUserService.internalUsername()
      );
      return new CommonResult<String>().success(message);
    } catch (Exception e) {
      return new CommonResult<String>().fail(400, e.getMessage());
    }
  }
}
