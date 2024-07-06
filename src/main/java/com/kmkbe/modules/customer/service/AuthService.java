package com.kmkbe.modules.customer.service;

import com.kmkbe.core.service.JwtService;
import com.kmkbe.modules.customer.constant.CustomerIdType;
import com.kmkbe.modules.customer.constant.CustomerType;
import com.kmkbe.modules.customer.entity.*;
import com.kmkbe.modules.customer.mapper.CustomerMapper;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.customer.repository.LoginLogRepository;
import com.kmkbe.modules.customer.request.ForgotPinRequest;
import com.kmkbe.modules.customer.request.LoginRequest;
import com.kmkbe.modules.customer.request.SignUpRequest;
import com.kmkbe.modules.customer.dto.LoginDto;
import com.kmkbe.modules.customer.dto.RequestOtpDto;
import jakarta.annotation.Nullable;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import com.kmkbe.modules.customer.dto.CustomerDto;

import java.math.BigDecimal;
import java.security.SignatureException;
import java.time.*;
import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {
    private final LoginLogRepository loginLogRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final OtpService otpService;
    private final BCryptPasswordEncoder bcryptEncoder;
    private final JwtService jwtService;
    private final SecurityContextLogoutHandler logoutHandler;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public RequestOtpDto signUp(SignUpRequest request) throws MessagingException {
        final Customer cust = new Customer();
        cust.setCustCode(UUID.randomUUID());
        cust.setCustName(request.getName());
        cust.setCustEmail(request.getEmail());
        cust.setCustTypeCode(request.getCustomerType().name());
        cust.setCustIdNo(request.getCustomerIdNo());
        cust.setCustMobilePhone(request.getMobilePhone());
        cust.setAgreeTc(request.getIsAgreeTc());
        cust.setCustPin(request.getPin());
        cust.setDtmUpd(OffsetDateTime.now());

        if (request.getCustomerNo() != null && !request.getCustomerNo().isEmpty()) {
            cust.setCustNo(request.getCustomerNo());
        }

        if (request.getCustomerType() == CustomerType.Company) {
            cust.setCustIdTypeCode(CustomerIdType.NPWP.name());
            if (request.getCompany() != null) {
                final SignUpRequest.Company companyReq = request.getCompany();
                final CustomerCompany company = new CustomerCompany();

                {
                    company.setCustCode(cust.getCustCode());
                    company.setCustCompanyType(companyReq.getCompanyType());
                    company.setCompanyModel(companyReq.getCompanyModel().name());
                    company.setIdentityType(companyReq.getIdentityType());
                    company.setIdentityNo(companyReq.getIdentityNo());
                    company.setIdentityIssuedDate(companyReq.getIdentityIssuedDate());
                    company.setIdentityExpiredDate(companyReq.getIdentityExpiredDate());
                    company.setCompanyAddress(companyReq.getCompanyAddress());
                    company.setRt(companyReq.getRt());
                    company.setRw(companyReq.getRw());
                    company.setKelurahan(companyReq.getKelurahan());
                    company.setKecamatan(companyReq.getKecamatan());
                    company.setCity(companyReq.getCity());
                    company.setProvince(companyReq.getProvince());
                    company.setZipcode(companyReq.getZipCode());
                    company.setArea(companyReq.getArea());
                    company.setPhone(companyReq.getPhone());
                    company.setOwnershipStatus(companyReq.getOwnershipStatus());
                    company.setStaySince(companyReq.getStaySince());

                    final long differenceDays = Duration.between(
                            companyReq.getStaySince(),
                            OffsetDateTime.now()
                    ).toMinutes() / 1440;

                    company.setStayLength(new BigDecimal(differenceDays));
                }

                customerService.createCompany(company);
            }
        } else if (request.getCustomerType() == CustomerType.Personal) {
            cust.setCustIdTypeCode(CustomerIdType.KTP.name());
            if (request.getPersonal() != null) {
                final SignUpRequest.Personal personalReq = request.getPersonal();
                final CustomerPersonal personal = new CustomerPersonal();

                {
                    personal.setCustCode(cust.getCustCode());
                    personal.setBirthplace(personalReq.getBirthPlace());
                    personal.setBirthdate(personalReq.getBirthDate());
                    personal.setGender(personalReq.getGender());
                    personal.setIdentityType(personalReq.getIdentityType());
                    personal.setIdentityNo(personalReq.getIdentityNo());
                    personal.setExpiredDate(personalReq.getExpiredDate());
                    personal.setMotherMaidenName(personalReq.getMotherMaidenName());
                    personal.setMaritalStatus(personalReq.getMaritalStatus());
                    personal.setCustModel(personalReq.getCustomerModel().name());
                    personal.setLegalAddress(personalReq.getLegalAddress());
                    personal.setRt(personalReq.getRt());
                    personal.setRw(personalReq.getRw());
                    personal.setKelurahan(personalReq.getKelurahan());
                    personal.setKecamatan(personalReq.getKecamatan());
                    personal.setCity(personalReq.getCity());
                    personal.setProvince(personalReq.getProvince());
                    personal.setZipcode(personalReq.getZipCode());
                    personal.setArea(personalReq.getArea());
                    personal.setPhone(personalReq.getPhone());
                    personal.setOwnershipStatus(personalReq.getOwnershipStatus());
                    personal.setStaySince(personalReq.getStaySince());
                    personal.setStayLength(new BigDecimal((Duration.between(
                            personalReq.getStaySince(),
                            OffsetDateTime.now()
                    ).toMinutes() / 1440)));
                }

                customerService.createPersonal(personal);
            }
        }

        customerService.create(cust);
        final OtpLog otpLog = otpService.create(cust, OtpService.OtpType.SIGNUP);

        return new RequestOtpDto(
                otpService.genRequestId(cust, otpLog),
                cust.getCustEmail(),
                otpLog.getExpiredDate()
        );
    }

    @Transactional
    public LoginDto login(LoginRequest request) {
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

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.pin()
                )
        );

        final LoginLog loginLog = new LoginLog();
        loginLog.setCustCode(cust.getCustCode());
        loginLog.setLoginLogCode(UUID.randomUUID());
        loginLog.setLoginDate(OffsetDateTime.now());
        loginLog.setLoginRole("Customer"); // need to change
        loginLog.setIsLogout(false);
        loginLogRepository.save(loginLog);

        return new LoginDto(
                jwtService.generateToken(cust),
                jwtService.getExpirationTime()
        );
    }

    @Transactional
    public String logout(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws SignatureException, IllegalStateException {
        if (authentication == null) {
            throw new IllegalStateException("User has logged out");
        }

        logoutHandler.logout(request, response, authentication);

        final Customer cust = CustomerMapper.INSTANCE.custEntityFromDto(authenticatedCustomer(authentication));
        final Optional<LoginLog> find = loginLogRepository.findTopByCustCode(cust.getCustCode());
        if (find.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }

        final LoginLog loginLog = find.get();
        loginLog.setIsLogout(true);
        loginLog.setLogoutDate(OffsetDateTime.now());
        loginLog.setUsrLogout(OffsetDateTime.now());
        loginLogRepository.save(loginLog);

        return "Logout Successfully";
    }

    public String forgotPin(ForgotPinRequest request) {
        final Optional<Customer> find = customerRepository.findByCustEmail(request.email());
        if (find.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }

        final Customer cust = find.get();
        cust.setCustPin(bcryptEncoder.encode(request.pin()));
        customerRepository.save(cust);

        return "Pin updated";
    }

    public CustomerDto authenticatedCustomer(Authentication authentication) throws SignatureException {
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Customer cust = (Customer) authentication.getPrincipal();
            return CustomerMapper.INSTANCE.custDtoFromEntity(cust);
        }

        throw new SignatureException();
    }

    @Nullable
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
