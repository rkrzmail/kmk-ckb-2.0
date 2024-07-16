package com.kmkbe.modules.customer.utils;

import com.kmkbe.modules.customer.dto.CustomerDto;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.mapper.CustomerMapper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.security.SignatureException;

public class CustomerUtils {
    public static Customer authenticateCustomer(Authentication authentication) throws SignatureException {
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return (Customer) authentication.getPrincipal();
        }

        throw new SignatureException();
    }

    public static CustomerDto authenticateCustomerDto(Authentication authentication) throws SignatureException {
        return CustomerMapper.INSTANCE.custDtoFromEntity(authenticateCustomer(authentication));
    }
}
