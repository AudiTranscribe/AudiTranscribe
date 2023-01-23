/*
 * AudioDataObject0x000B0001.java
 * Description: Data object that stores the audio data.
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

package app.auditranscribe.io.audt_file.v0x000B0001.data_encapsulators;

import app.auditranscribe.io.audt_file.v0x00090002.data_encapsulators.AudioDataObject0x00090002;

/**
 * Data object that stores the audio data.
 */
public class AudioDataObject0x000B0001 extends AudioDataObject0x00090002 {
    /**
     * Initialization method for the audio data object.
     *
     * @param compressedOriginalMP3Bytes The LZ4 compressed bytes of the original MP3 audio file.
     * @param compressedSlowedMP3Bytes   The LZ4 compressed bytes of the slowed MP3 audio file.
     * @param sampleRate                 Sample rate of the audio file
     * @param totalDurationInMS          Total duration of the audio in <b>milliseconds</b>.
     */
    public AudioDataObject0x000B0001(byte[] compressedOriginalMP3Bytes, byte[] compressedSlowedMP3Bytes, double sampleRate, int totalDurationInMS) {
        super(compressedOriginalMP3Bytes, compressedSlowedMP3Bytes, sampleRate, totalDurationInMS);
    }
}
