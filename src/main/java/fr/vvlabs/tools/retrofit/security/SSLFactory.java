package fr.vvlabs.tools.retrofit.security;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author vvillain
 * A factory for creating SSL socket and keystore.
 */
@Slf4j
public class SSLFactory {

    // ===========================================================
    // Fields
    // ===========================================================

    @Getter
    private X509TrustManager trustManager;

    // ===========================================================
    // Constructors
    // ===========================================================

    public SSLFactory() {
        this.trustManager = new SelfSignedX509TrustManager();
    }

    /**
     * Builds the SSL factory.
     *
     * @param certificatePath
     *            the certificate path
     * @param certificatePwrd
     *            the certificate pwrd
     * @param protocol
     *            the protocol
     * @return the SSL socket factory
     */
    public SSLSocketFactory buildSSLSocketFactory(final String certificatePath, final String certificatePwrd,
                final String protocol) {

        SSLSocketFactory sslFactory = null;
        try {
            // Load Keystore with password
            KeyStore keyStore = buildKeyStore(certificatePath, certificatePwrd);

            // Create a Key manager to authenticate the Client.
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, certificatePwrd.toCharArray());
            KeyManager[] keyManagers = kmf.getKeyManagers();

            // Trust Manager for Self-Signed Certificate
            X509TrustManager[] trustAllCerts = new X509TrustManager[] { this.trustManager };

            // Init SSL Context with the protocol, key and trust manager
            SSLContext sslContext = SSLContext.getInstance(protocol);
            sslContext.init(keyManagers, trustAllCerts, new SecureRandom());

            // Create SSL Factory with the trust manager, keystore
            sslFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            SSLFactory.log.error(e.getMessage(), e);
        }
        return sslFactory;
    }

    /**
     * Builds the key store.
     *
     * @param keystoreFile
     *            the keystore file
     * @param keystorePwd
     *            the keystore pwd
     * @return the key store
     */
    public KeyStore buildKeyStore(final String keystoreFile, final String keystorePwd) {
        KeyStore keyStore = null;
        try (InputStream in = getClass().getResourceAsStream(keystoreFile)) {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            char[] password = keystorePwd.toCharArray();
            keyStore.load(in, password);
        } catch (Exception e) {
            SSLFactory.log.error(e.getMessage(), e);
        }
        return keyStore;
    }
}
