/*
 * FFmpegCommandFailedException.java
 *
 * Created on 2022-07-03
 * Updated on 2022-07-03
 *
 * Description: Exception that is thrown when the ffmpeg command fails to run.
 */

package site.overwrite.auditranscribe.exceptions.audio;

/**
 * Exception that is thrown when the ffmpeg command fails to run.
 */
public class FFmpegCommandFailedException extends RuntimeException {
    public FFmpegCommandFailedException() {
        super();
    }

    public FFmpegCommandFailedException(String message) {
        super(message);
    }

    public FFmpegCommandFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public FFmpegCommandFailedException(Throwable cause) {
        super(cause);
    }

    protected FFmpegCommandFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
