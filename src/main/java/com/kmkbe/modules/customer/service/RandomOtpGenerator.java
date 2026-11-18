package com.kmkbe.modules.customer.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.text.DecimalFormat;

@Component
public class RandomOtpGenerator implements OtpGenerator {
    /**
     * Security issue #7 (SECURITY.md): java.util.Random predictable - OTP wajib
     * memakai SecureRandom. Panjang 4 digit (sesuai FE).
     */
    private final SecureRandom random = new SecureRandom();
    private final DecimalFormat formatter = new DecimalFormat("0000");

    @Override
    public String generate() {
        return formatter.format(random.nextInt(10_000));
    }
}
