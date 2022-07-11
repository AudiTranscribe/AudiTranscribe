/*
 * GUIDataObject401.java
 *
 * Created on 2022-05-02
 * Updated on 2022-07-11
 *
 * Description: Data object that stores the GUI data.
 */

package site.overwrite.auditranscribe.io.audt_file.data_encapsulators;

import java.util.Objects;

/**
 * Data object that stores the GUI data.
 */
public abstract class GUIDataObject extends AbstractAUDTDataObject {
    // Constants
    public static final int SECTION_ID = 4;

    // Attributes
    public int musicKeyIndex;
    public int timeSignatureIndex;
    public double bpm;
    public double offsetSeconds;
    public double playbackVolume;
    public int currTimeInMS;

    // Overwritten methods
    @Override
    public int numBytesNeeded() {
        return 4 +   // Section ID
                4 +  // Music key index
                4 +  // Time signature index
                8 +  // BPM
                8 +  // Offset seconds
                8 +  // Playback volume
                4 +  // Current time in milliseconds
                4;   // EOS delimiter
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GUIDataObject that = (GUIDataObject) o;
        return (
                musicKeyIndex == that.musicKeyIndex &&
                        timeSignatureIndex == that.timeSignatureIndex &&
                        Double.compare(that.bpm, bpm) == 0 &&
                        Double.compare(that.offsetSeconds, offsetSeconds) == 0 &&
                        Double.compare(that.playbackVolume, playbackVolume) == 0 &&
                        currTimeInMS == that.currTimeInMS
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                musicKeyIndex, timeSignatureIndex, bpm, offsetSeconds, playbackVolume, currTimeInMS
        );
    }
}
