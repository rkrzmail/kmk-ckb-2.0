package com.kmkbe.modules.customer.service;

import com.kmkbe.core.service.JwtService;
import com.kmkbe.modules.customer.constant.CustomerIdType;
import com.kmkbe.modules.customer.constant.CustomerType;
import com.kmkbe.modules.customer.constant.LoginRole;
import com.kmkbe.modules.customer.dto.LoginDto;
import com.kmkbe.modules.customer.dto.RequestOtpDto;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.entity.OtpLog;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.customer.request.ForgotPinRequest;
import com.kmkbe.modules.customer.request.LoginRequest;
import com.kmkbe.modules.customer.request.RefreshTokenRequest;
import com.kmkbe.modules.customer.request.SignUpRequest;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SignatureException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final CustomerCompanyService companyService;
    private final CustomerPersonalService personalService;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final LoginLogService loginLogService;
    private final ChangePasswordLogService changePasswordLogService;
    private final BCryptPasswordEncoder bcryptEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Transactional
    public RequestOtpDto signUp(SignUpRequest request) throws MessagingException {
        try {
            CustomerType type;
            if (request.getCustomerType().equalsIgnoreCase("perusahaan")) {
                type = CustomerType.Company;
            } else {
                type = CustomerType.Personal;
            }

            final Customer cust = new Customer();
            cust.setCustCode(UUID.randomUUID());
            cust.setCustName(request.getName());
            cust.setCustEmail(request.getEmail());
            cust.setCustTypeCode(type.name());
            cust.setCustIdNo(request.getCustomerIdNo());
            cust.setCustMobilePhone(request.getMobilePhone());
            cust.setAgreeTc(request.getIsAgreeTc());
            cust.setCustPin(request.getPin());
            cust.setDtmUpd(Instant.now());

            if (request.getCustomerNo() != null && !request.getCustomerNo().isEmpty()) {
                cust.setCustNo(request.getCustomerNo());
            }

            if (type == CustomerType.Company) {
                cust.setCustIdTypeCode(CustomerIdType.NPWP.name());
                if (request.getCompany() != null) {
                    companyService.create(cust, request.getCompany());
                }
            } else {
                cust.setCustIdTypeCode(CustomerIdType.KTP.name());
                if (request.getPersonal() != null) {
                    //personalService.create(cust, request.getPersonal());
                }
            }

            customerService.create(cust);
            final OtpLog otpLog = otpService.create(cust, OtpService.OtpType.SIGNUP);

            return new RequestOtpDto(
                    otpService.genRequestId(cust, otpLog),
                    cust.getCustEmail(),
                    otpLog.getExpiredDate()
            );
        } catch (Exception e) {
            log.error("AuthService signUp: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public LoginDto signIn(LoginRequest request) {
        try {
            final Optional<Customer> findCust = customerRepository.findByCustEmail(request.email());
            if (findCust.isEmpty()) {
                throw new EntityNotFoundException("User not found");
            }

            final Customer cust = findCust.get();
            if (!bcryptEncoder.matches(request.pin(), cust.getCustPin())) {
                throw new BadCredentialsException("Email or pin is invalid, try to entry right email and pin");
            }

            if (!cust.getIsActive()) {
                throw new IllegalStateException("Your account is not activated yet");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.pin()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            loginLogService.create(cust, LoginRole.Customer);

            return new LoginDto(
                    jwtService.generateToken(cust),
                    jwtService.generateRefreshToken(cust),
                    jwtService.getExpirationTime()
            );
        } catch (Exception e) {
            log.error("AuthService signIn: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public String logout(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws SignatureException {
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

    public LoginDto refreshToken(RefreshTokenRequest request) throws Exception {
        try {
            final String username = jwtService.extractUsername(request.refreshToken());
            final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(request.refreshToken(), userDetails)) {
                return new LoginDto(
                        jwtService.generateToken(userDetails),
                        jwtService.generateRefreshToken(userDetails),
                        jwtService.getExpirationTime()
                );
            }

            throw new IllegalStateException("Refresh token are expired or invalid, try to login again");
        } catch (Exception e) {
            log.error("AuthService refreshToken: {}", e.getMessage());
            throw e;
        }
    }
}
