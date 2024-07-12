package com.kmkbe.modules.customer.controller;

import com.kmkbe.core.model.CommonResult;
import com.kmkbe.modules.customer.entity.Customer;
import com.kmkbe.modules.customer.request.RequestOtpRequest;
import com.kmkbe.modules.customer.request.VerifyOtpRequest;
import com.kmkbe.modules.customer.dto.RequestOtpDto;
import com.kmkbe.modules.customer.service.OtpService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
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
    private final OtpService otpService;

    @PutMapping("/verify/sign-up")
    public CommonResult<Object> verifySignUp(@RequestBody VerifyOtpRequest request) throws Exception {
        return new CommonResult<Object>().success(null, otpService.verifySignUp(request));
    }

    @PutMapping("/verify/forgot-pin")
    public CommonResult<String> verifyForgotPin(@RequestBody VerifyOtpRequest request) throws Exception {
        return new CommonResult<String>().success(otpService.verifyForgotPin(request));
    }

    @PostMapping("/send/forgot-pin")
    public CommonResult<RequestOtpDto> sendForgotPin(@RequestBody RequestOtpRequest request) throws MessagingException {
        return new CommonResult<RequestOtpDto>().success(otpService.sendForgotPin(request.email()));
    }

    @PostMapping("/resend/sign-up")
    public CommonResult<RequestOtpDto> resendSignUpOtp(@RequestBody RequestOtpRequest request) throws MessagingException {
        return new CommonResult<RequestOtpDto>().success(otpService.resend(request, OtpService.OtpType.SIGNUP));
    }

    @PostMapping("/resend/forgot-pin")
    public CommonResult<RequestOtpDto> resendForgotPinOtp(@RequestBody RequestOtpRequest request) throws MessagingException {
        return new CommonResult<RequestOtpDto>().success(otpService.resend(request, OtpService.OtpType.CHANGE_PIN));
    }
}
