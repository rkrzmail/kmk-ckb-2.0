package com.kmkbe.modules.common.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RotationCounterService {
    private final Map<String, AtomicLong> agreementCounters = new ConcurrentHashMap<>();

    public long getAndIncrement(String agreementCode) {
        return agreementCounters
                .computeIfAbsent(agreementCode, k -> new AtomicLong(0))
                .getAndIncrement();
    }
}
