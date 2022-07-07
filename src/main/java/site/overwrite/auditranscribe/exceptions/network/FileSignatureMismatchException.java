/*
 * FileSignatureMismatchException.java
 *
 * Created on 2022-07-07
 * Updated on 2022-07-07
 *
 * Description: Exception to mark when the calculated file hash does not match the expected file
 *              hash.
 */

package site.overwrite.auditranscribe.exceptions.network;

/**
 * Exception to mark when the calculated file hash does not match the expected file hash.
 */
public class FileSignatureMismatchException extends Exception {
    public FileSignatureMismatchException() {
        super();
    }

    public FileSignatureMismatchException(String message) {
        super(message);
    }

    public FileSignatureMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileSignatureMismatchException(Throwable cause) {
        super(cause);
    }

    protected FileSignatureMismatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
