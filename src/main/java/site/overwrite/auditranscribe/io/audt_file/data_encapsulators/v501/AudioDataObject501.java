/*
 * AudioDataObject501.java
 *
 * Created on 2022-07-11
 * Updated on 2022-07-11
 *
 * Description: Data object that stores the audio data.
 */

package site.overwrite.auditranscribe.io.audt_file.data_encapsulators.v501;

import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.AudioDataObject;

/**
 * Data object that stores the audio data.
 */
public class AudioDataObject501 extends AudioDataObject {
    /**
     * Initialization method for the audio data object.
     *
     * @param compressedMP3Bytes The LZ4 compressed bytes of the MP3 audio file.
     * @param sampleRate         Sample rate of the audio file
     * @param totalDurationInMS  Total duration of the audio in <b>milliseconds</b>.
     * @param audioFileName      The name of the audio file.
     */
    public AudioDataObject501(byte[] compressedMP3Bytes, double sampleRate, int totalDurationInMS, String audioFileName) {
        this.compressedMP3Bytes = compressedMP3Bytes;
        this.sampleRate = sampleRate;
        this.totalDurationInMS = totalDurationInMS;
        this.audioFileName = audioFileName;
    }
}
