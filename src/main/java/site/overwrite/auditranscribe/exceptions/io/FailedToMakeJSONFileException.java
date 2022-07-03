/*
 * NoSuchPropertyException.java
 *
 * Created on 2022-07-03
 * Updated on 2022-07-03
 *
 * Description: Exception to mark when a JSON file failed to be created.
 */

package site.overwrite.auditranscribe.exceptions.io;

/**
 * Exception to mark when a JSON file failed to be created.
 */
public class FailedToMakeJSONFileException extends RuntimeException {
    public FailedToMakeJSONFileException() {
    }

    public FailedToMakeJSONFileException(String message) {
        super(message);
    }

    public FailedToMakeJSONFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedToMakeJSONFileException(Throwable cause) {
        super(cause);
    }

    public FailedToMakeJSONFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
