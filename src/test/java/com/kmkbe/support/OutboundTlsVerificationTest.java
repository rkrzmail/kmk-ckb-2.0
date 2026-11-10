package com.kmkbe.support;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Regresi test untuk security issue #4 (SECURITY.md):
 * setelah trust-all SSL dihapus, koneksi HTTPS outbound wajib lolos
 * verifikasi dengan truststore JVM default.
 *
 * Test positif: host outbound nyata di config prod (Confins R3 & e-sign
 * ad-ins, CA publik Sectigo/GoDaddy) WAJIB lolos handshake tanpa bypass.
 *
 * Test kontrol negatif: host dengan sertifikat self-signed / hostname
 * salah (badssl.com) WAJIB DITOLAK. Inilah detektor trust-all — bila
 * bypass trust-all kembali dipasang (mis. ByPassSslComponent hidup lagi),
 * kontrol negatif akan lolos handshake dan test ini GAGAL.
 */
class OutboundTlsVerificationTest {

  private void openVerified(String url) throws Exception {
    HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
    connection.setConnectTimeout(10_000);
    connection.setReadTimeout(10_000);
    try (var in = connection.getInputStream()) {
      // memaksa handshake TLS penuh + response; kegagalan verifikasi
      // (self-signed / expired / hostname mismatch) melempar
      // SSLHandshakeException dengan truststore default
      new Scanner(in, java.nio.charset.StandardCharsets.UTF_8).findWithinHorizon(".", 64);
    } finally {
      connection.disconnect();
    }
  }

  @Test
  void confinsR3HandshakeIsVerifiedByDefaultTruststore() {
    assertThatCode(() -> openVerified("https://confinsr3.csulfinance.com/"))
      .doesNotThrowAnyException();
  }

  @Test
  void adInsEsignHandshakeIsVerifiedByDefaultTruststore() {
    assertThatCode(() -> openVerified("https://gdkwebserver.ad-ins.com/"))
      .doesNotThrowAnyException();
  }

  @Test
  void selfSignedCertificateMustBeRejected() {
    // kontrol negatif: gagal jika trust-all aktif kembali
    assertThatThrownBy(() -> openVerified("https://self-signed.badssl.com/"))
      .isInstanceOf(java.io.IOException.class);
  }

  @Test
  void wrongHostnameCertificateMustBeRejected() {
    // kontrol negatif hostname verification: gagal jika promiscuous verifier aktif
    assertThatThrownBy(() -> openVerified("https://wrong.host.badssl.com/"))
      .isInstanceOf(java.io.IOException.class);
  }
}
