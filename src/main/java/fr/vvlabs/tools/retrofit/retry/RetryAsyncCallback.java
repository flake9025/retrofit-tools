package fr.vvlabs.tools.retrofit.retry;

import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author vvillain
 * Retry Async Callback.
 *
 * @param <T> the generic type 
 * Helper to build asynchronous retry-callback for Retrofit 2
 */
@Slf4j
public abstract class RetryAsyncCallback<T> implements Callback<T> {

	// ===========================================================
	// Fields
	// ===========================================================

	private final Call<T> call;
	private int retryCount = 0;

	// ===========================================================
	// Constructors
	// ===========================================================

	/**
	 * Instantiates a new retry callback.
	 *
	 * @param call    the call
	 * @param retries the retries
	 */
	public RetryAsyncCallback(final Call<T> call) {
		this.call = call;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void onResponse(final Call<T> call, final Response<T> response) {
		if (RetryPolicy.isSuccessful(response.code())) {
			RetryAsyncCallback.log.info("response is successful !");
			onSuccess(call, response);
		} else {
			if (this.retryCount++ < RetryPolicy.MAX_RETRIES) {

				try {
					RetryAsyncCallback.log.trace("Wait for " + RetryPolicy.RETRY_INTERVAL_MILLISECONDS);
					Thread.sleep(RetryPolicy.RETRY_INTERVAL_MILLISECONDS);
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				}

				RetryAsyncCallback.log.debug("Response KO(status={}) Retrying API call, attempt {}/{}", response.code(),
						this.retryCount, RetryPolicy.MAX_RETRIES + ")");
				this.call.clone().enqueue(this);
			} else {
				onError(call, new RetryCallbackException(response.code(),
						"Request failed after " + RetryPolicy.MAX_RETRIES + " attempts."));
			}
		}
	}

	@Override
	public void onFailure(final Call<T> call, final Throwable t) {
		RetryAsyncCallback.log.error(t.getMessage(), t);
		if (this.retryCount++ < RetryPolicy.MAX_RETRIES) {
			RetryAsyncCallback.log
					.debug("Retrying API Call -  (" + this.retryCount + " / " + RetryPolicy.MAX_RETRIES + ")");
			this.call.clone().enqueue(this);
		} else {
			onError(call, t);
		}
	}

	public abstract void onSuccess(final Call<T> call, final Response<T> response);

	public abstract void onError(final Call<T> call, final Throwable t);
}
