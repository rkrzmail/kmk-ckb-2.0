package com.kmkbe.modules.customer.controller;

import com.kmkbe.core.model.CommonResult;
import com.kmkbe.modules.customer.constant.CustomerType;
import com.kmkbe.modules.customer.dto.CustomerDto;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.entity.CustomerCompany;
import com.kmkbe.modules.customer.entity.CustomerPersonal;
import com.kmkbe.modules.customer.mapper.CustomerMapper;
import com.kmkbe.modules.customer.request.UpdateCustomerRequest;
import com.kmkbe.modules.customer.service.AuthService;
import com.kmkbe.modules.customer.service.CustomerCompanyService;
import com.kmkbe.modules.customer.service.CustomerPersonalService;
import com.kmkbe.modules.customer.service.CustomerService;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;

@RestController
@RequestMapping("/api/v1/customer")
@Tag(
        name = "Customer",
        description = "Customer Endpoints"
)
@RequiredArgsConstructor
public class CustomerController {
    private final AuthService authService;
    private final CustomerService customerService;
    private final CustomerCompanyService customerCompanyService;
    private final CustomerPersonalService customerPersonalService;

    @GetMapping
    public CommonResult<CustomerDto> profile(
            Authentication authentication
    ) throws SignatureException, BadCredentialsException, IllegalStateException, IllegalAccessException {
        Customer customer = CustomerUtils.authenticateCustomer(authentication);
        CustomerDto result = CustomerMapper.INSTANCE.custDtoFromEntity(customer);
        if (customer.getCompany() != null) {
            result.setAddress(CustomerMapper.addressDtoFromCompany(customer.getCompany()));
            result.setCompany(CustomerMapper.INSTANCE.companyDtoFromEntity(customer.getCompany()));
        } else if (customer.getPersonal() != null) {
            result.setAddress(CustomerMapper.addressDtoFromPersonal(customer.getPersonal()));
            result.setPersonal(CustomerMapper.INSTANCE.personalDtoFromEntity(customer.getPersonal()));
        }
        return new CommonResult<CustomerDto>().success(result);
    }

    @PutMapping
    @Transactional
    public CommonResult<CustomerDto> updateCustomer(
            Authentication authentication,
            @Valid @RequestBody UpdateCustomerRequest request
    ) throws Exception {
        Customer customer = customerService.update(authentication, request);
        CustomerDto result = CustomerMapper.INSTANCE.custDtoFromEntity(customer);
        if (customer.getCustTypeCode().equals(CustomerType.Company.name())) {
            if (request.getCompany() == null) {
                throw new IllegalArgumentException("Company cannot be null");
            }

            CustomerCompany company = customerCompanyService.update(
                    customer,
                    request.getCompany(),
                    request.getAddress()
            );
            result.setAddress(CustomerMapper.addressDtoFromCompany(company));
            result.setCompany(CustomerMapper.INSTANCE.companyDtoFromEntity(company));

        } else if (customer.getCustTypeCode().equals(CustomerType.Personal.name())) {
            if (request.getPersonal() == null) {
                throw new IllegalArgumentException("Personal cannot be null");
            }

            CustomerPersonal personal = customerPersonalService.update(
                    customer,
                    request.getPersonal(),
                    request.getAddress()
            );
            result.setAddress(CustomerMapper.addressDtoFromPersonal(personal));
            result.setPersonal(CustomerMapper.INSTANCE.personalDtoFromEntity(personal));
        }

        return new CommonResult<CustomerDto>().success(result);
    }
}
