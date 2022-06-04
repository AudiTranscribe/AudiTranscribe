/*
 * FFmpegNotFound.java
 *
 * Created on 2022-06-03
 * Updated on 2022-06-03
 *
 * Description: Exception that is thrown when the ffmpeg binary path is not found.
 */

package site.overwrite.auditranscribe.audio.ffmpeg;

/**
 * Exception that is thrown when the ffmpeg binary path is not found.
 */
public class FFmpegNotFound extends Exception {
    public FFmpegNotFound() {
        super();
    }

    public FFmpegNotFound(String message) {
        super(message);
    }

    public FFmpegNotFound(String message, Throwable cause) {
        super(message, cause);
    }

    public FFmpegNotFound(Throwable cause) {
        super(cause);
    }

    protected FFmpegNotFound(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
