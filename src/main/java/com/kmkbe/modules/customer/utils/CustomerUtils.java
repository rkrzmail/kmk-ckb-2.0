package com.kmkbe.modules.customer.utils;

import com.kmkbe.core.constants.CommonConstants;
import com.kmkbe.modules.customer.dto.CustomerDto;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.mapper.CustomerMapper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SignatureException;
import java.time.Duration;
import java.time.Instant;

public class CustomerUtils {
    public static Customer authenticateCustomer(Authentication authentication) throws SignatureException {
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            return (Customer) authentication.getPrincipal();
        }

        throw new SignatureException();
    }

    public static CustomerDto authenticateCustomerDto(Authentication authentication) throws SignatureException {
        return CustomerMapper.INSTANCE.custDtoFromEntity(authenticateCustomer(authentication));
    }

    public static double calculateStayLength(Instant staySince) {
        return BigDecimal.valueOf(Duration.between(
                        staySince,
                        Instant.now()
                ).toMinutes() / (double) CommonConstants.MONTH_IN_MINUTES).setScale(1, RoundingMode.CEILING)
                .doubleValue();
    }
}
