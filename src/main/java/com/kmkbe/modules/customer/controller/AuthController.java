package com.kmkbe.modules.customer.controller;

import com.kmkbe.core.service.JwtService;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.common.service.EmailService;
import com.kmkbe.modules.customer.request.ForgotPinRequest;
import com.kmkbe.modules.customer.request.LoginRequest;
import com.kmkbe.modules.customer.request.SignUpRequest;
import com.kmkbe.modules.customer.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final SecurityContextLogoutHandler logoutHandler;

    @PostMapping("/sign-up")
    public ResponseEntity<Object> signUp(@RequestBody SignUpRequest request) {
        return ResponseEntity.ok().body(authService.signUp(request));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<Object> signIn(@RequestBody LoginRequest request) {
        return ResponseEntity.ok().body(authService.login(request));
    }


    @DeleteMapping("/sign-out")
    public ResponseEntity<Object> signOut(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws SignatureException, IllegalStateException {
        final String result = authService.logout();
        logoutHandler.logout(request, response, authentication);
        return ResponseEntity.ok(result);
    }


    @PutMapping("/forgot-pin")
    public ResponseEntity<Object> forgotPin(@RequestBody ForgotPinRequest request) {
        return ResponseEntity.ok().body(authService.forgotPin(request));
    }

}
