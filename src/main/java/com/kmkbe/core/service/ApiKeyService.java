package com.kmkbe.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApiKeyService {
    private final KeySecretServices keySecretServices;

    public Extractor extractor(@NonNull String encodedKey) {
        try {
            final String payloadStr = keySecretServices.decodePayloadAsString(encodedKey);
            if (keySecretServices.validate(encodedKey)) {
                return null;
            }

            return null;
        } catch (Exception e) {
            log.error("Error while extracting ApiKey: {}", e.getMessage());
            return null;
        }
    }


    public record Extractor(String apiKey, boolean isValid) {
    }
}
