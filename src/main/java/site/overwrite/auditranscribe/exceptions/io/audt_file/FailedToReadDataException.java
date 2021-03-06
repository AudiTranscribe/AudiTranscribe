/*
 * FailedToReadDataException.java
 *
 * Created on 2022-05-02
 * Updated on 2022-06-23
 *
 * Description: Exception to mark that the program failed to read the data stored in an AUDT file successfully.
 */

package site.overwrite.auditranscribe.exceptions.io.audt_file;

/**
 * Exception to mark that the program failed to read the data stored in an AUDT file successfully.
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
