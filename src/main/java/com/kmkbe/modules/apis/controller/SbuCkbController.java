package com.kmkbe.modules.apis.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.feign.model.dto.CsulInquiryInvoiceRemoteDto;
import com.kmkbe.modules.apis.service.ApiSbuCkbService;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.modules.loan_submission.request.CalculateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.CreateSubmissionRequest;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.SignatureException;
import java.text.ParseException;
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
  private final ObjectMapper objectMapper;
  private final ApiSbuCkbService apiSbuCkbService;

  public SbuCkbController(ObjectMapper objectMapper,
                          ApiSbuCkbService apiSbuCkbService
  ) {
    this.objectMapper = objectMapper;
    this.apiSbuCkbService = apiSbuCkbService;
  }

  @GetMapping(value = "/request/token", consumes = MediaType.ALL_VALUE)
  public CommonResult<Map<String, Object>> requestToken(@RequestHeader("ApiKey") String apiKey) {
    return apiSbuCkbService.createToken(apiKey);
  }

  @PostMapping(value = "/simulations/calculate/{jwtToken}")
  public CommonResult<EstimatedDisburseDto> getCalculateDisburse(
    @PathVariable("jwtToken") String jwtToken,
    @RequestHeader("ApiKey") String apiKey,
    @RequestBody CalculateSimulationRequest request
  ) throws IOException, SignatureException, ParseException {

    /**
     * Check token Auth
     */
    apiSbuCkbService.apiValidation(apiKey, jwtToken);
    return apiSbuCkbService.simulation(request);
  }

  @PostMapping(value = "/api/v1/sbu/ckb/submissions/{jwtToken}")
  public CommonResult<CreatedSimulationDto> createSimulation(
    @RequestBody @Valid CreateSubmissionRequest request,
    @PathVariable("jwtToken") String jwtToken,
    @RequestHeader("ApiKey") String apiKey
  ) throws Exception {
    /**
     * Check token Auth
     */
    apiSbuCkbService.apiValidation(apiKey, jwtToken);

    return apiSbuCkbService.submission(request);
  }

  @PostMapping(value = "/listpostedinvoice/{jwtToken}")
  public CommonResult<CsulInquiryInvoiceRemoteDto> getListPostedInvoice(
    @PathVariable("jwtToken") String jwtToken,
    @RequestHeader("ApiKey") String apiKey,
    @RequestBody(required = false) Object rawBody
  ) {

    /**
     * Check token Auth
     */
    apiSbuCkbService.apiValidation(apiKey, jwtToken);

    /**
     * Check Bouwheer Code
     */

    JsonNode node = objectMapper.valueToTree(rawBody);
    String vendorCode = node.path("vendorCode").asText();
    return apiSbuCkbService.inquiryListPostedInvoice(vendorCode);
  }

  @GetMapping(value = "/financing/status/{jwtToken}") //approvals/status
  public CommonResult<StatusLabelDto> updateFinancingStatus(
    @PathVariable("jwtToken") String jwtToken,
    @RequestHeader("ApiKey") String apiKey, @RequestParam("financingHdrCode") String financingHdrCode
  ) {

    /**
     * Check token Auth
     */
    apiSbuCkbService.apiValidation(apiKey, jwtToken);

    return apiSbuCkbService.statusFinancing(apiKey, financingHdrCode);
  }

  @PostMapping(value = "/invoice-paid/{jwtToken}")
  public CommonResult<Object>
  invoicePaid(
    @PathVariable("jwtToken") String jwtToken,
    @RequestHeader("ApiKey") String apiKey,
    @Valid @RequestBody FinancingInvoicePaidRequest request
  ) throws Exception {

    /**
     * Check token Auth
     */
    apiSbuCkbService.apiValidation(apiKey, jwtToken);

    return apiSbuCkbService.invoicePaid(apiKey, request);
  }

  @PostMapping(value = "/inquiry/disburse/{jwtToken}")
  public CommonResult<Object>
  inquiryDisburse(
    @PathVariable("jwtToken") String jwtToken,
    @RequestHeader("ApiKey") String apiKey,
    @Valid @RequestBody FinancingInvoicePaidRequest request
  ) {
    /**
     * Check token Auth
     */
    apiSbuCkbService.apiValidation(apiKey, jwtToken);
    return apiSbuCkbService.inquiryDisburse(apiKey, request);
  }
}
