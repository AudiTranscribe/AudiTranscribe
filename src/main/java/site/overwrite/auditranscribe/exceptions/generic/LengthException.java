/*
 * LengthException.java
 *
 * Created on 2022-05-25
 * Updated on 2022-06-23
 *
 * Description: Exception to mark when an array does not have an appropriate length (i.e. length
 *              check failed).
 */

package site.overwrite.auditranscribe.exceptions.generic;

/**
 * Exception to mark when an array does not have an appropriate length (i.e. length check failed).
 */
public class LengthException extends RuntimeException {
    public LengthException() {
    }

    public LengthException(String message) {
        super(message);
    }

    public LengthException(String message, Throwable cause) {
        super(message, cause);
    }

    public LengthException(Throwable cause) {
        super(cause);
    }

    public LengthException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
