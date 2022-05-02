/*
 * GUIDataObject.java
 *
 * Created on 2022-05-02
 * Updated on 2022-05-02
 *
 * Description: Data object that stores the GUI data.
 */

package site.overwrite.auditranscribe.io.data_encapsulators;

import java.util.Objects;

/**
 * Data object that stores the GUI data.
 */
public class GUIDataObject extends AbstractDataObject {
    // Attributes
    public int musicKeyIndex;
    public int timeSignatureIndex;
    public double bpm;
    public double offsetSeconds;
    public double playbackVolume;
    public String audioFileName;
    public int totalDurationInMS;
    public int currTimeInMS;

    /**
     * Initialization method for the GUI data object.
     *
     * @param musicKeyIndex      The index of the music key in the dropdown menu shown in the
     *                           application.
     * @param timeSignatureIndex The index of the time signature in the dropdown menu shown in the application.
     * @param bpm                Number of beats per minute.
     * @param offsetSeconds      Number of seconds offset from the start of the audio.
     * @param playbackVolume     Volume to play back at.
     * @param audioFileName      Name of the audio file.
     * @param totalDurationInMS  Total duration of the audio in <b>milliseconds</b>.
     * @param currTimeInMS       Current playback time of the audio in <b>milliseconds</b>.
     */
    public GUIDataObject(
            int musicKeyIndex, int timeSignatureIndex, double bpm, double offsetSeconds, double playbackVolume,
            String audioFileName, int totalDurationInMS, int currTimeInMS
    ) {
        this.musicKeyIndex = musicKeyIndex;
        this.timeSignatureIndex = timeSignatureIndex;
        this.bpm = bpm;
        this.offsetSeconds = offsetSeconds;
        this.playbackVolume = playbackVolume;
        this.audioFileName = audioFileName;
        this.totalDurationInMS = totalDurationInMS;
        this.currTimeInMS = currTimeInMS;
    }

    // Overwritten methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GUIDataObject that = (GUIDataObject) o;
        return (musicKeyIndex == that.musicKeyIndex &&
                timeSignatureIndex == that.timeSignatureIndex &&
                Double.compare(that.bpm, bpm) == 0 &&
                Double.compare(that.offsetSeconds, offsetSeconds) == 0 &&
                Double.compare(that.playbackVolume, playbackVolume) == 0 &&
                Objects.equals(audioFileName, that.audioFileName) &&
                totalDurationInMS == that.totalDurationInMS &&
                currTimeInMS == that.currTimeInMS
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                musicKeyIndex, timeSignatureIndex, bpm, offsetSeconds, playbackVolume, audioFileName, totalDurationInMS,
                currTimeInMS
        );
    }
}
