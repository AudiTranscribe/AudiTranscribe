/*
 * FormatException.java
 *
 * Created on 2022-05-25
 * Updated on 2022-06-23
 *
 * Description: Exception to mark when a string does not have the correct format.
 */

package site.overwrite.auditranscribe.exceptions.generic;

/**
 * Exception to mark when a string does not have the correct format.
 */
public class FormatException extends RuntimeException {
    public FormatException() {
    }

    public FormatException(String message) {
        super(message);
    }

    public FormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormatException(Throwable cause) {
        super(cause);
    }

    public FormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
