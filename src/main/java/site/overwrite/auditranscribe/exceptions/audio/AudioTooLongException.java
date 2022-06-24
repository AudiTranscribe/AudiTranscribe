/*
 * AudioTooLongException.java
 *
 * Created on 2022-06-21
 * Updated on 2022-06-23
 *
 * Description: Exception thrown when an audio file is too long.
 */

package site.overwrite.auditranscribe.exceptions.audio;

/**
 * Exception thrown when an audio file is too long.
 */
public class AudioTooLongException extends Exception {
    public AudioTooLongException() {
        super();
    }

    public AudioTooLongException(String message) {
        super(message);
    }

    public AudioTooLongException(String message, Throwable cause) {
        super(message, cause);
    }

    public AudioTooLongException(Throwable cause) {
        super(cause);
    }

    protected AudioTooLongException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
