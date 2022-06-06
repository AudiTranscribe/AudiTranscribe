/*
 * AudioDataObject.java
 *
 * Created on 2022-05-06
 * Updated on 2022-06-06
 *
 * Description: Data object that stores the audio data.
 */

package site.overwrite.auditranscribe.io.audt_file.data_encapsulators;

import java.util.Arrays;
import java.util.Objects;

/**
 * Data object that stores the audio data.
 */
public class AudioDataObject extends AbstractDataObject {
    // Attributes
    public byte[] compressedMP3Bytes;
    public double sampleRate;
    public String audioFileName;

    /**
     * Initialization method for the audio data object.
     *
     * @param compressedMP3Bytes The LZ4 compressed bytes of the MP3 audio file.
     * @param sampleRate         Sample rate of the audio file
     * @param audioFileName      The name of the audio file.
     */
    public AudioDataObject(byte[] compressedMP3Bytes, double sampleRate, String audioFileName) {
        this.compressedMP3Bytes = compressedMP3Bytes;
        this.sampleRate = sampleRate;
        this.audioFileName = audioFileName;
    }

    // Overwritten methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioDataObject that = (AudioDataObject) o;
        return (
                Double.compare(that.sampleRate, sampleRate) == 0 &&
                        Arrays.equals(compressedMP3Bytes, that.compressedMP3Bytes) &&
                        audioFileName.equals(that.audioFileName)
        );
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(sampleRate, audioFileName);
        result = 31 * result + Arrays.hashCode(compressedMP3Bytes);
        return result;
    }
}
