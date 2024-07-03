package com.kmkbe.modules.customer.controller;

import com.kmkbe.core.model.CommonResult;
import com.kmkbe.modules.customer.service.AuthService;
import com.kmkbe.modules.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kmkbe.modules.customer.dto.CustomerDto;

import java.security.SignatureException;

@RestController
@RequestMapping("/api/v1/customer")
@Tag(
        name = "Customer",
        description = "Customer Endpoints"
)
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;
    private final AuthService authService;

    @GetMapping("/profile")
    public CommonResult<CustomerDto> getAllCustomers(
            Authentication authentication
    ) throws SignatureException, BadCredentialsException, IllegalStateException {
        return new CommonResult<CustomerDto>().success(authService.authenticatedCustomer(authentication));
    }
}
