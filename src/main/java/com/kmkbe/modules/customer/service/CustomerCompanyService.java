package com.kmkbe.modules.customer.service;

import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.entity.CustomerCompany;
import com.kmkbe.modules.customer.repository.CustomerCompanyRepository;
import com.kmkbe.modules.customer.request.SignUpRequest;
import com.kmkbe.modules.customer.request.UpdateCustomerRequest;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerCompanyService {
    private final CustomerCompanyRepository customerCompanyRepository;

    public void get() {
        try {

        } catch (Exception e) {
            log.error("get: {}", e.getMessage());
        }
    }

    public void create(Customer customer, SignUpRequest.Company companyReq) {
        try {
            final boolean userExists = customerCompanyRepository
                    .findByCustCode(customer)
                    .isPresent();

            if (userExists) {
                throw new IllegalStateException("User already exists");
            }

            final CustomerCompany company = new CustomerCompany();
            {
                company.setCustCode(customer);
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
                company.setZipCode(companyReq.getZipCode());
                company.setArea(companyReq.getArea());
                company.setPhone(companyReq.getPhone());
                company.setOwnershipStatus(companyReq.getOwnershipStatus());
                company.setStaySince(companyReq.getStaySince());
                company.setStayLength(CustomerUtils.calculateStayLength(companyReq.getStaySince()));
            }

            customerCompanyRepository.save(company);
        } catch (Exception e) {
            log.error("create: {}", e.getMessage());
        }
    }

    public CustomerCompany update(
            Customer customer,
            UpdateCustomerRequest.UpdateCompanyRequest request,
            UpdateCustomerRequest.UpdateAddressRequest addressRequest
    ) {
        try {
            final Optional<CustomerCompany> find = customerCompanyRepository.findByCustCode(customer);
            final CustomerCompany company = find.orElseGet(CustomerCompany::new);
            if (find.isEmpty()) {
                company.setCustCompanyCode(UUID.randomUUID());
                company.setUsrCrt(customer.getCustName());
                company.setDtmCrt(Instant.now());
            } else {
                company.setUsrUpd(customer.getCustName());
                company.setDtmUpd(Instant.now());
            }

            company.setCustCode(customer);
            company.setCustCompanyType(request.getCustCompanyType());
            company.setCompanyModel(request.getCompanyModel());
            company.setIdentityType(request.getIdentityType());
            company.setIdentityNo(request.getIdentityNo());
            company.setIdentityIssuedDate(request.getIdentityIssuedDate().toInstant());
            company.setIdentityExpiredDate(request.getIdentityExpiredDate().toInstant());
            company.setCompanyAddress(request.getCompanyAddress());
            {
                company.setKelurahan(addressRequest.getKelurahan());
                company.setKecamatan(addressRequest.getKecamatan());
                company.setCity(addressRequest.getCity());
                company.setZipCode(addressRequest.getZipCode());
                company.setArea(addressRequest.getArea());
                company.setRt(addressRequest.getRt());
                company.setRw(addressRequest.getRw());
            }
            company.setPhone(request.getPhone());
            company.setOwnershipStatus(request.getOwnershipStatus());
            company.setStaySince(request.getStaySince().toInstant());
            company.setStayLength(CustomerUtils.calculateStayLength(request.getStaySince().toInstant()));
            company.setDtmUpd(Instant.now());
            company.setUsrUpd(customer.getCustName());
            return customerCompanyRepository.save(company);
        } catch (Exception e) {
            log.error("update: {}", e.getMessage());
            throw e;
        }
    }
}
