package com.kmkbe.modules.customer.service;

import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.service.JwtService;
import com.kmkbe.core.utils.CommonFormattingUtils;
import com.kmkbe.modules.common.service.LoginLogService;
import com.kmkbe.core.domain.constant.LoginRole;
import com.kmkbe.core.domain.dto.LoginDto;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.model.RefreshToken;
import com.kmkbe.core.domain.repository.CustomerRepository;
import com.kmkbe.modules.customer.request.ForgotPinRequest;
import com.kmkbe.modules.customer.request.LoginRequest;
import com.kmkbe.modules.common.request.RefreshTokenRequest;
import com.kmkbe.modules.common.service.refresh_token.IRefreshTokenServices;
import com.kmkbe.modules.customer.utils.CustomerUtils;
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

    @Qualifier("DbRefreshTokenServices")
    //@Qualifier("CacheRefreshTokenServices")
    private final IRefreshTokenServices refreshTokenServices;

    @Transactional
    public LoginDto signIn(LoginRequest request) {
        try {
            if (!CommonFormattingUtils.isEmailValid(request.email().toLowerCase())) {
                throw CommonInvalidException.invalidEmail();
            }

            final Optional<Customer> findCust = customerRepository.findByCustEmail(request.email().toLowerCase());
            if (findCust.isEmpty()) {
                throw CommonInvalidException.notRegistered();
            }

            final Customer cust = findCust.get();
            if (!bcryptEncoder.matches(request.pin(), cust.getCustPin())) {
                throw CommonInvalidException.invalidPin();
            }

            if (!cust.getIsActive()) {
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

            return new LoginDto(
                    jwtService.generateToken(cust),
                    refreshTokenResult.getRefreshToken().toString(),
                    jwtService.getExpirationTime()
            );
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
