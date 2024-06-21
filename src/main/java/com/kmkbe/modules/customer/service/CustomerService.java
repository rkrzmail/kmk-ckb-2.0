package com.kmkbe.modules.customer.service;

import com.kmkbe.core.annotation.TrackExecutionTime;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.entity.CustomerCompany;
import com.kmkbe.modules.customer.entity.CustomerPersonal;
import com.kmkbe.modules.customer.repository.CustomerCompanyRepository;
import com.kmkbe.modules.customer.repository.CustomerPersonalRepository;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
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

    @TrackExecutionTime
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

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
        customer.setDtmCrt(OffsetDateTime.now());

        customerRepository.save(customer);

        return customer.getCustCode();
    }

    public UUID createPersonal(CustomerPersonal personal) {
        final boolean userExists = customerPersonalRepository
                .findByCustCode(personal.getCustCode())
                .isPresent();

        if (userExists) {
            throw new IllegalStateException("User already exists");
        }

        customerPersonalRepository.save(personal);

        return personal.getCustCode();
    }

    public UUID createCompany(CustomerCompany company) {
        final boolean userExists = customerCompanyRepository
                .findByCustCode(company.getCustCode())
                .isPresent();

        if (userExists) {
            throw new IllegalStateException("User already exists");
        }

        customerCompanyRepository.save(company);

        return company.getCustCode();
    }
}
