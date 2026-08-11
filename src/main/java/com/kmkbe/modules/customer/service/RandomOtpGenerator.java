package com.kmkbe.modules.customer.service;

import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.Random;

@Component
public class RandomOtpGenerator implements OtpGenerator {
    private final Random random = new Random();
    private final DecimalFormat formatter = new DecimalFormat("0000");

    @Override
    public String generate() {
        return formatter.format(random.nextInt(9999));
    }
}
