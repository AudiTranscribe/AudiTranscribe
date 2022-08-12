/*
 * APIServerTimeoutException.java
 *
 * Created on 2022-08-12
 * Updated on 2022-08-12
 *
 * Description: Exception to mark when the API server timed out.
 */

package site.overwrite.auditranscribe.exceptions.network;

/**
 * Exception to mark when the API server timed out.
 */
public class APIServerTimeoutException extends Exception {
    public APIServerTimeoutException() {
        super();
    }

    public APIServerTimeoutException(String message) {
        super(message);
    }

    public APIServerTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public APIServerTimeoutException(Throwable cause) {
        super(cause);
    }

    protected APIServerTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
