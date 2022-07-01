/*
 * FilterNotFoundException.java
 *
 * Created on 2022-07-01
 * Updated on 2022-07-01
 *
 * Description: Exception thrown if the audio filter file could not be found.
 */

package site.overwrite.auditranscribe.exceptions.audio;

/**
 * Exception thrown if the audio filter file could not be found.
 */
public class FilterNotFoundException extends RuntimeException {
    public FilterNotFoundException() {
        super();
    }

    public FilterNotFoundException(String message) {
        super(message);
    }

    public FilterNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FilterNotFoundException(Throwable cause) {
        super(cause);
    }

    protected FilterNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
