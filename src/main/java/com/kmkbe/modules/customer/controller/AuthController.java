package com.kmkbe.modules.customer.controller;

import com.kmkbe.core.domain.constant.CustomerType;
import com.kmkbe.core.domain.dto.InquiryVendorRemoteDto;
import com.kmkbe.core.domain.dto.LoginDto;
import com.kmkbe.core.domain.dto.RequestOtpDto;
import com.kmkbe.core.domain.entity.Bouwheer;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.OtpLog;
import com.kmkbe.core.domain.entity.RedisAttack;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.repository.BouwheerRepository;
import com.kmkbe.core.domain.repository.RedisAttackRepository;
import com.kmkbe.core.domain.repository.RedisRepository;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.common.request.RefreshTokenRequest;
import com.kmkbe.modules.customer.request.ForgotPinRequest;
import com.kmkbe.modules.customer.request.LoginRequest;
import com.kmkbe.modules.customer.request.SignUpRequest;
import com.kmkbe.modules.customer.service.*;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import com.kmkbe.nikita.utils.Utils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(
  name = "Auhtentication",
  description = "Customer Authentication Endpoints"
)
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final SecurityContextLogoutHandler logoutHandler;
  private final CustomerService customerService;
  private final CustomerCompanyService customerCompanyService;
  private final CustomerPersonalService customerPersonalService;
  private final OtpService otpService;
  private final CustomerRemoteService customerRemoteService;
  private final DocumentService documentService;
  private final RedisRepository redisRepository;
  private final RedisAttackRepository redisAttackRepository;
  private final BouwheerRepository bouwheerRepository;

  //@Transactional
  @PostMapping("/sign-up")
  public CommonResult<RequestOtpDto> signUp(
    @Valid @RequestBody SignUpRequest request
  ) throws Exception {
    final InquiryVendorRemoteDto vendor;

    try {
      vendor = customerRemoteService.inquiryVendor(request.getVendorCode()).getData();
    } catch (Exception e) {
      throw CommonInvalidException.builder()
        .title("Perusahaan Tidak Ditemukan")
        .message("Mohon maaf, saat ini Anda belum dapat menggunakan " +
          "Dana Sakti. Harap melakukan pengecekan ulang " +
          "dengan pihak PT. Trakindo Utama.")
        .build();
    }

    // Validate Bouwheer Code
    Optional<Bouwheer> bouwheerOptional = bouwheerRepository.findByBouwheerCode(UUID.fromString(request.getBouwheer()));
    if (bouwheerOptional.isEmpty()) {
      throw new IllegalArgumentException("Invalid Bouwheer Code " + request.getBouwheer());
    }

    final CustomerType type;
    if (request.getCustomerType().equalsIgnoreCase("perusahaan")) {
      type = CustomerType.Company;
      String address = "", province = "", city = "", kecamatan = "", kelurahan = "";
      if (
        vendor.getVendorBuilding() != null
          && !vendor.getVendorBuilding().isEmpty()
      ) {
        address = vendor.getVendorBuilding().getFirst().getAddressInfo();
        province = vendor.getVendorBuilding().getFirst().getStateName();
        city = vendor.getVendorBuilding().getFirst().getCityName();
        kecamatan = vendor.getVendorBuilding().getFirst().getDistrictName();
        kelurahan = vendor.getVendorBuilding().getFirst().getDistrictName();
      }

      LocalDateTime staySince;
      try {
        staySince = LocalDateTime.parse(vendor.getFoundedDate());
      } catch (Exception e) {
        staySince = DateTimeUtils.now();
      }

      request.setCompany(
        SignUpRequest.Company.builder()
          .companyModel("")
          .companyType(vendor.getJenisPerusahaanDescription())
          .identityType("AKTA")
          .identityNo(request.getCustomerIdNo())
          .identityIssuedDate(DateTimeUtils.now())
          .identityExpiredDate(DateTimeUtils.now())
          .companyAddress(address)
          .custIdNo(vendor.getNipSiup())
          .rt("")
          .rw("")
          .kelurahan(kelurahan)
          .kecamatan(kecamatan)
          .city(city)
          .province(province)
          .zipCode("")
          .area("")
          .phone(request.getMobilePhone())
          .ownershipStatus("")
          .staySince(staySince)
          .build()
      );
    } else if (request.getCustomerType().equalsIgnoreCase("perorangan")) {
      type = CustomerType.Personal;
    } else {
      throw new Exception("Tipe Debitur is not valid or is not in list");
    }

    final Customer cust = customerService.create(request, vendor, type);
    if (type == CustomerType.Company) {
      customerCompanyService.create(cust, request.getCompany());
    } else {
      customerPersonalService.create(cust, request.getPersonal());
    }
    documentService.mappingFromInquiryVendor(cust, vendor);

    final OtpLog otpLog = otpService.create(cust, OtpService.OtpType.SIGNUP);

    return new CommonResult<RequestOtpDto>().success(
      new RequestOtpDto(
        otpService.genRequestId(cust, otpLog),
        cust.getCustEmail(),
        otpLog.getExpiredDate()
      )
    );
  }

  @PostMapping("/sign-in")
  public CommonResult<LoginDto> signIn(
    @Valid @RequestBody LoginRequest request
  ) {
    return new CommonResult<LoginDto>().success(authService.signIn(request));
  }

  @PutMapping("/forgot-pin")
  public CommonResult<Object> forgotPin(
    @Valid @RequestBody ForgotPinRequest request
  ) {
    //validate dan bruce attack
    String key = "forgot:" + request.email();
    int counter = 0;
    RedisAttack redisAttack;
    Optional<RedisAttack> redisAttacks = redisAttackRepository.findTopByRedis(key);
    if (redisAttacks.isEmpty()) {
      counter = 1;
      redisAttack = RedisAttack.builder()
        .redis(key)
        .session("")
        .countAttack(counter)
        .modifiedDate(DateTimeUtils.nowDate())
        .build();
      redisAttackRepository.save(redisAttack);
    } else {
      redisAttack = redisAttacks.get();
      counter = redisAttack.getCountAttack() + 1;
      redisAttack.setCountAttack(counter);
      redisAttack.setModifiedDate(DateTimeUtils.nowDate());
      redisAttackRepository.save(redisAttack);
    }
    if (counter > 5) {
      // Define a formatter for parsing the dates
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

      // Parse the input dates
      LocalDateTime startDate = LocalDateTime.parse(Utils.formatDate(redisAttack.getModifiedDate()), formatter);
      LocalDateTime endDate = DateTimeUtils.nowLocal();

      // Calculate the duration between the dates
      Duration duration = Duration.between(startDate, endDate);
      if (duration.toMinutes() < 15) {
        throw CommonInvalidException.invalidAttack();
      }
      redisAttack.setCountAttack(1);//update 1
    }

    final String message = authService.forgotPin(request);

    redisAttack.setCountAttack(0);
    redisAttack.setModifiedDate(DateTimeUtils.nowDate());
    redisAttackRepository.save(redisAttack);
    return new CommonResult<>().success(null, message);
  }

  @DeleteMapping("/sign-out")
  public CommonResult<Object> signOut(
    Authentication authentication,
    HttpServletRequest request,
    HttpServletResponse response
  ) throws SignatureException, IllegalStateException {
    final String result = authService.logout(authentication);
    logoutHandler.logout(request, response, authentication);
    return new CommonResult<>().success(
      null,
      result
    );
  }

  /* Unsecure refresh token */
   /* @PostMapping("/refresh-token")
    public CommonResult<RefreshTokenDto> refreshToken(HttpServletRequest request) {
        // claims object was passing in header from middleware (JwtAuthenticationFilter)
        DefaultClaims claims = (DefaultClaims) request.getAttribute("claims");
        Map<String, Object> expectedMap = new HashMap<>(claims);
        String token = jwtService.generateRefreshToken(expectedMap, expectedMap.get("sub").toString());
        return new CommonResult<RefreshTokenDto>().success(
                new RefreshTokenDto(
                        token,
                        jwtService.getRefreshTokenExpirationTime()
                )
        );
    }*/

  @PostMapping("/refresh-token")
  public CommonResult<LoginDto> refreshToken(
    @Valid @RequestBody RefreshTokenRequest request
  ) throws Exception {
    return new CommonResult<LoginDto>().success(
      authService.refreshToken(request)
    );
  }
}
