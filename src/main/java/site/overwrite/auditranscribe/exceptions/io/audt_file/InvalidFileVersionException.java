/*
 * InvalidFileVersionException.java
 *
 * Created on 2022-07-11
 * Updated on 2022-07-11
 *
 * Description: Exception to mark that a requested file version is invalid.
 */

package site.overwrite.auditranscribe.exceptions.io.audt_file;

/**
 * Exception to mark that a requested file version is invalid.
 */
public class InvalidFileVersionException extends Exception {
    public InvalidFileVersionException() {
    }

    public InvalidFileVersionException(String message) {
        super(message);
    }

    public InvalidFileVersionException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidFileVersionException(Throwable cause) {
        super(cause);
    }
}
