package com.kmkbe.core.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.lang.NonNull;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Slf4j
public class CustomClientHttpRequestFactory extends SimpleClientHttpRequestFactory {
    private static final HostnameVerifier PROMISCUOUS_VERIFIER = (hostname, session) -> true;

    private static final TrustManager[] ALL_CERT_TRUST_MANAGER = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {

                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {

                }
            }
    };

    private static SSLContext ALL_CERT_TRUST_SSLCONTEXT = null;

    static {
        try {
            ALL_CERT_TRUST_SSLCONTEXT = SSLContext.getInstance("SSL");
            ALL_CERT_TRUST_SSLCONTEXT.init(null, ALL_CERT_TRUST_MANAGER, new SecureRandom());
        } catch (Exception e) {
            log.error("Error disabling SSL verification");
            log.error(e.getMessage(), e);
        }
    }


    private final boolean disableSslVerification;

    public CustomClientHttpRequestFactory() {
        this.setConnectTimeout(12000000);
        this.setReadTimeout(12000000);
        this.disableSslVerification = true;
    }

    public CustomClientHttpRequestFactory(int connectTimeout, int readTimeout, boolean disableSslVerification) {
        this.setConnectTimeout(connectTimeout);
        this.setReadTimeout(readTimeout);
        this.disableSslVerification = disableSslVerification;
    }

    @Override
    protected void prepareConnection(@NonNull HttpURLConnection connection, @NonNull String httpMethod) throws IOException {
        if (disableSslVerification && ALL_CERT_TRUST_SSLCONTEXT != null && connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setHostnameVerifier(PROMISCUOUS_VERIFIER);
            ((HttpsURLConnection) connection).setSSLSocketFactory(ALL_CERT_TRUST_SSLCONTEXT.getSocketFactory());
        }
        super.prepareConnection(connection, httpMethod);
    }
}
