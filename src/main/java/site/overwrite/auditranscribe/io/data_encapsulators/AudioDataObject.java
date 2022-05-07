/*
 * AudioDataObject.java
 *
 * Created on 2022-05-06
 * Updated on 2022-05-07
 *
 * Description: Data object that stores the audio data.
 */

package site.overwrite.auditranscribe.io.data_encapsulators;

import java.util.Objects;

/**
 * Data object that stores the audio data.
 */
public class AudioDataObject extends AbstractDataObject {
    // Attributes
    public String audioFilePath;
    public double sampleRate;

    /**
     * Initialization method for the audio data object.
     *
     * @param audioFilePath <b>Absolute</b> file path to the original audio file.
     * @param sampleRate    Sample rate of the audio file.
     */
    public AudioDataObject(String audioFilePath, double sampleRate) {
        this.audioFilePath = audioFilePath;
        this.sampleRate = sampleRate;
    }

    // Overwritten methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioDataObject that = (AudioDataObject) o;
        return audioFilePath.equals(that.audioFilePath) && sampleRate == that.sampleRate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(audioFilePath, sampleRate);
    }
}
