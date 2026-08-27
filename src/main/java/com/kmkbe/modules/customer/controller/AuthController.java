package com.kmkbe.modules.customer.controller;

import com.kmkbe.core.domain.dto.LoginDto;
import com.kmkbe.core.domain.dto.RequestOtpDto;
import com.kmkbe.helpers.base.BaseResponse;
import com.kmkbe.core.domain.entity.RedisAttack;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.core.domain.repository.RedisAttackRepository;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.security.CurrentUserService;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.modules.common.request.RefreshTokenRequest;
import com.kmkbe.modules.customer.model.request.ForgotPinRequest;
import com.kmkbe.modules.customer.model.request.LoginRequest;
import com.kmkbe.modules.customer.model.request.SignUpRequest;
import com.kmkbe.modules.customer.service.*;
import com.kmkbe.helpers.utils.Utils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Tag(
  name = "Auhtentication",
  description = "Customer Authentication Endpoints"
)
public class AuthController {
  private final AuthService authService;
  private final SecurityContextLogoutHandler logoutHandler;
  private final RedisAttackRepository redisAttackRepository;
  private final CurrentUserService currentUserService;

  public AuthController(AuthService authService,
                        SecurityContextLogoutHandler logoutHandler,
                        RedisAttackRepository redisAttackRepository,
                        CurrentUserService currentUserService) {
    this.authService = authService;
    this.logoutHandler = logoutHandler;
    this.redisAttackRepository = redisAttackRepository;
    this.currentUserService = currentUserService;
  }

  @PostMapping("/sign-up")
  public CommonResult<RequestOtpDto> signUp(
    @Valid @RequestBody SignUpRequest request
  ) throws Exception {
    return authService.signUp(request);
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
    int counter;
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
