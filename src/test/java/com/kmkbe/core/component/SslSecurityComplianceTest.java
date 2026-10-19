package com.kmkbe.core.component;

import com.kmkbe.core.factory.CustomClientHttpRequestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.net.ssl.HttpsURLConnection;

import static org.assertj.core.api.Assertions.assertThat;

class SslSecurityComplianceTest {

    @Test
    void byPassSslComponentDoesNotAlterGlobalHttpsUrlConnectionDefaults() {
        var defaultSocketFactoryBefore = HttpsURLConnection.getDefaultSSLSocketFactory();
        var defaultHostnameVerifierBefore = HttpsURLConnection.getDefaultHostnameVerifier();

        // Inisialisasi ByPassSslComponent yang telah dinetralkan
        ByPassSslComponent component = new ByPassSslComponent();
        assertThat(component).isNotNull();

        var defaultSocketFactoryAfter = HttpsURLConnection.getDefaultSSLSocketFactory();
        var defaultHostnameVerifierAfter = HttpsURLConnection.getDefaultHostnameVerifier();

        // Memastikan komponen tidak mengganti SocketFactory atau HostnameVerifier global menjadi trust-all
        assertThat(defaultSocketFactoryAfter).isSameAs(defaultSocketFactoryBefore);
        assertThat(defaultHostnameVerifierAfter).isSameAs(defaultHostnameVerifierBefore);
    }

    @Test
    void customClientHttpRequestFactoryDefaultsToSafeSslVerification() {
        CustomClientHttpRequestFactory factory = new CustomClientHttpRequestFactory();

        assertThat(factory.isDisableSslVerification()).isFalse();

        int connectTimeout = (int) ReflectionTestUtils.getField(factory, "connectTimeout");
        int readTimeout = (int) ReflectionTestUtils.getField(factory, "readTimeout");

        // Memastikan timeout wajar (bukan 12.000.000 ms)
        assertThat(connectTimeout).isLessThanOrEqualTo(30000);
        assertThat(readTimeout).isLessThanOrEqualTo(60000);
    }
}
