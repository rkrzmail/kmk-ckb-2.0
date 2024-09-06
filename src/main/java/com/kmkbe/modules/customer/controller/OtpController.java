package com.kmkbe.modules.customer.controller;

import com.kmkbe.core.domain.dto.LoginDto;
import com.kmkbe.core.domain.dto.RequestOtpDto;
import com.kmkbe.core.domain.model.CommonResult;
import com.kmkbe.modules.customer.request.LoginRequest;
import com.kmkbe.modules.customer.request.RequestOtpRequest;
import com.kmkbe.modules.customer.request.VerifyOtpRequest;
import com.kmkbe.modules.customer.service.AuthService;
import com.kmkbe.modules.customer.service.OtpService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/otp")
@Tag(
        name = "OTP",
        description = "Customer OTP Endpoints"
)
@RequiredArgsConstructor
public class OtpController {
    private final AuthService authService;
    private final OtpService otpService;

    @PutMapping("/verify/sign-up")
    public CommonResult<LoginDto> verifySignUp(
            @Valid @RequestBody VerifyOtpRequest request
    ) throws Exception {
        LoginRequest loginRequest = new LoginRequest(
                request.email(),
                request.pin()
        );

        String message = otpService.verifySignUp(request);
        return new CommonResult<LoginDto>().success(
                authService.signIn(loginRequest),
                message
        );
    }

    @PutMapping("/verify/forgot-pin")
    public CommonResult<String> verifyForgotPin(
            @Valid @RequestBody VerifyOtpRequest request
    ) throws Exception {
        return new CommonResult<String>().success(otpService.verifyForgotPin(request));
    }

    @PostMapping("/send/forgot-pin")
    public CommonResult<RequestOtpDto> sendForgotPin(
            @Valid @RequestBody RequestOtpRequest request
    ) throws Exception {
        return new CommonResult<RequestOtpDto>().success(otpService.sendForgotPin(request.email()));
    }

    @PostMapping("/resend/sign-up")
    public CommonResult<RequestOtpDto> resendSignUpOtp(
            @Valid @RequestBody RequestOtpRequest request
    ) throws Exception {
        return new CommonResult<RequestOtpDto>().success(otpService.resend(request, OtpService.OtpType.SIGNUP));
    }

    @PostMapping("/resend/forgot-pin")
    public CommonResult<RequestOtpDto> resendForgotPinOtp(
            @Valid @RequestBody RequestOtpRequest request
    ) throws Exception {
        return new CommonResult<RequestOtpDto>().success(otpService.resend(request, OtpService.OtpType.CHANGE_PIN));
    }
}
