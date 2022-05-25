/*
 * IncorrectFileFormatException.java
 *
 * Created on 2022-05-02
 * Updated on 2022-05-25
 *
 * Description: Exception to mark that the file does not have the AUDT file format.
 */

package site.overwrite.auditranscribe.exceptions;

/**
 * Exception to mark that the file does not have the AUDT file format.
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
