package com.kmkbe.modules.customer.controller;

import com.kmkbe.modules.customer.request.SignUpRequest;
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

    @PostMapping("/resend/sign-up")
    public ResponseEntity<Object> resendSignUp(@RequestBody String custCode) {
        otpService.resendSignUp(custCode);
        return ResponseEntity.ok().body("Resend Success");
    }
}
