package com.kmkbe.modules.customer.controller;

import com.kmkbe.core.model.CommonResult;
import com.kmkbe.modules.customer.request.ForgotPinRequest;
import com.kmkbe.modules.customer.request.LoginRequest;
import com.kmkbe.modules.customer.request.SignUpRequest;
import com.kmkbe.modules.customer.dto.LoginDto;
import com.kmkbe.modules.customer.dto.RequestOtpDto;
import com.kmkbe.modules.customer.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(
        name = "Auhtentication",
        description = "Customer Authentication Endpoints"
)
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-up")
    public CommonResult<RequestOtpDto> signUp(@RequestBody SignUpRequest request) throws Exception {
        return new CommonResult<RequestOtpDto>().success(authService.signUp(request));
    }

    @PostMapping("/sign-in")
    public CommonResult<LoginDto> signIn(@RequestBody LoginRequest request) {
        return new CommonResult<LoginDto>().success(authService.login(request));
    }

    @PutMapping("/forgot-pin")
    public CommonResult<Object> forgotPin(@RequestBody ForgotPinRequest request) {
        final String message = authService.forgotPin(request);
        return new CommonResult<>().success(null, message);
    }

    @DeleteMapping("/sign-out")
    public CommonResult<Object> signOut(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws SignatureException, IllegalStateException {
        return new CommonResult<>().success(
                null,
                authService.logout(authentication, request, response)
        );
    }
}
