package com.kmkbe.core.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * DEPRECATED & NEUTRALIZED FOR SECURITY REASONS (Issue C6).
 * Sebelumnya komponen ini mematikan verifikasi SSL secara global untuk seluruh JVM
 * (HttpsURLConnection.setDefaultSSLSocketFactory dan promiscuous HostnameVerifier),
 * membuka celah Man-In-The-Middle (MITM) pada seluruh koneksi outbound.
 *
 * Sekarang dinetralkan agar JVM selalu mematuhi truststore dan hostname verification standar.
 */
@Component
@Slf4j
public class ByPassSslComponent {

    public ByPassSslComponent() {
        log.info("ByPassSslComponent has been neutralized for security compliance. Standard SSL validation is enforced.");
    }
}

