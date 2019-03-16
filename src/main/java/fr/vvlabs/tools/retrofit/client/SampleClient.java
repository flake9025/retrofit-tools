package fr.vvlabs.tools.retrofit.client;

import java.io.File;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLSocketFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.vvlabs.tools.retrofit.mapper.JacksonMapper;
import fr.vvlabs.tools.retrofit.retry.RetryInterceptor;
import fr.vvlabs.tools.retrofit.security.SSLFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @author vvillain
 * Sample Retrofit REST Client with certificate file, password, and TLS Connection
 */
@Slf4j
public class SampleClient {

    // ===========================================================
    // Constants
    // ===========================================================

    private static final String SECURITY_PROTOCOL = "TLSv1.2";
    private static final int HTTP_POOL_MAX_IDLE_CONNECTIONS = 10;
    private static final int HTTP_POOL_IDLE_TIME_MINUTES = 5;
    private static final int HTTP_TIMEOUT_IN_MS = 60000;

    // ===========================================================
    // Fields
    // ===========================================================

    private Retrofit client;
    private ObjectMapper jacksonMapper;
    private String baseUrl;
    private String certificatePath;
    private String certificatePwrd;
    private long timeoutInMs;
    private Map<Class<?>, Object> serviceCache;
    private AtomicBoolean clientInitialized;

    // ===========================================================
    // Constructors
    // ===========================================================

    public SampleClient() {
        this.timeoutInMs = SampleClient.HTTP_TIMEOUT_IN_MS;
        this.serviceCache = new HashMap<>();
        // Check environment
        initServerConfiguration();
        // Init jackson mapping
        this.jacksonMapper = new JacksonMapper();
        this.clientInitialized = new AtomicBoolean(false);
    }

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * Check environment variables.
     */
    private void initServerConfiguration() {
        SampleClient.log.debug("initServerConfiguration() ...");

        String certificatePathEnv = System.getenv("CERT_FILE");
        if (certificatePathEnv != null) {
            // Load certificate from path
            if (!new File(certificatePathEnv).exists()) {
                SampleClient.log.error("CERT_FILE not found: {}", certificatePathEnv);
            }
        }else {
            throw new MissingResourceException("Certificate file variable missing", this.getClass().getName(),
                    "CERT_FILE");
        }
        this.certificatePwrd = System.getenv("CERT_PASSWORD");

        this.baseUrl = System.getenv("API_URL");
        if (this.baseUrl == null) {
        	throw new MissingResourceException("server URL variable missing", this.getClass().getName(),
                    "API_URL");
        }
        if (!this.baseUrl.endsWith("/")) {
            this.baseUrl += "/";
        }
    }

    /**
     * Gets the service.
     *
     * @param <T>
     *            the generic type
     * @param service
     *            the service
     * @return the service
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(final Class<T> service) {
        synchronized (this.clientInitialized) {
            if (!this.clientInitialized.get()) {
                initClient();
            }
        }
        return (T) this.serviceCache.computeIfAbsent(service, this::createService);
    }

    /**
     * Creates the service.
     *
     * @param <T>
     *            the generic type
     * @param service
     *            the service
     * @return the t
     */
    private <T> T createService(final Class<T> service) {
        T serviceImpl = this.client.create(service);
        this.serviceCache.put(service, serviceImpl);
        return serviceImpl;
    }

    /**
     * Inits the client.
     *
     * @throws GeneralSecurityException
     */
    private void initClient() {
        try {
            SampleClient.log.info("initClient() with ApiURL={} ", this.baseUrl);

            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

            // Init SSL Configuration
            SSLFactory sslFactory = new SSLFactory();
            SSLSocketFactory sslSocketFactory = new SSLFactory().buildSSLSocketFactory(this.certificatePath,
                        this.certificatePwrd, SampleClient.SECURITY_PROTOCOL);
            okHttpClientBuilder.sslSocketFactory(sslSocketFactory, sslFactory.getTrustManager());

            // Set timeouts
            okHttpClientBuilder.connectTimeout(this.timeoutInMs, TimeUnit.MILLISECONDS)
                        .readTimeout(this.timeoutInMs, TimeUnit.MILLISECONDS)
                        .writeTimeout(this.timeoutInMs, TimeUnit.MILLISECONDS);

            // Set retry policy
            okHttpClientBuilder.addInterceptor(new RetryInterceptor());

            // Create Pool
            ConnectionPool pool = new ConnectionPool(SampleClient.HTTP_POOL_MAX_IDLE_CONNECTIONS,
                        SampleClient.HTTP_POOL_IDLE_TIME_MINUTES, TimeUnit.MINUTES);
            okHttpClientBuilder.connectionPool(pool);

            // Finally create HTTP client
            this.client = new Retrofit.Builder().client(okHttpClientBuilder.build()).baseUrl(this.baseUrl)
                        .addConverterFactory(JacksonConverterFactory.create(this.jacksonMapper)).build();

            this.serviceCache = new HashMap<>();

            this.clientInitialized.set(true);
        } catch (Exception e) {
            SampleClient.log.error(e.getMessage(), e);
        }
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public String getBaseUrl() {
        return this.baseUrl;
    }
}
