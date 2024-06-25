package com.kmkbe.modules.customer.controller;

import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.service.AuthService;
import com.kmkbe.modules.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SignatureException;
import java.util.List;

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
    public ResponseEntity<Object> getAllCustomers(
            Authentication authentication
    ) throws SignatureException {
        return ResponseEntity.ok().body(authService.authenticatedCustomer(authentication));
    }
}
