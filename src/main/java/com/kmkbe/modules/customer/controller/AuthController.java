package com.kmkbe.modules.customer.controller;

import com.kmkbe.adapter.ApiCsulAdapter;
import com.kmkbe.core.domain.constant.CustomerType;
import com.kmkbe.core.domain.dto.InquiryVendorRemoteDto;
import com.kmkbe.core.domain.dto.LoginDto;
import com.kmkbe.core.domain.dto.RequestOtpDto;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.helpers.constant.ErrorConstant;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.entity.OtpLog;
import com.kmkbe.core.domain.entity.RedisAttack;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.core.domain.repository.RedisAttackRepository;
import com.kmkbe.core.domain.repository.RedisRepository;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.feign.model.dto.CsulGetVendorDto;
import com.kmkbe.modules.common.request.RefreshTokenRequest;
import com.kmkbe.modules.customer.model.request.ForgotPinRequest;
import com.kmkbe.modules.customer.model.request.LoginRequest;
import com.kmkbe.modules.customer.model.request.SignUpRequest;
import com.kmkbe.modules.customer.service.*;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import com.kmkbe.modules.remote.service.CustomerRemoteService;
import com.kmkbe.nikita.utils.Utils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Slf4j
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
  private final ApiCsulAdapter apiCsulAdapter;
  private final CustomerRepository customerRepository;
  private final CurrentUserService currentUserService;

  //@Transactional
  @PostMapping("/sign-up")
  public CommonResult<RequestOtpDto> signUp(
    @Valid @RequestBody SignUpRequest request
  ) throws Exception {
    final CsulGetVendorDto vendor;

    vendor = apiCsulAdapter.findByCode(request.getVendorCode());
    if (vendor == null) {
      log.info(ErrorConstant.ERROR_MESSAGE_80 + "{}", request.getBouwheerCode());
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, ErrorConstant.ERROR_MESSAGE_80 + "Vendor ID Perusahaan Tidak Ditemukan");
    }

    log.info("Inquiry Vendor {} ", vendor);
    // Validate Bouwheer Code
    Optional<Bouwheer> bouwheerOptional = bouwheerRepository.findByBouwheerCode(UUID.fromString(request.getBouwheerCode()));
    if (bouwheerOptional.isEmpty()) {
      throw new IllegalArgumentException("Invalid Bouwheer Code " + request.getBouwheerCode());
    }

    final CustomerType type;
    if (request.getCustomerType().equalsIgnoreCase("perusahaan")) {
      type = CustomerType.Company;
      String address = "", province = "", city = "", kecamatan = "", kelurahan = "";

      if (vendor.getVendorBuilding() != null) {
        address = Optional.ofNullable(vendor.getVendorBuilding().getAddressInfo()).orElse("");
        province = Optional.ofNullable(vendor.getVendorBuilding().getStateName()).orElse("");
        city = Optional.ofNullable(vendor.getVendorBuilding().getCityName()).orElse("");
        kecamatan = Optional.ofNullable(vendor.getVendorBuilding().getDistrictName()).orElse("");
        kelurahan = Optional.ofNullable(vendor.getVendorBuilding().getDistrictName()).orElse("");
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
          .companyType(vendor.getJenisPerusahaan())
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

    final Customer cust = customerService.create(request, type);
    if (type == CustomerType.Company) {
      customerCompanyService.create(cust, request.getCompany());
    } else {
      customerPersonalService.create(cust, request.getPersonal());
    }


    documentService.mappingFromInquiryVendor(cust, InquiryVendorRemoteDto.builder()
      .vendorId(vendor.getVendorId())
      .sapCode(vendor.getSapCode())
      .vendorName(vendor.getVendorName())
      .foundedDate(vendor.getFoundedDate())
      .npwp(vendor.getNpwp())
      .npwpLink(vendor.getNpwpUrl())
      .nipSiup(vendor.getNipSiup())
      .nipSiupLink(vendor.getNipSiupLink())
      .pkpNumber(vendor.getPkpNumber())
      .pkpLink(vendor.getPkpLink())
      .jenisPerusahaan(vendor.getJenisPerusahaan())
      .jenisPerusahaanName(vendor.getJenisPerusahaan())
      .jenisPerusahaanDescription(vendor.getJenisPerusahaan())
      .ktpNpwpVendorStockId(vendor.getKtpNpwpVendorStockId())
      .ktpNpwpVendorStockLink(vendor.getKtpNpwpVendorStockLink())
      .aktaPendirianLink(vendor.getAktaPendirianLink())
      .aktaPerubahanLink(vendor.getAktaPerubahanLink())
      .pengesahanKemenkumhamLink(vendor.getPengesahanKemenkumhamLink())
      .vendorBuilding(Collections.singletonList(InquiryVendorRemoteDto.VendorBuilding.builder()
        .ownershipStatus(vendor.getVendorBuilding() != null && vendor.getVendorBuilding().getOwnershipStatus() != null
          ? String.valueOf(vendor.getVendorBuilding().getOwnershipStatus()) : "0")
        .jenis(vendor.getVendorBuilding() != null && vendor.getVendorBuilding().getJenis() != null
          ? String.valueOf(vendor.getVendorBuilding().getJenis()) : "0")
        .category(vendor.getVendorBuilding() != null && vendor.getVendorBuilding().getCategory() != null
          ? String.valueOf(vendor.getVendorBuilding().getCategory()) : "0")
        .addressDetail(vendor.getVendorBuilding() != null ? vendor.getVendorBuilding().getAddressDetail() : "")
        .addressInfo(vendor.getVendorBuilding() != null ? vendor.getVendorBuilding().getAddressInfo() : "")
        .stateName(vendor.getVendorBuilding() != null ? vendor.getVendorBuilding().getStateName() : "")
        .cityName(vendor.getVendorBuilding() != null ? vendor.getVendorBuilding().getCityName() : "")
        .districtName(vendor.getVendorBuilding() != null ? vendor.getVendorBuilding().getDistrictName() : "")
        .build()))
      .laporanKeuanganLink(vendor.getLaporanKeuanganLink())
      .email(vendor.getEmail())
      .phone(vendor.getPhone())
      .website(vendor.getWebsite())
      .fax(vendor.getFax())
      .ktpDirectur(vendor.getKtpDirectur())
      .ktpDirekturLink(vendor.getKtpDirekturLink())
      .positionRef(vendor.getPositionRef())
      .bankDetail(vendor.getBankDetail())
      .vendorRegistrationDoc(vendor.getVendorRegistrationDoc())
      .otherDocument(vendor.getOtherDocument())
      .build());

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
  public BaseResponse signIn(
    @Valid @RequestBody LoginRequest request
  ) {
    return authService.signIn(request);
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
    return new CommonResult<>().success(message);
  }

  @DeleteMapping("/sign-out")
  public CommonResult<Object> signOut(
    HttpServletRequest request,
    HttpServletResponse response
  ) throws SignatureException, IllegalStateException {
    final String result = authService.logout(currentUserService.customer());
    logoutHandler.logout(request, response, null);
    return new CommonResult<>().success(
      result
    );
  }

  @PostMapping("/refresh-token")
  public CommonResult<LoginDto> refreshToken(
    @Valid @RequestBody RefreshTokenRequest request
  ) throws Exception {
    return new CommonResult<LoginDto>().success(
      authService.refreshToken(request)
    );
  }
}
