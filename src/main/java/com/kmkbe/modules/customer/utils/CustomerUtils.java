package com.kmkbe.modules.customer.utils;

import com.kmkbe.core.constants.CommonConstants;
import com.kmkbe.modules.customer.model.dto.CustomerDto;
import com.kmkbe.modules.customer.model.entity.Customer;
import com.kmkbe.core.domain.mapper.CustomerMapper;
import com.kmkbe.core.utils.DateTimeUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SignatureException;
import java.time.Duration;
import java.time.LocalDateTime;

public class CustomerUtils {
    public static Customer authenticateCustomer(Authentication authentication) throws SignatureException {
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
            if (authentication.getPrincipal() instanceof Customer) {
                return (Customer) authentication.getPrincipal();
            }
        }

        throw new SignatureException("You are not authorized to access this resource");
    }

    public static CustomerDto authenticateCustomerDto(Authentication authentication) throws SignatureException {
        return CustomerMapper.INSTANCE.custDtoFromEntity(authenticateCustomer(authentication));
    }

    public static double calculateStayLength(LocalDateTime staySince) {
        return BigDecimal.valueOf(Duration.between(
                        staySince,
                        DateTimeUtils.now()
                ).toMinutes() / (double) CommonConstants.MONTH_IN_MINUTES).setScale(1, RoundingMode.CEILING)
                .doubleValue();
    }

    public static void clearCustomerInactiveData(JdbcTemplate jdbcTemplate, Customer customer) {
        jdbcTemplate.update("DELETE FROM public.legal_file WHERE cust_code = ?", customer.getCustCode());
        jdbcTemplate.update("DELETE FROM public.customer_personal WHERE cust_code = ?", customer.getCustCode());
        jdbcTemplate.update("DELETE FROM public.customer_company WHERE cust_code = ?", customer.getCustCode());
        jdbcTemplate.update("DELETE FROM public.customer WHERE cust_code = ?", customer.getCustCode());
    }
}
