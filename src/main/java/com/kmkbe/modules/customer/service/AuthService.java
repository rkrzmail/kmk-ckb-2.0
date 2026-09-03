package com.kmkbe.modules.customer.service;

import com.kmkbe.adapter.ApiCsulAdapter;
import com.kmkbe.core.domain.constant.CustomerModel;
import com.kmkbe.core.domain.constant.CustomerType;
import com.kmkbe.core.domain.dto.InquiryVendorRemoteDto;
import com.kmkbe.core.domain.dto.RequestOtpDto;
import com.kmkbe.core.domain.entity.OtpLog;
import com.kmkbe.core.domain.entity.RedisLog;
import com.kmkbe.core.domain.entity.RedisAttack;
import com.kmkbe.core.domain.constant.AuditActorType;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.repository.OtpRepository;
import com.kmkbe.core.domain.repository.RedisAttackRepository;
import com.kmkbe.core.domain.repository.RedisRepository;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.service.JwtService;
import com.kmkbe.core.utils.CommonFormattingUtils;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.exception.BusinessException;
import com.kmkbe.feign.model.dto.CsulGetVendorDto;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.helpers.constant.ErrorConstant;
import com.kmkbe.modules.bouwheer.model.entity.Bouwheer;
import com.kmkbe.modules.bouwheer.repository.BouwheerRepository;
import com.kmkbe.modules.common.service.LoginLogService;
import com.kmkbe.modules.common.service.AuditTrailService;
import com.kmkbe.core.domain.constant.LoginRole;
import com.kmkbe.core.domain.dto.LoginDto;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.model.RefreshToken;
import com.kmkbe.modules.customer.model.request.SignUpRequest;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.customer.model.request.ForgotPinRequest;
import com.kmkbe.modules.customer.model.request.LoginRequest;
import com.kmkbe.modules.common.request.RefreshTokenRequest;
import com.kmkbe.modules.common.service.refresh_token.IRefreshTokenServices;
import com.kmkbe.helpers.utils.Utils;
import com.kmkbe.modules.loan_submission.service.DocumentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.SignatureException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class AuthService {
  private final CustomerRepository customerRepository;
  private final OtpService otpService;
  private final JwtService jwtService;
  private final LoginLogService loginLogService;
  private final ChangePasswordLogService changePasswordLogService;
  private final BCryptPasswordEncoder bcryptEncoder;
  private final AuthenticationManager authenticationManager;
  private final OtpRepository otpRepository;
  private final RedisRepository redisRepository;
  private final RedisAttackRepository redisAttackRepository;
  private final AuditTrailService auditTrailService;
  private final ApiCsulAdapter apiCsulAdapter;
  private final BouwheerRepository bouwheerRepository;
  private final CustomerService customerService;
  private final CustomerCompanyService customerCompanyService;
  private final CustomerPersonalService customerPersonalService;
  private final DocumentService documentService;

  @Qualifier("DbRefreshTokenServices")
  //@Qualifier("CacheRefreshTokenServices")
  private final IRefreshTokenServices refreshTokenServices;

  @Transactional
  public CommonResult<RequestOtpDto> signUp(SignUpRequest request
  ) throws Exception {
    final CsulGetVendorDto vendor;

    // Find vendor code use API
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
          .identityType("AKTA PENDIRIAN")
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
      throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, ErrorConstant.ERROR_MESSAGE_80 + "Tipe Debitur is not valid or is not in list");
    }

    final Customer cust = customerService.create(request, type);
    if (type == CustomerType.Company) {
      customerCompanyService.create(cust, request.getCompany());
    } else {
      customerPersonalService.create(cust,SignUpRequest.Personal.builder()
        .customerModel(CustomerModel.Pegawai)
        .identityType("KTP")
        .identityNo(request.getCustomerIdNo())
        .phone(request.getMobilePhone())
        .build());
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

  //@Transactional
  public BaseResponseBuilder<LoginDto> signIn(LoginRequest request) {
    try {

      //validate dan bruce attack
      String key = "signIn:" + request.email();
      int counter = 0;
      RedisAttack redisAttack;
      // Define a formatter for parsing the dates
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      LocalDateTime startDate;
      Optional<RedisAttack> redisAttacks = redisAttackRepository.findTopByRedis(key);
      if (redisAttacks.isEmpty()) {
        counter = 1;
        redisAttack = RedisAttack.builder()
          .redis(key)
          .session("")
          .countAttack(counter)
          .modifiedDate(DateTimeUtils.nowDate())
          .build();
        startDate = DateTimeUtils.nowLocal();
        redisAttackRepository.save(redisAttack);
      } else {
        redisAttack = redisAttacks.get();
        startDate = LocalDateTime.parse(Utils.formatDate(redisAttack.getModifiedDate()), formatter);

        counter = redisAttack.getCountAttack() + 1;
        redisAttack.setCountAttack(counter);
        redisAttack.setModifiedDate(DateTimeUtils.nowDate());
        redisAttackRepository.save(redisAttack);
      }


      if (counter > 5) {

        // Parse the input dates
        LocalDateTime endDate = DateTimeUtils.nowLocal();

        // Calculate the duration between the dates
        Duration duration = Duration.between(startDate, endDate);
        if (duration.toMinutes() < 15) {
          throw CommonInvalidException.invalidAttack();
        }
        redisAttack.setCountAttack(1);//update 1

      }

      if (!CommonFormattingUtils.isEmailValid(request.email().toLowerCase())) {
        throw CommonInvalidException.invalidEmailOrPin();
      }

      final Optional<Customer> findCust = customerRepository.findByCustEmail(request.email().toLowerCase());
      if (findCust.isEmpty()) {
        throw CommonInvalidException.invalidEmailOrPin();
      }
      //check singgle session

      final Customer cust = findCust.get();

      if(!cust.isActive()){
        log.info(ErrorConstant.ERROR_MESSAGE_81 + "{}", request.email());
        throw new BusinessException(HttpStatus.CONFLICT, ErrorConstant.ERROR_CODE_80, "Your Account not active, please confirm to Admin");
      }

      if (!bcryptEncoder.matches(request.pin(), cust.getCustPin())) {
        throw CommonInvalidException.invalidPin();
      }

      if (!cust.isActive()) {
        throw CommonInvalidException.notActive();
      }

      Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
          request.email().toLowerCase(),
          request.pin()
        )
      );

      SecurityContextHolder.getContext().setAuthentication(authentication);

      loginLogService.create(cust, LoginRole.Customer);

      final RefreshToken refreshTokenResult = refreshTokenServices.create(
        IRefreshTokenServices.User.builder()
          .userCode(cust.getCustCode())
          .build()
      );


      LoginDto loginDto = new LoginDto(
        jwtService.generateToken(cust),
        refreshTokenResult.getRefreshToken().toString(),
        jwtService.getExpirationTime());
      String jwt = refreshTokenResult.getRefreshToken().toString();

      //benar
      redisAttack.setCountAttack(0);
      redisAttack.setModifiedDate(DateTimeUtils.nowDate());
      redisAttackRepository.save(redisAttack);

      RedisLog redis = RedisLog.builder()
        .redis(request.email())
        .session(jwt)
        .build();
      redisRepository.save(redis);

      auditTrailService.recordAuthentication(
        "CUSTOMER_AUTH",
        AuditActorType.CUSTOMER,
        cust.getCustEmail(),
        cust.getCustCode(),
        true,
        null
      );

      return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY,loginDto);
    } catch (Exception e) {
      auditTrailService.recordAuthentication(
        "CUSTOMER_AUTH",
        AuditActorType.CUSTOMER,
        request.email(),
        null,
        false,
        e.getMessage()
      );
      log.error("AuthService signIn: {}", e.getMessage());
      throw e;
    }
  }

  @Transactional
  public String logout(Customer cust) {
    try {
      if (cust == null) {
        throw new IllegalStateException("User has logged out");
      }

      loginLogService.logout(cust);

      return "Logout Successfully";
    } catch (Exception e) {
      log.error("AuthService logout: {}", e.getMessage());
      throw e;
    }
  }

  @Transactional
  public String forgotPin(ForgotPinRequest request) {
    try {


      final Optional<Customer> find = customerRepository.findByCustEmail(request.email());
      if (find.isEmpty()) {
        throw new EntityNotFoundException("User not found");
      }

      Optional<OtpLog> findCust = otpRepository.findTopByEmailAndOtpCodeOrderByDtmCrtDesc(request.email(), request.token());
      if (findCust.isEmpty()) {
        throw new EntityNotFoundException("User doesn't exists");
      }


      final Customer cust = find.get();
      final String oldPin = cust.getCustPin();
      final String newPin = bcryptEncoder.encode(request.pin());

      cust.setCustPin(newPin);
      Customer saved = customerRepository.save(cust);
      changePasswordLogService.create(cust, oldPin, newPin);
      auditTrailService.record(
        "CUSTOMER",
        com.kmkbe.core.domain.constant.AuditAction.UPDATE,
        "Customer",
        saved.getCustCode(),
        new PasswordChangeAuditData(saved.getCustEmail(), false),
        new PasswordChangeAuditData(saved.getCustEmail(), true)
      );

      return "Forgot pin successfully, try to login with new pin now";
    } catch (Exception e) {
      log.error("AuthService forgotPin: {}", e.getMessage());
      throw e;
    }
  }

  @Transactional
  public LoginDto refreshToken(RefreshTokenRequest request) throws Exception {
    try {
      final RefreshToken payload = refreshTokenServices.verify(request.refreshToken());
      refreshTokenServices.invalidate(payload.getRefreshToken().toString());


      Optional<RedisLog> redisLog = redisRepository.findFirstBySession(payload.getRefreshToken().toString());
      if (redisLog.isEmpty()) {
        // throw new BadCredentialsException("Invalid token, Multi Login");
      }


      final Customer customer = customerRepository
        .findByCustCode(payload.getUserCode())
        .orElseThrow(() -> new IllegalStateException("Invalid Refresh Token, Entire Customer doesn't exists. Try to login again."));

      final RefreshToken refreshTokenResult = refreshTokenServices.create(
        IRefreshTokenServices.User.builder()
          .userCode(customer.getCustCode())
          .build()
      );

      return new LoginDto(
        jwtService.generateToken(customer),
        refreshTokenResult.getRefreshToken().toString(),
        jwtService.getExpirationTime()
      );
    } catch (Exception e) {
      log.error("AuthService refreshToken: {}", e.getMessage());
      throw e;
    }
  }

  private record PasswordChangeAuditData(String email, boolean pinChanged) {
  }
}
