package com.kmkbe.modules.customer.controller;

import com.kmkbe.core.model.CommonResult;
import com.kmkbe.modules.customer.dto.CustomerDto;
import com.kmkbe.modules.customer.request.UpdateCustomerRequest;
import com.kmkbe.modules.customer.service.AuthService;
import com.kmkbe.modules.customer.service.CustomerService;
import com.kmkbe.modules.customer.utils.CustomerUtils;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    private final CustomerService customerService;
    private final AuthService authService;

    @GetMapping
    public CommonResult<CustomerDto> profile(
            Authentication authentication
    ) throws SignatureException, BadCredentialsException, IllegalStateException {
        return new CommonResult<CustomerDto>().success(
                CustomerUtils.authenticateCustomerDto(authentication)
        );
    }

    @PutMapping
    public CommonResult<CustomerDto> updateCustomer(
            Authentication authentication,
            @RequestBody UpdateCustomerRequest request
    ) throws Exception {
        return new CommonResult<CustomerDto>().success(
                customerService.update(authentication, request)
        );
    }
}
