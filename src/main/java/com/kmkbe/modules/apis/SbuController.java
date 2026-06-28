package com.kmkbe.modules.apis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kmkbe.core.domain.dto.*;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.FinancingDtl;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.model.ApiSbu;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.model.PaginationResult;
import com.kmkbe.core.domain.model.ValidationResponse;
import com.kmkbe.core.domain.repository.ApiSbuRepository;
import com.kmkbe.core.domain.repository.CustomerRepository;
import com.kmkbe.core.domain.request.InquiryDisburseRequest;
import com.kmkbe.core.domain.request.PaginationRequest;
import com.kmkbe.core.domain.response.InquiryDisburseResult;
import com.kmkbe.core.exception.CommonInvalidException;
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
import com.kmkbe.modules.user.utils.UserInternalUtils;
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
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.security.SignatureException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/sbu/ckb")//sbu
@Tag(
  name = "/api/v1/sbu/ckb",
  description = ""
)
@RequiredArgsConstructor
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


  public ValidationResponse apiValidation(String apiKey, String jwtToken) throws IOException {
    Optional<ApiSbu> apiSbu = apiSbuRepository.findByAppKey(apiKey);
    if (apiSbu.isEmpty()) {
      throw new IllegalApiKeyException();
    }
    ValidationResponse validationResponse = jwtValidatorService.validate(apiKey, jwtToken, apiSbu.get());
    String bouwheerCode = apiSbu.get().getBouwheerCode().toString();
    if (!validationResponse.getBouwheer().equalsIgnoreCase(bouwheerCode)) {
      throw new RuntimeException("bouwheer tidak cocok");
    }
    if (validationResponse.getExp() < (System.currentTimeMillis() / 1000)) {
      throw new RemoteException(" expired");
    }
    return validationResponse;
  }

  @PostMapping(value = "/test/{jwtToken}", consumes = MediaType.ALL_VALUE)
  public CommonResult<Object> test(
    @PathVariable("jwtToken") String jwtToken,
    @RequestHeader("ApiKey") String apiKey,
    @RequestBody(required = false) Object rawBody) throws IOException {

    System.out.println("JWT Token : " + jwtToken);
    System.out.println("ApiKey    : " + apiKey);
    System.out.println("Raw Body  : " + rawBody);

        /*
         1. ambil app_key dan api_screet
         2. data header dan row payload
         3. base64+payload+api_screet
         4. cocokan datanya dengan token kirim di path, kalo cococok ok


            Optional<ApiSbu> apiSbu = apiSbuRepository.findByAppKey(apiKey);
            if (apiSbu.isEmpty())    {
               throw new IllegalApiKeyException();
            }

            String strSecret = apiSbu.get().getAppSecret();
            String strApp = apiSbu.get().getAppPath();
            String bouwheerCode = apiSbu.get().getBouwheerCode().toString();

              ValidationResponse validationResponse = jwtValidatorService.validate(apiKey, jwtToken    );

                if (!validationResponse.getBouwheer().equalsIgnoreCase(bouwheerCode)){
                    throw new RuntimeException("bouwheer tidak cocok");
                }
                if (validationResponse.getExp()<(System.currentTimeMillis()/1000)){
                    throw new RemoteException(   " expired");
                }
         */

    ValidationResponse validationResponse = apiValidation(apiKey, jwtToken);

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
    String strApp = apiSbu.get().getAppPath();
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
      throw new IllegalArgumentException("Customer Bouwheer not found " + request.getBouwheerCode());
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
    @RequestHeader("ApiKey") String apiKey,
    @RequestBody(required = false) Object rawBody
  ) throws Exception {
    Optional<Customer> customerOptional = customerRepository.findByBouwheer(request.getBouwheerCode());
    if (customerOptional.isEmpty()) {
      throw new IllegalArgumentException("Customer Bouwheer not found " + request.getBouwheerCode());
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

  @PostMapping(value = "/listpostedinvoice/{jwtToken}")
  public CommonResult<InquiryInvoiceRemoteDto> listpostedinvoice(
    @PathVariable("jwtToken") String jwtToken,
    @RequestBody(required = false) Object rawBody
  ) throws Exception {
    JsonNode node = objectMapper.valueToTree(rawBody);
    String vendorCode = node.path("vendorCode").asText();

    BaseSimpleRemoteResponseDto<InquiryInvoiceRemoteDto> inquiryInvoice = invoiceRemoteDto.inquiryInvoice(vendorCode);

    return new CommonResult<InquiryInvoiceRemoteDto>().success(
      inquiryInvoice.getData(),
      ""
    );
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
