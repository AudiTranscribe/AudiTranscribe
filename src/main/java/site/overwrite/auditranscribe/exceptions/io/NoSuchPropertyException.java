/*
 * NoSuchPropertyException.java
 *
 * Created on 2022-05-25
 * Updated on 2022-06-23
 *
 * Description: Exception to mark when a property file does not have the specified property.
 */

package site.overwrite.auditranscribe.exceptions.io;

/**
 * Exception to mark when a property file does not have the specified property.
 */
public class NoSuchPropertyException extends RuntimeException {
    public NoSuchPropertyException() {
    }

    public NoSuchPropertyException(String message) {
        super(message);
    }

    public NoSuchPropertyException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchPropertyException(Throwable cause) {
        super(cause);
    }

    public NoSuchPropertyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
