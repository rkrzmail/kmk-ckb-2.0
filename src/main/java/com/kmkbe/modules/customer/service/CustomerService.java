package com.kmkbe.modules.customer.service;

import com.kmkbe.core.exception.CommonInvalidException;
import com.kmkbe.modules.customer.constant.CustomerIdType;
import com.kmkbe.modules.customer.constant.CustomerType;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.customer.request.SignUpRequest;
import com.kmkbe.modules.customer.request.UpdateCustomerRequest;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import com.kmkbe.modules.remote.dto.InquiryVendorRemoteDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SignatureException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder bcryptEncoder;

    public Customer create(
            SignUpRequest request,
            InquiryVendorRemoteDto vendor
    ) throws CommonInvalidException {
        try {
            final CustomerType type;
            if (request.getCustomerType().equalsIgnoreCase("perusahaan")) {
                type = CustomerType.Company;
            } else if (request.getCustomerType().equalsIgnoreCase("perorangan")) {
                type = CustomerType.Personal;
            } else {
                throw new Exception("Tipe Debitur is not valid or is not in list");
            }

            if (!request.getIsAgreeTc()) {
                throw new Exception("Setujui Syarat dan Ketentuan for sign up");
            }

            final Optional<Customer> find = customerRepository
                    .findByCustEmail(request.getEmail());

            if (find.isPresent()) {
                if (find.get().getIsActive()) {
                    throw CommonInvalidException.alreadyRegistered();
                } else {
                    customerRepository.delete(find.get());
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
            customer.setCustMobilePhone(request.getMobilePhone());
            customer.setAgreeTc(request.getIsAgreeTc());
            customer.setCustPin(encodePin);
            customer.setIsEmailValid(false);
            customer.setIsActive(false);

            if (request.getVendorCode() != null && !request.getVendorCode().isEmpty()) {
                customer.setCustExternalCode(request.getVendorCode());
            }

            customer.setUsrCrt(customer.getCustName());
            customer.setDtmCrt(Instant.now());
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
        customer.setDtmUpd(Instant.now());
        customerRepository.save(customer);
    }


    public Customer update(
            Authentication authentication,
            UpdateCustomerRequest request

    ) throws SignatureException {
        try {
            Customer customer = CustomerUtils.authenticateCustomer(authentication);
            //customer.setCustEmail(request.getCustEmail());
            if (!customer.getCustTypeCode().equalsIgnoreCase(request.getCustTypeCode())) {
                throw new IllegalArgumentException("Can't update customerTypeCode, please ensure valid payload");
            }

            customer.setCustName(request.getCustName());
            //customer.setCustTypeCode(request.getCustTypeCode());
            customer.setCustIdNo(request.getCustIdNo());
            customer = customerRepository.save(customer);
            return customer;
        } catch (Exception e) {
            log.error("update, error {}", e.getMessage());
            throw e;
        }
    }
}
