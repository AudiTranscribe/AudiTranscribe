/*
 * AudioDataObject.java
 *
 * Created on 2022-05-06
 * Updated on 2022-07-11
 *
 * Description: Data object that stores the audio data.
 */

package site.overwrite.auditranscribe.io.audt_file.data_encapsulators;

import java.util.Arrays;
import java.util.Objects;

/**
 * Data object that stores the audio data.
 */
public abstract class AudioDataObject extends AbstractAUDTDataObject {
    // Constants
    public static final int SECTION_ID = 3;

    // Attributes
    public byte[] compressedMP3Bytes;
    public double sampleRate;
    public int totalDurationInMS;
    public String audioFileName;

    // Overwritten methods

    @Override
    public int numBytesNeeded() {
        return 4 +  // Section ID
                (4 + compressedMP3Bytes.length) +  // +4 for the length of the MP3 audio data
                8 +   // Sample rate
                4 +   // Total duration in milliseconds
                (4 + audioFileName.getBytes().length) +  // String length + string bytes of audio file name
                4;    // EOS delimiter
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioDataObject that = (AudioDataObject) o;
        return (
                Double.compare(that.sampleRate, sampleRate) == 0 &&
                        totalDurationInMS == that.totalDurationInMS &&
                        Arrays.equals(compressedMP3Bytes, that.compressedMP3Bytes) &&
                        audioFileName.equals(that.audioFileName)
        );
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(sampleRate, totalDurationInMS, audioFileName);
        result = 31 * result + Arrays.hashCode(compressedMP3Bytes);
        return result;
    }
}
