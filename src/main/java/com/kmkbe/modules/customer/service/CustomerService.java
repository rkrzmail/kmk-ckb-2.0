package com.kmkbe.modules.customer.service;

import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.customer.request.UpdateCustomerRequest;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SignatureException;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder bcryptEncoder;

    public UUID create(Customer customer) {
        final boolean userExists = customerRepository
                .findByCustEmail(customer.getCustEmail())
                .isPresent();

        if (userExists) {
            throw new IllegalStateException("Email already taken");
        }

        final String encodePin = bcryptEncoder.encode(customer.getCustPin());
        customer.setCustPin(encodePin);
        customer.setIsEmailValid(false);
        customer.setIsActive(false);
        customer.setUsrCrt(customer.getCustName());
        customer.setDtmCrt(Instant.now());

        customerRepository.save(customer);

        return customer.getCustCode();
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
            customer.setCustName(request.getCustName());
            customer.setCustTypeCode(request.getCustTypeCode());
            customer.setCustIdNo(request.getCustIdNo());
            customer = customerRepository.save(customer);
            return customer;
        } catch (Exception e) {
            log.error("update, error {}", e.getMessage());
            throw e;
        }
    }
}
