/*
 * AudioDataObject0x000B00.java
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

package app.auditranscribe.io.audt_file.v0x000B00.data_encapsulators;

import app.auditranscribe.io.audt_file.base.data_encapsulators.AudioDataObject;

/**
 * Data object that stores the audio data.
 */
public class AudioDataObject0x000B00 extends AudioDataObject {
    /**
     * Initialization method for the audio data object.
     *
     * @param mp3Bytes          The bytes of the MP3 audio file.
     * @param sampleRate        Sample rate of the audio file
     * @param totalDurationInMS Total duration of the audio in <b>milliseconds</b>.
     */
    public AudioDataObject0x000B00(
            byte[] mp3Bytes, double sampleRate, int totalDurationInMS
    ) {
        this.mp3Bytes = mp3Bytes;
        this.sampleRate = sampleRate;
        this.totalDurationInMS = totalDurationInMS;
    }

    // Public methods
    @Override
    public int numBytesNeeded() {
        return 4 +  // Section ID
                (4 + mp3Bytes.length) +  // +4 for the length of the MP3 audio data
                8 +   // Sample rate
                4 +   // Total duration in milliseconds
                4;    // EOS delimiter
    }
}
