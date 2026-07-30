package com.kmkbe.modules.apis.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.feign.model.dto.CsulInquiryInvoiceRemoteDto;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.helpers.constant.ErrorConstant;
import com.kmkbe.modules.apis.service.ApiSbuService;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.ApiSbu;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.ValidationResponse;
import com.kmkbe.core.domain.repository.ApiSbuRepository;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.core.domain.request.InquiryDisburseRequest;
import com.kmkbe.core.domain.response.InquiryDisburseResult;
import com.kmkbe.core.exception.IllegalApiKeyException;
import com.kmkbe.core.service.*;
import com.kmkbe.modules.loan_submission.request.CalculateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.CreateSimulationRequest;
import com.kmkbe.modules.loan_submission.request.FinancingInvoicePaidRequest;
import com.kmkbe.modules.loan_submission.service.FinancingDtlService;
import com.kmkbe.modules.loan_submission.service.FinancingHdrService;
import com.kmkbe.modules.loan_submission.service.FinancingService;
import com.kmkbe.modules.loan_submission.service.LoanSubmissionService;
import com.kmkbe.modules.remote.service.InvoiceRemoteDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.rmi.RemoteException;
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

public class SbuController {
  private final SbuRemoteService sbuRemoteService;
  private final RestTemplate restTemplate;
  private final FinancingHdrService financingHdrService;

  private final FinancingService financingService;
  private final FinancingDtlService financingDtlService;
  private final ApiSbuRepository apiSbuRepository;
  private final JwtService jwtService;
  private final JwtGeneratorService jwtGeneratorService;
  private final JwtValidatorService jwtValidatorService;
  private final ObjectMapper objectMapper;
  private final AuthenticationManager authenticationManager;
  private final LoanSubmissionService loanSubmissionService;
  private final CustomerRepository customerRepository;
  private final ApiSbuService apiSbuService;

  public SbuController(SbuRemoteService sbuRemoteService,
                       RestTemplate restTemplate,
                       FinancingHdrService financingHdrService,
                       FinancingService financingService,
                       FinancingDtlService financingDtlService,
                       ApiSbuRepository apiSbuRepository,
                       JwtService jwtService,
                       JwtGeneratorService jwtGeneratorService,
                       JwtValidatorService jwtValidatorService,
                       ObjectMapper objectMapper,
                       AuthenticationManager authenticationManager,
                       LoanSubmissionService loanSubmissionService,
                       CustomerRepository customerRepository,
                       ApiSbuService apiSbuService,
                       InvoiceRemoteDto invoiceRemoteDto) {
    this.sbuRemoteService = sbuRemoteService;
    this.restTemplate = restTemplate;
    this.financingHdrService = financingHdrService;
    this.financingService = financingService;
    this.financingDtlService = financingDtlService;
    this.apiSbuRepository = apiSbuRepository;
    this.jwtService = jwtService;
    this.jwtGeneratorService = jwtGeneratorService;
    this.jwtValidatorService = jwtValidatorService;
    this.objectMapper = objectMapper;
    this.authenticationManager = authenticationManager;
    this.loanSubmissionService = loanSubmissionService;
    this.customerRepository = customerRepository;
    this.apiSbuService = apiSbuService;
    this.invoiceRemoteDto = invoiceRemoteDto;
  }

  @PostMapping(value = "/test/{jwtToken}", consumes = MediaType.ALL_VALUE)
  public CommonResult<Object> test(
    @PathVariable("jwtToken") String jwtToken,
    @RequestHeader("ApiKey") String apiKey,
    @RequestBody(required = false) Object rawBody) throws IOException {
    ValidationResponse validationResponse = apiSbuService.apiValidation(apiKey, jwtToken);

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
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80,"Customer Bouwheer not found");
    }

    Customer customer = customerOptional.get();
    List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    Authentication authentication = new UsernamePasswordAuthenticationToken(
      customer,
      null,
      authorities
    );
    SecurityContextHolder.getContext().setAuthentication(authentication);
    return new CommonResult<EstimatedDisburseDto>().success(
      loanSubmissionService.calculateDisburse(authentication, request)
    );
  }

  @PostMapping(value = "/simulations/{jwtToken}")
  public CommonResult<CreatedSimulationDto> createSimulation(
    @RequestBody CreateSimulationRequest request,
    @PathVariable("jwtToken") String jwtToken,
    @RequestHeader("ApiKey") String apiKey
  ) throws Exception {
    Optional<Customer> customerOptional = customerRepository.findByCustCode(UUID.fromString("33cade0f-4ce6-46e5-be19-258eddb7e6a6"));
    if (customerOptional.isEmpty()) {
      log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", request.getBouwheerCode());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80,"Customer Bouwheer not found");
    }

    Customer customer = customerOptional.get();
    List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    Authentication authentication = new UsernamePasswordAuthenticationToken(
      customer,
      null,
      authorities
    );
    SecurityContextHolder.getContext().setAuthentication(authentication);
    var result = loanSubmissionService.createSimulation(authentication, request);
    return new CommonResult<CreatedSimulationDto>().success(
      result,
      "Simulation Created Successfully"
    );
  }

  private final InvoiceRemoteDto invoiceRemoteDto;

//  @PostMapping(value = "/listpostedinvoice/{jwtToken}")
//  public CommonResult<CsulInquiryInvoiceRemoteDto> listpostedinvoice(
//    @PathVariable("jwtToken") String jwtToken,
//    @RequestBody(required = false) Object rawBody
//  ) throws Exception {
//    JsonNode node = objectMapper.valueToTree(rawBody);
//    String vendorCode = node.path("vendorCode").asText();
//
//    BaseSimpleRemoteResponseDto<CsulInquiryInvoiceRemoteDto> inquiryInvoice = invoiceRemoteDto.inquiryInvoice(vendorCode);
//
//    return new CommonResult<CsulInquiryInvoiceRemoteDto>().success(
//      inquiryInvoice.getData(),
//      ""
//    );
//  }

  @PostMapping(value = "/listpostedinvoice/{jwtToken}")
  public CommonResult<CsulInquiryInvoiceRemoteDto> getListPostedInvoice(
    @PathVariable("jwtToken") String jwtToken,
    @RequestBody(required = false) Object rawBody
  ){
    JsonNode node = objectMapper.valueToTree(rawBody);
    String vendorCode = node.path("vendorCode").asText();
    return apiSbuService.inquiryListPostedInvoice(vendorCode);
  }

  @GetMapping(value = "/financing/status") //approvals/status
  public CommonResult<Object> updateApproval(
    HttpServletRequest httpServletRequest
  ) {

    String apiKey = httpServletRequest.getHeader("ApiKey");
    Optional<ApiSbu> apiSbu = apiSbuRepository.findByAppKey(apiKey);
    if (apiSbu.isEmpty()) {
      throw new IllegalApiKeyException();
    }

    String strSecret = apiSbu.get().getAppSecret();
    String strApp = apiSbu.get().getAppPath();
    try {
      financingService.recallApprovalStatus();
    } catch (Exception ignored) {
      ignored.printStackTrace();
    }

    return new CommonResult<>().success(null, "Success Check Approval Status");
  }

  @PostMapping(value = "/invoice-paid/{jwtToken}")
  public CommonResult<Object>
  invoicePaid(
    @PathVariable("jwtToken") String jwtToken,
    Authentication authentication,
    HttpServletRequest httpServletRequest,
    @Valid @RequestBody FinancingInvoicePaidRequest request
  ) throws Exception {
    try {
      String providedApiKey = httpServletRequest.getHeader("ApiKey");
      Optional<ApiSbu> apiSbu = apiSbuRepository.findByAppKey(providedApiKey);
      if (apiSbu.isEmpty()) {
        throw new IllegalApiKeyException();
      }

      FinancingHdr financingHdr = financingHdrService.paidFinancing(
        authentication,
        request,
        providedApiKey
      );

      financingDtlService.updatePaid(request, financingHdr);


      try {
        financingDtlService.paymentReceive(request, financingHdr);
      } catch (Exception ignored) {
        //akan ada proses skeduler
      }
      return new CommonResult<>().success(null, "Success Submitted");
    } catch (Exception e) {
      return new CommonResult<>().fail(500, e.getMessage());
    }
  }


  @PostMapping(value = "/inquiry/disburse/{jwtToken}")
  public CommonResult<Object>
  inquiryDisburse(
    @PathVariable("jwtToken") String jwtToken,
    Authentication authentication,
    HttpServletRequest httpServletRequest,
    @Valid @RequestBody FinancingInvoicePaidRequest request
  ) throws Exception {
    try {
      String providedApiKey = httpServletRequest.getHeader("ApiKey");
      Optional<ApiSbu> apiSbu = apiSbuRepository.findByAppKey(providedApiKey);
      if (apiSbu.isEmpty()) {
        throw new IllegalApiKeyException();
      }

      FinancingHdr financingHdr = financingHdrService.paidFinancing(
        authentication,
        request,
        providedApiKey
      );

      return new CommonResult<>().success(null, "Success Submitted");
    } catch (Exception e) {
      return new CommonResult<>().fail(500, e.getMessage());
    }
  }


  public InquiryDisburseResult inquiryDisburse(@Nullable InquiryDisburseRequest inquiryDisburseRequest) throws JsonProcessingException {
    try {
      final HttpHeaders headers = sbuRemoteService.adInsKeyHeaders();
      final HttpEntity<InquiryDisburseRequest> requestArgs = new HttpEntity<>(
        inquiryDisburseRequest,
        headers
      );

      final ResponseEntity<String> response = restTemplate.exchange(
        sbuRemoteService.inquiry_Disburse(),
        HttpMethod.POST,
        requestArgs,
        new ParameterizedTypeReference<>() {
        }
      );
      //int  o = response.getStatusCode().value();
      String stsr = String.valueOf(response.getBody());
      ObjectMapper om = new ObjectMapper();
      om.registerModule(new JavaTimeModule());
      InquiryDisburseResult root = om.readValue(stsr, InquiryDisburseResult.class);
      return root;//response.getBody();

    } catch (Exception e) {
      log.error("mstRefMasterInput: {}", e.getMessage());
      throw e;
    }
  }
}
