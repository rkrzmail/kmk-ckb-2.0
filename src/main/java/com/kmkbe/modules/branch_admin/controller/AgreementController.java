package com.kmkbe.modules.branch_admin.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.core.domain.constant.FinancingStatus;
import com.kmkbe.core.domain.dto.AgreementDto;
import com.kmkbe.core.domain.dto.InquiryAgreementDto;
import com.kmkbe.core.domain.entity.*;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.helpers.base.BasePaginationRequest;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.helpers.constant.ErrorConstant;
import com.kmkbe.modules.branch_admin.request.CreateInquiryAgreementRequest;
import com.kmkbe.modules.branch_admin.service.AgreementFileSigningService;
import com.kmkbe.modules.branch_admin.service.AgreementService;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.remote.request.UpdateFinancingStatusRequest;
import com.kmkbe.modules.remote.service.FinancingRemoteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.SignatureException;
import java.time.LocalDateTime;

@Validated
@RestController
@RequestMapping("/api/v1/cwr/agreement")
@Tag(
  name = "Persetujuan Kredit Endpoints",
  description = "Berisi endpoints data persetujuan/kelayakan kredit debitur"
)
@RequiredArgsConstructor
public class AgreementController {
  private final AgreementService agreementService;
  private final FinancingRemoteService financingRemoteService;
  private final FinancingHdrService financingHdrService;
  private final FinancingHdrRepository financingHdrRepository;
  private final CurrentUserService currentUserService;
  private final AgreementFileSigningService agreementFileSigningService;

  @GetMapping("/list/{cwrCode}/{financingHdrCode}")
  public CommonResult<PaginationResult<AgreementDto>> getCwrDisbursement(
    @PathVariable("cwrCode") String cwrCode,
    @PathVariable("financingHdrCode") String financingHdrCode,
    BasePaginationRequest request
  ) throws JsonProcessingException, SignatureException {

    currentUserService.authenticatedInternalUser();
    return new CommonResult<PaginationResult<AgreementDto>>().success(
      agreementService.list(
        cwrCode,
        financingHdrCode,
        request
      )
    );
  }

  @GetMapping("/inquiry")
  public CommonResult<InquiryAgreementDto> getInquiryAgreement(
    @RequestParam("agreementNo") String agreementNo,
    String cwrCode
  ) throws JsonProcessingException, SignatureException {
    currentUserService.authenticatedInternalUser();
    return new CommonResult<InquiryAgreementDto>().success(
      agreementService.inquiryAgreementCwr(
        cwrCode,
        agreementNo
      )
    );
  }

  @PostMapping("/inquiry/create")
  public CommonResult<Object> createInquiryAgreement(
    @Valid @RequestBody CreateInquiryAgreementRequest request
  ) throws Exception {
    agreementService.createInquiryAgreement(currentUserService.internalUser(), request);
    return new CommonResult<>().success(
      null
    );
  }

  @PostMapping(
    value = "/upload/contract",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  public CommonResult<Object> uploadContact(
    @Valid @RequestParam("financingHdrCode") String financingHdrCode,
    @Valid @RequestPart MultipartFile file

  ) throws Exception {
    FinancingHdr financingHdr = financingHdrService.findByCode(financingHdrCode);

    if (financingHdr.getFinancingStatus().equals("LIVE")) {
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Status sudah GOLIVE");
    }

    if (financingHdr.getFinancingStep().equals("GOLIVE")) {
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Status sudah GOLIVE");
    }

    agreementService.validateInvoicesForContractUpload(financingHdr.getFinancingHdrCode());

    Agreement agreement = agreementService.findByFinancingHdr(financingHdr);
    if (agreement == null) {
      throw new IllegalStateException("Agreement Not Found with given argument");
    }

    String agreementFile;
    try {
      agreementFile = agreementService.upload(
        currentUserService.internalUser(),
        file,
        agreement.getAgreementCode(),
        String.valueOf(financingHdr.getBouwheer().getBouwheerCode())
      );
    } catch (DataIntegrityViolationException exception) {
      for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
        if (cause instanceof ConstraintViolationException violation
          && "agreement_file_agreement_code_idx".equals(violation.getConstraintName())) {
          throw new BusinessException(HttpStatus.CONFLICT, HttpStatus.CONFLICT.value(),
            "Kontrak untuk No. Perjanjian " + agreement.getAgreementCode()
              + " gagal diunggah karena data dokumennya bentrok saat diperbarui. "
              + "Minta administrator memeriksa ID dokumen yang duplikat pada tabel agreement_file. "
              + "Status pengajuan tidak berubah dan email notifikasi tidak diproses.");
        }
      }
      throw new BusinessException(HttpStatus.CONFLICT, HttpStatus.CONFLICT.value(),
        "Upload kontrak gagal karena data dokumen perjanjian bermasalah. Status pengajuan tidak berubah dan email notifikasi tidak diproses. Hubungi administrator.");
    }


    // Update financing status
    /**
     * For TU
     */
//    final UpdateFinancingStatusRequest updateFinancingStatusRequest = UpdateFinancingStatusRequest.builder()
//      .financingCode(financingHdrCode)
//      .status(UpdateFinancingStatusRequest.Status.Approved)
//      .vendorCode(financingHdr.getCustomer().getCustExternalCode())
//      .build();

//    financingRemoteService.updateFinancingStatus(
//      updateFinancingStatusRequest
//    );

    /**
     * Save Agreement Signing Manual
     */
    agreementFileSigningService.saveSigningResult(
      agreement.getAgreementCode(),
      agreementFile,
      currentUserService.internalUsername(),
      financingHdrCode,
      "SIGN_DOC"
    );

    financingHdr.setFinancingStatus(FinancingStatus.IN_PROCESS.getValue());
    financingHdr.setFinancingStep(FinancingStatus.SIGNED.getValue());
    financingHdr.setUsrUpd(currentUserService.usernameOrDefault(AppConstants.CREATOR));
    financingHdr.setDtmUpd(LocalDateTime.now());
    financingHdrRepository.save(financingHdr);

    /**
     * Send email to bouwheer
     */
    agreementService.sendBouwheerPaymentNotification(financingHdr);

    /**
     * Send email to debtor
     */
    agreementService.sendDebtorDisbursementNotification(financingHdr);
    return new CommonResult<>().success(
      null
    );
  }
}
