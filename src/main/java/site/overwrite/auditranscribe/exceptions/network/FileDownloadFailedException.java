/*
 * FileDownloadFailedException.java
 *
 * Created on 2022-07-07
 * Updated on 2022-07-07
 *
 * Description: Exception thrown to mark that the file download failed.
 */

package site.overwrite.auditranscribe.exceptions.network;

/**
 * Exception thrown to mark that the file download failed.
 */
public class FileDownloadFailedException extends Exception {
    public FileDownloadFailedException() {
        super();
    }

    public FileDownloadFailedException(String message) {
        super(message);
    }

    public FileDownloadFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileDownloadFailedException(Throwable cause) {
        super(cause);
    }

    protected FileDownloadFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
