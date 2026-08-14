package com.kmkbe.modules.apis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kmkbe.adapter.ApiCsulAdapter;
import com.kmkbe.core.domain.dto.CreatedSimulationDto;
import com.kmkbe.core.domain.dto.EstimatedDisburseDto;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.service.JwtGeneratorService;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.modules.api_sbu.model.entity.ApiSbu;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.ValidationResponse;
import com.kmkbe.modules.api_sbu.repository.ApiSbuRepository;
import com.kmkbe.core.exception.IllegalApiKeyException;
import com.kmkbe.core.service.JwtValidatorService;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.feign.model.dto.CsulInquiryInvoiceRemoteDto;
import com.kmkbe.helpers.constant.ErrorConstant;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.modules.customer.model.request.SignUpRequest;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.customer.service.CustomerCompanyService;
import com.kmkbe.modules.customer.service.CustomerService;
import com.kmkbe.modules.loan_submission.request.CalculateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.CreateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import com.kmkbe.modules.loan_submission.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SignatureException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class ApiSbuCkbService {

  private final ApiCsulAdapter apiCsulAdapter;
  private final ApiSbuRepository apiSbuRepository;
  private final JwtValidatorService jwtValidatorService;
  private final CustomerRepository customerRepository;
  private final LoanSubmissionService loanSubmissionService;
  private final BouwheerRepository bouwheerRepository;
  private final JwtGeneratorService jwtGeneratorService;
  private final FinancingService financingService;
  private final FinancingHdrService financingHdrService;
  private final FinancingDtlService financingDtlService;
  private final CustomerCompanyService customerCompanyService;
  private final DocumentService documentService;
  private final CustomerService customerService;


  public ApiSbuCkbService(ApiCsulAdapter apiCsulAdapter,
                          ApiSbuRepository apiSbuRepository,
                          JwtValidatorService jwtValidatorService,
                          CustomerRepository customerRepository,
                          LoanSubmissionService loanSubmissionService,
                          BouwheerRepository bouwheerRepository,
                          JwtGeneratorService jwtGeneratorService,
                          FinancingService financingService,
                          FinancingHdrService financingHdrService,
                          FinancingDtlService financingDtlService,
                          CustomerCompanyService customerCompanyService,
                          DocumentService documentService,
                          CustomerService customerService
  ) {
    this.apiCsulAdapter = apiCsulAdapter;
    this.apiSbuRepository = apiSbuRepository;
    this.jwtValidatorService = jwtValidatorService;
    this.customerRepository = customerRepository;
    this.loanSubmissionService = loanSubmissionService;
    this.bouwheerRepository = bouwheerRepository;
    this.jwtGeneratorService = jwtGeneratorService;
    this.financingService = financingService;
    this.financingHdrService = financingHdrService;
    this.financingDtlService = financingDtlService;
    this.customerCompanyService = customerCompanyService;
    this.documentService = documentService;
    this.customerService = customerService;
  }


  /**
   * Create Token
   *
   * @param apiKey
   * @return
   */
  public CommonResult<Map<String, Object>> createToken(String apiKey) {
    Optional<ApiSbu> apiSbu = apiSbuRepository.findByAppKey(apiKey);
    if (apiSbu.isEmpty()) {
      log.info(ErrorConstant.ERROR_MESSAGE_80 + "{}", apiKey);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Invalid Bouwheer API-KEY");
    }

    CommonResult<Map<String, Object>> result;
    String strSecret = apiSbu.get().getAppSecret();
    String bouwheerCode = apiSbu.get().getBouwheerCode().toString();

    Date now = new Date();
    Date expireDate = new Date(now.getTime() + (30 * 60 * 1000)); // now + 30 menit

    Map<String, Object> response;
    try {
      String token = jwtGeneratorService.generateToken(
        apiKey,
        strSecret,
        bouwheerCode,
        expireDate
      );

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String expireDateFormatted = sdf.format(expireDate);

      response = new HashMap<>();
      response.put("token", token);
      response.put("bouwheer", bouwheerCode);
      response.put("expired_at", expireDateFormatted);

      result = new CommonResult<Map<String, Object>>().success(response);
    } catch (IllegalArgumentException e) {
      result = new CommonResult<Map<String, Object>>().fail(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
    }

    return result;
  }

  /**
   *
   * @param apiKey
   * @param jwtToken
   * @return
   */
  public ValidationResponse apiValidation(String apiKey, String jwtToken) {
    Optional<ApiSbu> apiSbu = apiSbuRepository.findByAppKey(apiKey);
    if (apiSbu.isEmpty()) {
      log.info(ErrorConstant.ERROR_MESSAGE_80 + "{}", apiKey);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Invalid Bouwheer API-KEY");
    }

    ValidationResponse validationResponse = jwtValidatorService.validate(apiKey, jwtToken, apiSbu.get());
    String bouwheerCode = apiSbu.get().getBouwheerCode().toString();
    if (!validationResponse.getBouwheer().equalsIgnoreCase(bouwheerCode)) {
      log.info(ErrorConstant.ERROR_MESSAGE_80 + "{}", bouwheerCode);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Invalid Bouwheer Code");
    }
    if (validationResponse.getExp() < (System.currentTimeMillis() / 1000)) {
      log.info(ErrorConstant.ERROR_MESSAGE_80 + "{}", bouwheerCode);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Expired Token");
    }
    return validationResponse;
  }

  /**
   *
   * @return
   * @throws Exception
   */
  public CommonResult<CsulInquiryInvoiceRemoteDto> inquiryListPostedInvoice(String vendorCode) {
    CsulInquiryInvoiceRemoteDto inquiryInvoice = apiCsulAdapter.findListPostedInvoice(vendorCode);
    return new CommonResult<CsulInquiryInvoiceRemoteDto>().success(
      inquiryInvoice
    );
  }

  /**
   * Submission
   *
   * @param request
   * @return
   * @throws Exception
   */
  public CommonResult<CreatedSimulationDto> submission(CreateSimulationRequest request) throws Exception {
    /**
     * Check Bouwheer Code
     */
    UUID bouwheerUuid = UUID.fromString(request.getBouwheerCode());
    Optional<Bouwheer> bouwheerOptional = bouwheerRepository.findByBouwheerCode(bouwheerUuid);
    if (bouwheerOptional.isEmpty()) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", request.getBouwheerCode());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_81, "Bouwheer not found");
    }

    Customer customer;
    Optional<Customer> customerOptional = customerRepository.findByBouwheerAndCustExternalCode(request.getBouwheerCode(), request.getVendorCode());
    if (customerOptional.isPresent()) {
      customer = customerOptional.get();
    } else {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "Create new {}", request.getBouwheerCode());

      customer = customerRepository.save(Customer.builder()
        .custCode(UUID.randomUUID())
        .bouwheer(request.getBouwheerCode())
        .custExternalCode(request.getVendorCode())
        .custName("Customer - " + bouwheerOptional.get().getBouwheerName())
        .custIdNo(request.getVendorCode())
        .custMobilePhone(request.getVendorCode())
        .custEmail("tmp."+request.getVendorCode()+"danasakti.com")
        .isEmailValid(false)
        .approvalStatus("OPEN")
        .isPhoneValid(false)
        .isWaActive(false)
        .agreeTc(true)
        .agreeLegalShare(true)
        .isActive(false)
        .usrCrt(AppConstants.CREATOR)
        .custIdTypeCode("NPWP")
        .custTypeCode("Company")
        .dtmCrt(LocalDateTime.now())
        .build());
      /**
       * Insert temp customer company
       */
      customerCompanyService.create(customer, SignUpRequest.Company.builder()
        .identityType("NPWP")
        .identityNo(request.getVendorCode())
        .build());
    }

    var result = loanSubmissionService.createSimulation(customer, request);
    return new CommonResult<CreatedSimulationDto>().success(
      result
    );
  }

  /**
   * Calculate Disburse
   *
   * @param request
   * @return
   * @throws SignatureException
   * @throws ParseException
   * @throws JsonProcessingException
   */
  public CommonResult<EstimatedDisburseDto> simulation(CalculateSimulationRequest request) throws SignatureException, ParseException, JsonProcessingException {
    /**
     * Check Bouwheer Code
     */
    Optional<Customer> customerOptional = customerRepository.findByBouwheer(request.getBouwheerCode());
    if (customerOptional.isEmpty()) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", request.getBouwheerCode());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Customer Bouwheer not found");
    }

    Customer customer = customerOptional.get();
    return new CommonResult<EstimatedDisburseDto>().success(
      loanSubmissionService.calculateDisburse(customer, request)
    );
  }

  /**
   * Update Approval
   *
   * @param apiKey
   * @return
   */
  public CommonResult<Object> approval(String apiKey) {
/**
 * Check Bouwheer Code
 */

    Optional<ApiSbu> apiSbu = apiSbuRepository.findByAppKey(apiKey);
    if (apiSbu.isEmpty()) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", apiSbu);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "API SBU not found");
    }

    financingService.recallApprovalStatus();

    return new CommonResult<>().success(null);
  }

  /**
   * Update Invoice PAID
   *
   * @param apiKey
   * @param request
   * @return
   * @throws Exception
   */
  public CommonResult<Object> invoicePaid(String apiKey, FinancingInvoicePaidRequest request) throws Exception {
    /**
     * Check Bouwheer Code
     */
    Optional<ApiSbu> apiSbu = apiSbuRepository.findByAppKey(apiKey);
    if (apiSbu.isEmpty()) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", apiSbu);
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "API SBU not found");
    }

    FinancingHdr financingHdr = financingHdrService.paidFinancing(
      request,
      apiKey
    );

    log.info("Financing Financing Header Code {} ,  Step {} , Status {}, ",request.getFinancingCode(),financingHdr.getFinancingStep(),financingHdr.getFinancingStatus());
    financingDtlService.updatePaid(request, financingHdr);

    try {
      financingDtlService.paymentReceive(request, financingHdr);
    } catch (Exception ignored) {
      //akan ada proses skeduler
    }
    return new CommonResult<>().success(null);
  }

  /**
   * Inquery Disbursement
   *
   * @param apiKey
   * @param request
   * @return
   * @throws SignatureException
   */
  public CommonResult<Object> inquiryDisburse(String apiKey, FinancingInvoicePaidRequest request) throws SignatureException {
    /**
     * Check Bouwheer Code
     */
    Optional<ApiSbu> apiSbu = apiSbuRepository.findByAppKey(apiKey);
    if (apiSbu.isEmpty()) {
      throw new IllegalApiKeyException();
    }

    financingHdrService.paidFinancing(
      request,
      apiKey
    );

    return new CommonResult<>().success(null);
  }
}
