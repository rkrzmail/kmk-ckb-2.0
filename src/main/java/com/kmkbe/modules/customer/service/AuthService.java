package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.entity.OtpLog;
import com.kmkbe.core.domain.entity.RedisLog;
import com.kmkbe.core.domain.entity.RedisAttack;
import com.kmkbe.core.domain.repository.OtpRepository;
import com.kmkbe.core.domain.repository.RedisAttackRepository;
import com.kmkbe.core.domain.repository.RedisRepository;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.service.JwtService;
import com.kmkbe.core.utils.CommonFormattingUtils;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.helpers.base.BaseResponseBuilder;
import com.kmkbe.helpers.constant.AppConstants;
import com.kmkbe.modules.common.service.LoginLogService;
import com.kmkbe.core.domain.constant.LoginRole;
import com.kmkbe.core.domain.dto.LoginDto;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.model.RefreshToken;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.customer.model.request.ForgotPinRequest;
import com.kmkbe.modules.customer.model.request.LoginRequest;
import com.kmkbe.modules.common.request.RefreshTokenRequest;
import com.kmkbe.modules.common.service.refresh_token.IRefreshTokenServices;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import com.kmkbe.nikita.utils.Utils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SignatureException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
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

  @Qualifier("DbRefreshTokenServices")
  //@Qualifier("CacheRefreshTokenServices")
  private final IRefreshTokenServices refreshTokenServices;

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

      return new BaseResponseBuilder<>(true, AppConstants.CODE_OK, AppConstants.PROCESS_SUCCESSFULLY,loginDto);
    } catch (CommonInvalidException e) {
      log.error("AuthService signIn: {}", e.getMessage());
      throw e;
    }
  }

  @Transactional
  public String logout(Authentication authentication) throws SignatureException {
    try {
      if (authentication == null) {
        throw new IllegalStateException("User has logged out");
      }

      final Customer cust = CustomerUtils.authenticateCustomer(authentication);
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
      customerRepository.save(cust);
      changePasswordLogService.create(cust, oldPin, newPin);

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
}
