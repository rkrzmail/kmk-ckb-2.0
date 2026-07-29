package com.kmkbe.modules.customer.controller;

import com.kmkbe.core.domain.dto.LoginDto;
import com.kmkbe.core.domain.dto.RequestOtpDto;
import com.kmkbe.core.domain.entity.RedisAttack;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.repository.RedisAttackRepository;
import com.kmkbe.core.domain.repository.RedisRepository;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.customer.model.request.LoginRequest;
import com.kmkbe.modules.customer.model.request.RequestOtpRequest;
import com.kmkbe.modules.customer.model.request.VerifyOtpRequest;
import com.kmkbe.modules.customer.service.AuthService;
import com.kmkbe.modules.customer.service.OtpService;
import com.kmkbe.nikita.utils.Utils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/otp")
@Tag(
  name = "OTP",
  description = "Customer OTP Endpoints"
)
@RequiredArgsConstructor
public class OtpController {
  private final AuthService authService;
  private final OtpService otpService;
  private final RedisRepository redisRepository;
  private final RedisAttackRepository redisAttackRepository;


  @PutMapping("/verify/sign-up")
  public CommonResult<LoginDto> verifySignUp(
    @Valid @RequestBody VerifyOtpRequest request
  ) throws Exception {
    //validate dan bruce attack
    String key = "verifysignup:" + request.email();
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

    LoginRequest loginRequest = new LoginRequest(
      request.email(),
      request.pin()
    );

    String message = otpService.verifySignUp(request);


    redisAttack.setCountAttack(0);
    redisAttack.setModifiedDate(DateTimeUtils.nowDate());
    redisAttackRepository.save(redisAttack);
    return new CommonResult<LoginDto>().success(
      authService.signIn(loginRequest),
      message
    );
  }

  @PutMapping("/verify/forgot-pin")
  public CommonResult<String> verifyForgotPin(
    @Valid @RequestBody VerifyOtpRequest request
  ) throws Exception {

    //validate dan bruce attack
    String key = "verifyforgot:" + request.email();
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

    String string = otpService.verifyForgotPin(request);

    redisAttack.setCountAttack(0);
    redisAttack.setModifiedDate(DateTimeUtils.nowDate());
    redisAttackRepository.save(redisAttack);
    return new CommonResult<String>().success(string);
  }

  @PostMapping("/send/forgot-pin")
  public CommonResult<RequestOtpDto> sendForgotPin(
    @Valid @RequestBody RequestOtpRequest request
  ) throws Exception {
    return new CommonResult<RequestOtpDto>().success(otpService.sendForgotPin(request.email()));
  }

  @PostMapping("/resend/sign-up")
  public CommonResult<RequestOtpDto> resendSignUpOtp(
    @Valid @RequestBody RequestOtpRequest request
  ) throws Exception {
    return new CommonResult<RequestOtpDto>().success(otpService.resend(request, OtpService.OtpType.SIGNUP));
  }

  @PostMapping("/resend/forgot-pin")
  public CommonResult<RequestOtpDto> resendForgotPinOtp(
    @Valid @RequestBody RequestOtpRequest request
  ) throws Exception {
    return new CommonResult<RequestOtpDto>().success(otpService.resend(request, OtpService.OtpType.CHANGE_PIN));
  }

  @PostMapping("/generate")
  public CommonResult<String> generateOtp(@RequestBody RequestOtpRequest request) {
    try {
      // Generate OTP for the provided email
      otpService.resendOtpForEmail(request.email(), OtpService.OtpType.SIGNUP); // Directly generate OTP and send it
      return new CommonResult<String>().success("OTP has been sent to the email address: " + request.email());
    } catch (Exception e) {
      return new CommonResult<String>().fail(400, "Error generating OTP: " + e.getMessage());
    }
  }

  // New endpoint to resend OTP without checking the customer (using JSON body)
  @PostMapping("/resend")
  public CommonResult<String> resendOtp(@RequestBody RequestOtpRequest request) {
    try {
      // Resend OTP for the provided email
      otpService.resendOtpForEmail(request.email(), OtpService.OtpType.SIGNUP); // Resend OTP to the email
      return new CommonResult<String>().success("OTP has been resent to the email address: " + request.email());
    } catch (Exception e) {
      return new CommonResult<String>().fail(400, "Error resending OTP: " + e.getMessage());
    }
  }

  @PostMapping("/verify")
  public CommonResult<String> verifyOtp(@RequestBody VerifyOtpRequest request) {
    try {
      // Verify the OTP for the provided email
      otpService.verifyOtp(request.email(), request.otp());  // Validate OTP
      return new CommonResult<String>().success("OTP verified successfully.");
    } catch (Exception e) {
      return new CommonResult<String>().fail(400, "Error verifying OTP: " + e.getMessage());
    }
  }
}
