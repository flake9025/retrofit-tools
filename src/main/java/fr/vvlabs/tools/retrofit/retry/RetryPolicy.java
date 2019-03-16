package fr.vvlabs.tools.retrofit.retry;

import lombok.extern.slf4j.Slf4j;

/**
 * @author vvillain
 * Retry Policy.
 * Helper to check if a response should be considered as success for Retrofit 2
 */
@Slf4j
public class RetryPolicy {

    // ===========================================================
    // Constants
    // ===========================================================

    public static final int MAX_RETRIES = 5;
    public static final int RETRY_INTERVAL_MILLISECONDS = 200;

    // ===========================================================
    // Constructors
    // ===========================================================

    private RetryPolicy() {
    }

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * Checks if is successful.
     *
     * @param responseCode
     *            the response code
     * @return true, if is successful
     */
    public static boolean isSuccessful(final int responseCode) {
        // response.isSuccessful checks code between 200 & 300, here we want 400 to be ok
        int statusCode = responseCode / 100;
        switch (statusCode) {
        case 2: // 2xx Everythings OK
            return true;
        case 4: // 4xx
            // Malformed query or could be a SSL Cert problem, please check if everything is ok
            // on startup (no
            // Error message on Unable to load keystore)
            // no need to loop for this kind of error
            RetryPolicy.log.error(
                        "Retry policy : got code {} : no retry - Please check for eventual SSL Cert problem at startup (msg Unable to load keystore), or malformed query",
                        statusCode);
            return true;
        default:
            // For all others code, try again
            RetryPolicy.log.warn("Retry policy : got code {} : retrying", statusCode);
            return false;
        }
    }
}
