package com.kmkbe.modules.customer.controller;

import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.request.LoginRequest;
import com.kmkbe.modules.customer.request.SignUpRequest;
import com.kmkbe.modules.customer.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<Object> signUp(@RequestBody SignUpRequest request) {
        return ResponseEntity.ok().body(authService.signUp(request));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<Object> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok().body(authService.login(request));
    }
}
