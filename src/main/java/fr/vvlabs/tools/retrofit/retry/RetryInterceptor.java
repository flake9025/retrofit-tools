package fr.vvlabs.tools.retrofit.retry;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author vvillain
 * Retry Interceptor.
 * Helper to build synchronous retry-able requests for Retrofit 2
 */
@Slf4j
public class RetryInterceptor implements Interceptor {

    @Override
    public Response intercept(final Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        int retryCount = 0;
        while (!RetryPolicy.isSuccessful(response.code()) && retryCount < RetryPolicy.MAX_RETRIES) {
            retryCount++;
            RetryInterceptor.log.debug("Response KO(status={}) Retrying API call, attempt {}/{}", response.code(),
                        retryCount, RetryPolicy.MAX_RETRIES + ")");
            try {
                RetryInterceptor.log.trace("Wait for " + RetryPolicy.RETRY_INTERVAL_MILLISECONDS);
                Thread.sleep(RetryPolicy.RETRY_INTERVAL_MILLISECONDS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // build a new request
            Request newRequest = request.newBuilder().build();
            // close previous response
            response.close();
            response = chain.proceed(newRequest);
        }
        if (retryCount == RetryPolicy.MAX_RETRIES) {
            throw new RetryCallbackException(response.code(),
                        "Request failed after " + RetryPolicy.MAX_RETRIES + " attempts.");
        }
        RetryInterceptor.log.debug("API Call successful after " + (retryCount + 1) + " attempt(s)");
        // just pass the original response on
        return response;
    }
}
