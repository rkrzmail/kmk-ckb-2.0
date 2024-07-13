package com.kmkbe.modules.customer.service;

import com.kmkbe.core.annotation.TrackExecutionTime;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.entity.CustomerCompany;
import com.kmkbe.modules.customer.entity.CustomerPersonal;
import com.kmkbe.modules.customer.repository.CustomerCompanyRepository;
import com.kmkbe.modules.customer.repository.CustomerPersonalRepository;
import com.kmkbe.modules.customer.repository.CustomerRepository;
import com.kmkbe.modules.customer.request.SignUpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
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

    public void createPersonal(Customer cust, SignUpRequest.Personal personalReq) {
        final boolean userExists = customerPersonalRepository
                .findByCustCode(cust)
                .isPresent();

        if (userExists) {
            throw new IllegalStateException("User already exists");
        }

        final CustomerPersonal personal = new CustomerPersonal();
        {
            personal.setCustCode(cust);
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
            personal.setStayLength((double) (Duration.between(
                                personalReq.getStaySince(),
                                OffsetDateTime.now()
                        ).toMinutes() / 1440));
        }

        customerPersonalRepository.save(personal);
    }

    public void createCompany(Customer cust, SignUpRequest.Company companyReq) {
        final boolean userExists = customerCompanyRepository
                .findByCustCode(cust)
                .isPresent();

        if (userExists) {
            throw new IllegalStateException("User already exists");
        }

        final CustomerCompany company = new CustomerCompany();
        {
            company.setCustCode(cust);
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

            company.setStayLength((double) differenceDays);
        }

        customerCompanyRepository.save(company);
    }

    public void activated(Customer customer) {
        customer.setIsEmailValid(true);
        customer.setIsActive(true);
        customer.setUsrUpd(customer.getCustName());
        customer.setDtmUpd(OffsetDateTime.now());
        customerRepository.save(customer);
    }
}
