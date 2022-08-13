/*
 * APIServerException.java
 *
 * Created on 2022-08-12
 * Updated on 2022-08-13
 *
 * Description: Exception to mark when the API server connection encounters a problem, like a
 *              timeout or a refusal to connect.
 */

package site.overwrite.auditranscribe.exceptions.network;

/**
 * Exception to mark when the API server connection encounters a problem, like a timeout or a
 * refusal to connect.
 */
public class APIServerException extends Exception {
    public APIServerException() {
        super();
    }

    public APIServerException(String message) {
        super(message);
    }

    public APIServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public APIServerException(Throwable cause) {
        super(cause);
    }

    protected APIServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
