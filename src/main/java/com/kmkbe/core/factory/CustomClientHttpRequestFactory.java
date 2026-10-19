package com.kmkbe.core.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * Standard client request factory with configurable timeout.
 * Dinonaktifkan pemaksaan promiscuous verifier dan trust-all SSL context
 * guna mencegah kerentanan MITM (Issue C6).
 */
@Slf4j
public class CustomClientHttpRequestFactory extends SimpleClientHttpRequestFactory {

    private final boolean disableSslVerification;

    public CustomClientHttpRequestFactory() {
        this.setConnectTimeout(10000);
        this.setReadTimeout(30000);
        this.disableSslVerification = false;
    }

    public CustomClientHttpRequestFactory(int connectTimeout, int readTimeout, boolean disableSslVerification) {
        this.setConnectTimeout(connectTimeout);
        this.setReadTimeout(readTimeout);
        this.disableSslVerification = disableSslVerification;
        if (disableSslVerification) {
            log.warn("CustomClientHttpRequestFactory initialized with disableSslVerification=true. Standard JVM verification is maintained for security.");
        }
    }

    public boolean isDisableSslVerification() {
        return disableSslVerification;
    }
}

