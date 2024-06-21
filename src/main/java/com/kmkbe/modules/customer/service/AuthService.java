package com.kmkbe.modules.customer.service;

import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.constant.CustomerIdType;
import com.kmkbe.modules.customer.constant.CustomerType;
import com.kmkbe.modules.customer.entity.*;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.customer.repository.LoginLogRepository;
import com.kmkbe.modules.customer.request.LoginRequest;
import com.kmkbe.modules.customer.request.SignUpRequest;
import com.kmkbe.modules.customer.response.LoginResponse;
import com.kmkbe.modules.customer.response.SignUpResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {
    private final LoginLogRepository loginLogRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final BCryptPasswordEncoder bcryptEncoder;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        int a = 0;
        final Customer cust = new Customer();
        cust.setCustCode(UUID.randomUUID());
        cust.setCustName(request.getName());
        cust.setCustEmail(request.getEmail());
        cust.setCustTypeCode(request.getCustomerType().name());
        cust.setCustIdNo(request.getCustomerIdNo());
        cust.setCustMobilePhone(request.getMobilePhone());
        cust.setAgreeTc(request.getIsAgreeTc());
        cust.setCustPin(request.getPin());

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

        final UUID result = customerService.create(cust);
        final OtpLog otpLog = otpService.create(request.getName(), request.getMobilePhone(), request.getEmail());
        emailService.sendOtp(cust, otpLog.getOtpCode());
        return new SignUpResponse(result);
    }

    public LoginResponse login(LoginRequest request) {
        final Optional<Customer> findCust = customerRepository.findByCustEmailAndCustPin(request.email(), request.pin());
        if (findCust.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }

        final Customer cust = findCust.get();
        if (!bcryptEncoder.matches(request.pin(), cust.getCustPin())) {
            throw new EntityNotFoundException("Invalid pin");
        }

        final LoginLog loginLog = new LoginLog();
        loginLog.setCustCode(cust.getCustCode());
        loginLog.setLoginLogCode(UUID.randomUUID());
        loginLog.setLoginDate(OffsetDateTime.now());
        loginLog.setLoginRole("");
        loginLogRepository.save(loginLog);

        return new LoginResponse("");
    }

    public void logout() {

    }
}
