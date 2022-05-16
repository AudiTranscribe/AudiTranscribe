/*
 * FailedToReadDataException.java
 *
 * Created on 2022-05-02
 * Updated on 2022-05-10
 *
 * Description: Exception to mark that the program failed to read the data stored in the file successfully.
 */

package site.overwrite.auditranscribe.io.audt_file.exceptions;

/**
 * Exception to mark that the program failed to read the data stored in the file successfully.
 */
public class FailedToReadDataException extends Exception {
    public FailedToReadDataException() {
    }

    public FailedToReadDataException(String message) {
        super(message);
    }

    public FailedToReadDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedToReadDataException(Throwable cause) {
        super(cause);
    }
}
