package com.kmkbe.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/**
 * @deprecated Dead code: tidak memiliki pemanggil, dan {@link #extractor(String)}
 * selalu mengembalikan {@code null} (cabang if-nya pun terbalik - memanggil
 * {@code validate()} lalu tetap return null). Jangan dipakai sebagai contoh.
 * Verifikasi security issue #12 (SECURITY.md).
 */
@Deprecated
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
