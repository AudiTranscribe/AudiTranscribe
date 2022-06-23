/*
 * FFmpegNotFoundException.java
 *
 * Created on 2022-06-03
 * Updated on 2022-06-23
 *
 * Description: Exception that is thrown when the ffmpeg binary path is not found.
 */

package site.overwrite.auditranscribe.exceptions.audio;

/**
 * Exception that is thrown when the ffmpeg binary path is not found.
 */
public class FFmpegNotFoundException extends Exception {
    public FFmpegNotFoundException() {
        super();
    }

    public FFmpegNotFoundException(String message) {
        super(message);
    }

    public FFmpegNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FFmpegNotFoundException(Throwable cause) {
        super(cause);
    }

    protected FFmpegNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
