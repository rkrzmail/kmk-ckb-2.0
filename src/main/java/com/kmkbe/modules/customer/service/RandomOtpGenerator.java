package com.kmkbe.modules.customer.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.text.DecimalFormat;

@Component
public class RandomOtpGenerator implements OtpGenerator {
    /**
     * Security issue #7 (SECURITY.md): java.util.Random predictable - OTP wajib
     * memakai SecureRandom. Panjang dinaikkan 4 -> 6 digit; VerifyOtpRequest.otp
     * hanya @NotNull sehingga tidak breaking.
     */
    private final SecureRandom random = new SecureRandom();
    private final DecimalFormat formatter = new DecimalFormat("000000");

    @Override
    public String generate() {
        return formatter.format(random.nextInt(1_000_000));
    }
}
