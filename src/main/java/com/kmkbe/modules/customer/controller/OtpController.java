package com.kmkbe.modules.customer.controller;

import com.kmkbe.modules.customer.request.RequestOtpRequest;
import com.kmkbe.modules.customer.request.VerifyOtpRequest;
import com.kmkbe.modules.customer.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
public class OtpController {
    private final OtpService otpService;

    @PutMapping("/verify/sign-up")
    public ResponseEntity<Object> verifySignUp(@RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok().body(otpService.verifySignUp(request));
    }

    @PutMapping("/verify/forgot-pin")
    public ResponseEntity<Object> verifyForgotPin(@RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok().body(otpService.verifyForgotPin(request));
    }

    @PostMapping("/send/forgot-pin")
    public ResponseEntity<Object> sendForgotPin(@RequestBody RequestOtpRequest request) {
        return ResponseEntity.ok().body(otpService.sendForgotPin(request.email()));
    }

    @PostMapping("/resend")
    public String resendSignUp(@RequestBody RequestOtpRequest request) {
        return otpService.resend(request.email());
    }
}
