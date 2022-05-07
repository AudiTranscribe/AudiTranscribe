/*
 * IncorrectFileFormatException.java
 *
 * Created on 2022-05-02
 * Updated on 2022-05-02
 *
 * Description: Exception to mark that the file format is incorrect.
 */

package site.overwrite.auditranscribe.io.exceptions;

/**
 * Exception to mark that the file format is incorrect.
 */
public class IncorrectFileFormatException extends Exception {
    public IncorrectFileFormatException() {
    }

    public IncorrectFileFormatException(String message) {
        super(message);
    }

    public IncorrectFileFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectFileFormatException(Throwable cause) {
        super(cause);
    }
}
