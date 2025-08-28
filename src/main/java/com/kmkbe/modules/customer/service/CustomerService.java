package com.kmkbe.modules.customer.service;

import com.kmkbe.core.domain.constant.CustomerIdType;
import com.kmkbe.core.domain.constant.CustomerType;
import com.kmkbe.core.domain.dto.CustomerDto;
import com.kmkbe.core.domain.dto.InquiryVendorRemoteDto;
import com.kmkbe.core.domain.dto.ProfileFapDto;
import com.kmkbe.core.domain.dto.ProfileSITDto;
import com.kmkbe.core.domain.entity.Customer;
import com.kmkbe.core.domain.entity.FinancingHdr;
import com.kmkbe.core.domain.repository.CustomerCompanyRepository;
import com.kmkbe.core.domain.repository.CustomerPersonalRepository;
import com.kmkbe.core.domain.repository.CustomerRepository;
import com.kmkbe.core.domain.repository.FinancingHdrRepository;
import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.core.utils.DateTimeUtils;
import com.kmkbe.core.utils.FormatingUtils;
import com.kmkbe.modules.customer.request.SignUpRequest;
import com.kmkbe.modules.customer.request.UpdateCustomerRequest;
import com.kmkbe.modules.customer.request.UpdateFapRequest;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SignatureException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerPersonalRepository customerPersonalRepository;
    private final CustomerCompanyRepository customerCompanyRepository;
    private final BCryptPasswordEncoder bcryptEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final FinancingHdrRepository financingHdrRepository;

    public Customer create(
            SignUpRequest request,
            InquiryVendorRemoteDto vendor,
            CustomerType type
    ) throws CommonInvalidException {
        try {
            if (!request.getIsAgreeTc()) {
                throw new Exception("Setujui Syarat dan Ketentuan for sign up");
            }

            final Optional<Customer> find = customerRepository
                    .findByCustEmail(request.getEmail());

            if (find.isPresent()) {
                if (find.get().getIsActive()) {
                    throw CommonInvalidException.alreadyRegistered();
                } else {
                    CustomerUtils.clearCustomerInactiveData(jdbcTemplate, find.get());
                }
            }

            final String encodePin = bcryptEncoder.encode(request.getPin());

            final Customer customer = new Customer();
            customer.setCustCode(UUID.randomUUID());
            customer.setCustName(request.getName());
            customer.setCustEmail(request.getEmail().toLowerCase());

            if (request.getCustomerNo() != null && !request.getCustomerNo().isEmpty()) {
                customer.setCustNo(request.getCustomerNo());
            }

            if (type == CustomerType.Company) {
                customer.setCustIdTypeCode(CustomerIdType.NPWP.name());
                if (request.getCustomerIdNo() != null && request.getCustomerIdNo().length() < 16) {
                    //throw new Exception("NPWP minimal 16 Karakter");
                }
            } else {
                customer.setCustIdTypeCode(CustomerIdType.KTP.name());
                if (
                        request.getCustomerIdNo() != null
                                && request.getCustomerIdNo().length() != 16
                ) {
                    throw new Exception("KTP minimal dan maksimal 16 Karakter");
                }
            }

            customer.setCustTypeCode(type.name());
            customer.setCustIdNo(request.getCustomerIdNo());
            customer.setCustMobilePhone(FormatingUtils.formatOnlyNumber(request.getMobilePhone()));
            customer.setAgreeTc(request.getIsAgreeTc());
            customer.setCustPin(encodePin);
            customer.setIsEmailValid(false);
            customer.setIsActive(false);

            if (request.getVendorCode() != null && !request.getVendorCode().isEmpty()) {
                customer.setCustExternalCode(request.getVendorCode());
            }

            customer.setUsrCrt(customer.getCustName());
            customer.setDtmCrt(DateTimeUtils.now());
            return customerRepository.save(customer);
        } catch (CommonInvalidException e) {
            log.error("create, error {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void activated(Customer customer) {
        customer.setIsEmailValid(true);
        customer.setIsActive(true);
        customer.setUsrUpd(customer.getCustName());
        customer.setDtmUpd(DateTimeUtils.now());
        customerRepository.save(customer);
    }


    public Customer update(
            Authentication authentication,
            UpdateCustomerRequest request

    ) throws SignatureException {
        try {
            Customer customer = CustomerUtils.authenticateCustomer(authentication);
            boolean emailChanged = false;


//            if (!customer.getCustTypeCode().equalsIgnoreCase(request.getCustTypeCode())) {
//                throw new IllegalArgumentException("Can't update customerTypeCode, please ensure valid payload");
//            }

            String oldEmail = customer.getCustEmail();
            String newEmail = request.getCustEmail();

            if (newEmail != null && !oldEmail.equalsIgnoreCase(newEmail)) {
                log.info("Email change detected: oldEmail={} -> newEmail={}", oldEmail, newEmail);
                boolean emailExists = customerRepository.existsByCustEmailAndCustIdNoNot(
                        newEmail, customer.getCustIdNo()
                );
                if (emailExists) {
                    log.warn("Update aborted: newEmail={} already exists in database", newEmail);
                    throw new IllegalArgumentException("Email already exists, please use another one");
                }
                customer.setCustEmail(newEmail);
                emailChanged = true; // karena memang ada perubahan email
                log.info("Email successfully updated for custIdNo={}", customer.getCustIdNo());
            }else {
                log.info("No email change detected, email remains {}", oldEmail);
            }

            log.info("Updating other fields for custIdNo={}", customer.getCustIdNo());
            customer.setCustName(request.getCustName());
            //customer.setCustTypeCode(request.getCustTypeCode());
            customer.setCustIdNo(request.getCustIdNo());
            customer = customerRepository.save(customer);
            customer.setForceLogout(emailChanged);
            log.info("Customer updated successfully: custCode={}, forceLogout={}",
                    customer.getCustCode(), emailChanged);
            return customer;
        } catch (Exception e) {
            log.error("update, error {}", e.getMessage());
            throw e;
        }
    }

    public ProfileFapDto prolifeFAP(Authentication authentication, HttpServletRequest request){
            String financingHdrCode = request.getParameter("financingHdrCode");


        return null;
    }

    public ProfileSITDto prolifeSIT(Authentication authentication, HttpServletRequest request){
        String financingHdrCode = request.getParameter("financingHdrCode");

        Optional<FinancingHdr> financingHdr = financingHdrRepository.findByFinancingHdrCode(UUID.fromString(financingHdrCode));
        if(financingHdr.isPresent()) {
            FinancingHdr hdr = financingHdr.get();
            hdr.getCustomer().getCustName();
            if (hdr.getCustomer().getCustTypeCode().equalsIgnoreCase("")){
                hdr.getCustomer().getCompany().getDirectorName();
            }

        }
        return ProfileSITDto.builder()
                .rt("")
                .rt("")
                .namaBank("")
                .build();
    }

    public void updateFapData(UpdateFapRequest request) {
        String email = request.getEmail();

        Optional<Customer> customerOptional = customerRepository.findByCustEmail(email);
        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();

            UUID custCode = customer.getCustCode();

            Pageable pageable = PageRequest.of(0, 1);
            List<FinancingHdr> financingHdrList = financingHdrRepository.findLatestFinancingHdrByCustCode(custCode, pageable);

            if (!financingHdrList.isEmpty()) {
                FinancingHdr financingHdr = financingHdrList.get(0);

                financingHdr.setFapDate(request.getFapDate());
                financingHdr.setFapStatus(request.getFapStatus());

                financingHdrRepository.save(financingHdr);
            } else {
                throw new IllegalArgumentException("No FinancingHdr found for custCode: " + custCode);
            }
        } else {
            throw new IllegalArgumentException("Customer with email " + email + " not found");
        }
    }
}
