package de.plimplom.addonreader.util;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;

@Slf4j
public class HttpClientFactory {

    public static HttpClient createDefaultClient() {
        return createSecureClient();
    }

    /**
     * Creates a secure HTTP client with proper certificate validation.
     */
    private static HttpClient createClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Creates an HTTP client that trusts all certificates.
     * WARNING: This should only be used for development or in controlled environments.
     */
    private static HttpClient createSecureClient() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
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

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("Error creating SSL context", e);
            return createClient(); // Fallback to standard client
        }
    }
}
