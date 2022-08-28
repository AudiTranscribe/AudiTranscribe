/*
 * FFmpegNotFoundException.java
 * Description: Exception that is thrown when the ffmpeg binary path is not found.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public Licence as published by the Free Software Foundation, either version 3 of the
 * Licence, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public Licence for more details.
 *
 * You should have received a copy of the GNU General Public Licence along with this program. If
 * not, see <https://www.gnu.org/licenses/>
 *
 * Copyright © AudiTranscribe Team
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
