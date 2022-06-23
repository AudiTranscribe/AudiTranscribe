/*
 * OutdatedFileFormatException.java
 *
 * Created on 2022-06-23
 * Updated on 2022-06-23
 *
 * Description: Exception to mark that an AUDT file has an outdated file format.
 */

package site.overwrite.auditranscribe.exceptions.io.audt_file;

/**
 * Exception to mark that an AUDT file has an outdated file format.
 */
public class OutdatedFileFormatException extends Exception {
    public OutdatedFileFormatException() {
    }

    public OutdatedFileFormatException(String message) {
        super(message);
    }

    public OutdatedFileFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public OutdatedFileFormatException(Throwable cause) {
        super(cause);
    }
}
