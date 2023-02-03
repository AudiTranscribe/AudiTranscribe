/*
 * AudioDataObject0x00050002.java
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

package app.auditranscribe.io.audt_file.v0x00050002.data_encapsulators;

import app.auditranscribe.io.CompressionHandlers;
import app.auditranscribe.io.audt_file.base.data_encapsulators.AudioDataObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Data object that stores the audio data.
 */
public class AudioDataObject0x00050002 extends AudioDataObject {
    // Attributes
    private final String audioFileName;
    public final byte[] compressedOriginalMP3Bytes;

    /**
     * Initialization method for the audio data object.
     *
     * @param compressedMP3Bytes The LZ4 compressed bytes of the MP3 audio file.
     * @param sampleRate         Sample rate of the audio file
     * @param totalDurationInMS  Total duration of the audio in <b>milliseconds</b>.
     * @param audioFileName      The name of the audio file.
     */
    public AudioDataObject0x00050002(
            byte[] compressedMP3Bytes, double sampleRate, int totalDurationInMS, String audioFileName
    ) {
        this.sampleRate = sampleRate;
        this.totalDurationInMS = totalDurationInMS;
        this.mp3Bytes = CompressionHandlers.lz4DecompressFailSilently(compressedMP3Bytes);

        // No longer in the superclass
        this.audioFileName = audioFileName;
        this.compressedOriginalMP3Bytes = compressedMP3Bytes;
    }

    // Getter/setter methods

    /**
     * Method that gets the audio file name.<br>
     * This method needs to be implemented to maintain backwards compatability with version
     * 0x00050002.
     *
     * @return Audio file name.
     */
    public String getAudioFileName() {
        return audioFileName;
    }

    // Public methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AudioDataObject0x00050002 that = (AudioDataObject0x00050002) o;
        return Objects.equals(audioFileName, that.audioFileName) &&
                Arrays.equals(compressedOriginalMP3Bytes, that.compressedOriginalMP3Bytes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), audioFileName);
        result = 31 * result + Arrays.hashCode(compressedOriginalMP3Bytes);
        return result;
    }

    @Override
    public int numBytesNeeded() {
        return 4 +  // Section ID
                (4 + compressedOriginalMP3Bytes.length) +  // +4 for the length of the MP3 audio data
                8 +   // Sample rate
                4 +   // Total duration in milliseconds
                (4 + audioFileName.getBytes().length) +  // String length + string bytes of audio file name
                4;    // EOS delimiter
    }
}
