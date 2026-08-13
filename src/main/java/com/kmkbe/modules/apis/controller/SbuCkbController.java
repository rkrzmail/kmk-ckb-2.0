package com.kmkbe.modules.apis.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.feign.model.dto.CsulInquiryInvoiceRemoteDto;
import com.kmkbe.helpers.constant.ErrorConstant;
import com.kmkbe.modules.apis.service.ApiSbuCkbService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.modules.api_sbu.model.entity.ApiSbu;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.ValidationResponse;
import com.kmkbe.modules.api_sbu.repository.ApiSbuRepository;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.core.exception.IllegalApiKeyException;
import com.kmkbe.core.service.*;
import com.kmkbe.modules.loan_submission.request.CalculateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.CreateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import com.kmkbe.modules.loan_submission.service.FinancingDtlService;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.loan_submission.service.FinancingService;
import com.kmkbe.modules.loan_submission.service.LoanSubmissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.SignatureException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/sbu/ckb")//sbu
@Tag(
  name = "/api/v1/sbu/ckb",
  description = ""
)
public class SbuCkbController {
  private final FinancingHdrService financingHdrService;

  private final FinancingService financingService;
  private final FinancingDtlService financingDtlService;
  private final ApiSbuRepository apiSbuRepository;
  private final JwtGeneratorService jwtGeneratorService;
  private final ObjectMapper objectMapper;
  private final LoanSubmissionService loanSubmissionService;
  private final CustomerRepository customerRepository;
  private final ApiSbuCkbService apiSbuCkbService;

  public SbuCkbController(FinancingHdrService financingHdrService,
                          FinancingService financingService,
                          FinancingDtlService financingDtlService,
                          ApiSbuRepository apiSbuRepository,
                          JwtGeneratorService jwtGeneratorService,
                          ObjectMapper objectMapper,
                          LoanSubmissionService loanSubmissionService,
                          CustomerRepository customerRepository,
                          ApiSbuCkbService apiSbuCkbService
  ) {
    this.financingHdrService = financingHdrService;
    this.financingService = financingService;
    this.financingDtlService = financingDtlService;
    this.apiSbuRepository = apiSbuRepository;
    this.jwtGeneratorService = jwtGeneratorService;
    this.objectMapper = objectMapper;
    this.loanSubmissionService = loanSubmissionService;
    this.customerRepository = customerRepository;
    this.apiSbuCkbService = apiSbuCkbService;
  }

  @PostMapping(value = "/test/{jwtToken}", consumes = MediaType.ALL_VALUE)
  public CommonResult<Object> test(
    @PathVariable("jwtToken") String jwtToken,
    @RequestHeader("ApiKey") String apiKey,
    @RequestBody(required = false) Object rawBody) throws IOException {
    ValidationResponse validationResponse = apiSbuCkbService.apiValidation(apiKey, jwtToken);

    return new CommonResult<Object>().success(rawBody);

  }

  @GetMapping(value = "/request/token", consumes = MediaType.ALL_VALUE)
  public CommonResult<Map<String, Object>> test(
    @RequestHeader("ApiKey") String apiKey) throws IOException {
    Optional<ApiSbu> apiSbu = apiSbuRepository.findByAppKey(apiKey);
    if (apiSbu.isEmpty()) {
      throw new IllegalApiKeyException();
    }
    CommonResult<Map<String, Object>> result;
    String strSecret = apiSbu.get().getAppSecret();
    String bouwheerCode = apiSbu.get().getBouwheerCode().toString();

    Date now = new Date();
    Date expireDate = new Date(now.getTime() + (10 * 60 * 1000)); // now + 10 menit

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

  @PostMapping(value = "/simulations/calculate/{jwtToken}")
  public CommonResult<EstimatedDisburseDto> getCalculateDisburse(
    @PathVariable("jwtToken") String jwtToken,
    @RequestBody CalculateSimulationRequest request
  ) throws SignatureException, ParseException, JsonProcessingException {
    Optional<Customer> customerOptional = customerRepository.findByCustCode(UUID.fromString("33cade0f-4ce6-46e5-be19-258eddb7e6a6"));
    if (customerOptional.isEmpty()) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", request.getBouwheerCode());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Customer Bouwheer not found");
    }

    Customer customer = customerOptional.get();
    return new CommonResult<EstimatedDisburseDto>().success(
      loanSubmissionService.calculateDisburse(customer, request)
    );
  }

  @PostMapping(value = "/submissions/{jwtToken}")
  public CommonResult<CreatedSimulationDto> createSimulation(
    @RequestBody @Valid CreateSimulationRequest request,
    @PathVariable("jwtToken") String jwtToken,
    @RequestHeader("ApiKey") String apiKey
  ) throws Exception {
    Optional<Customer> customerOptional = customerRepository.findByCustCode(UUID.fromString("33cade0f-4ce6-46e5-be19-258eddb7e6a6"));
    if (customerOptional.isEmpty()) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", request.getBouwheerCode());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Customer Bouwheer not found");
    }

    Customer customer = customerOptional.get();
    var result = loanSubmissionService.createSimulation(customer, request);
    return new CommonResult<CreatedSimulationDto>().success(
      result
    );
  }

  @PostMapping(value = "/listpostedinvoice/{jwtToken}")
  public CommonResult<CsulInquiryInvoiceRemoteDto> getListPostedInvoice(
    @PathVariable("jwtToken") String jwtToken,
    @RequestBody(required = false) Object rawBody
  ) {
    JsonNode node = objectMapper.valueToTree(rawBody);
    String vendorCode = node.path("vendorCode").asText();
    return apiSbuCkbService.inquiryListPostedInvoice(vendorCode);
  }

  @GetMapping(value = "/financing/status/{jwtToken}") //approvals/status
  public CommonResult<Object> updateApproval(
    @PathVariable("jwtToken") String jwtToken,
    HttpServletRequest httpServletRequest
  ) {

    String apiKey = httpServletRequest.getHeader("ApiKey");
    Optional<ApiSbu> apiSbu = apiSbuRepository.findByAppKey(apiKey);
    if (apiSbu.isEmpty()) {
      throw new IllegalApiKeyException();
    }

    try {
      financingService.recallApprovalStatus();
    } catch (Exception ignored) {
      ignored.printStackTrace();
    }

    return new CommonResult<>().success(null);
  }

  @PostMapping(value = "/invoice-paid/{jwtToken}")
  public CommonResult<Object>
  invoicePaid(
    @PathVariable("jwtToken") String jwtToken,
    HttpServletRequest httpServletRequest,
    @Valid @RequestBody FinancingInvoicePaidRequest request
  ) {
    try {
      String providedApiKey = httpServletRequest.getHeader("ApiKey");
      Optional<ApiSbu> apiSbu = apiSbuRepository.findByAppKey(providedApiKey);
      if (apiSbu.isEmpty()) {
        throw new IllegalApiKeyException();
      }

      FinancingHdr financingHdr = financingHdrService.paidFinancing(
        request,
        providedApiKey
      );

      financingDtlService.updatePaid(request, financingHdr);


      try {
        financingDtlService.paymentReceive(request, financingHdr);
      } catch (Exception ignored) {
        //akan ada proses skeduler
      }
      return new CommonResult<>().success(null);
    } catch (Exception e) {
      return new CommonResult<>().fail(500, e.getMessage());
    }
  }

  @PostMapping(value = "/inquiry/disburse/{jwtToken}")
  public CommonResult<Object>
  inquiryDisburse(
    @PathVariable("jwtToken") String jwtToken,
    HttpServletRequest httpServletRequest,
    @Valid @RequestBody FinancingInvoicePaidRequest request
  ) {
    try {
      String providedApiKey = httpServletRequest.getHeader("ApiKey");
      Optional<ApiSbu> apiSbu = apiSbuRepository.findByAppKey(providedApiKey);
      if (apiSbu.isEmpty()) {
        throw new IllegalApiKeyException();
      }

      FinancingHdr financingHdr = financingHdrService.paidFinancing(
        request,
        providedApiKey
      );

      return new CommonResult<>().success(null);
    } catch (Exception e) {
      return new CommonResult<>().fail(500, e.getMessage());
    }
  }
}
