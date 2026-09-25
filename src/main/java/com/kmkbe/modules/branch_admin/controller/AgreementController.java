package com.kmkbe.modules.branch_admin.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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

  @Transactional
  @PostMapping(
    value = "/upload/contract",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  public CommonResult<Object> uploadContact(
    @Valid @RequestParam("financingHdrCode") String financingHdrCode,
    @Valid @RequestPart MultipartFile file

  ) throws Exception {
    FinancingHdr financingHdr = financingHdrService.findByCode(financingHdrCode);

    if (financingHdr.getFinancingStep().equals("GOLIVE")) {
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Status sudah GOLIVE");
    }

    agreementService.validateInvoicesForContractUpload(financingHdr.getFinancingHdrCode());

    Agreement agreement = agreementService.findByFinancingHdr(financingHdr);
    if (agreement == null) {
      throw new IllegalStateException("Agreement Not Found with given argument");
    }

    agreementService.upload(
      currentUserService.internalUser(),
      file,
      agreement.getAgreementCode(),
      String.valueOf(financingHdr.getBouwheer().getBouwheerCode())
    );


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

    financingHdr.setFinancingStatus(financingHdr.getFinancingStatus().equalsIgnoreCase("LIVE")?financingHdr.getFinancingStatus():"INPROCESS");
    financingHdr.setFinancingStep(financingHdr.getFinancingStep().equalsIgnoreCase("GOLIVE")?financingHdr.getFinancingStep():"SIGNED");
    financingHdr.setUsrUpd(currentUserService.usernameOrDefault(AppConstants.CREATOR));
    financingHdr.setDtmUpd(LocalDateTime.now());
    financingHdrRepository.save(financingHdr);

    /**
     * Save Agreement Signing Manual
     */

    agreementFileSigningService.saveSigningResult(
      agreement.getAgreementCode(),
      "-",
      currentUserService.internalUsername(),
      financingHdrCode,
      "SIGN_DOC"
    );

    /**
     * Send email to bouhweer
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
