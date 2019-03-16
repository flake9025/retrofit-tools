package fr.vvlabs.tools.retrofit.retry;

import java.io.IOException;

/**
 * @author vvillain
 * Exception thrown when the connection has failed after a few retry attempts
 */
public class RetryCallbackException extends IOException {

    private static final long serialVersionUID = 3950356158846058941L;

    // ===========================================================
    // Constructors
    // ===========================================================

    public RetryCallbackException(final int status,
                final String message) {
        super(message + " - status=[" + status + "]");
    }
}
