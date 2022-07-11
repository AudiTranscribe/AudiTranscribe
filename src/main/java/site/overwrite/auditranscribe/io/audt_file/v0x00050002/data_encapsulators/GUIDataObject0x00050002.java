/*
 * GUIDataObject0x00050002.java
 *
 * Created on 2022-07-11
 * Updated on 2022-07-11
 *
 * Description: Data object that stores the GUI data.
 */

package site.overwrite.auditranscribe.io.audt_file.v0x00050002.data_encapsulators;

import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.GUIDataObject;

/**
 * Data object that stores the GUI data.
 */
public class GUIDataObject0x00050002 extends GUIDataObject {
    /**
     * Initialization method for the GUI data object.
     *
     * @param musicKeyIndex      The index of the music key in the dropdown menu shown in the
     *                           application.
     * @param timeSignatureIndex The index of the time signature in the dropdown menu shown in the application.
     * @param bpm                Number of beats per minute.
     * @param offsetSeconds      Number of seconds offset from the start of the audio.
     * @param playbackVolume     Volume to play back at.
     * @param currTimeInMS       Current playback time of the audio in <b>milliseconds</b>.
     */
    public GUIDataObject0x00050002(
            int musicKeyIndex, int timeSignatureIndex, double bpm, double offsetSeconds, double playbackVolume,
            int currTimeInMS
    ) {
        this.musicKeyIndex = musicKeyIndex;
        this.timeSignatureIndex = timeSignatureIndex;
        this.bpm = bpm;
        this.offsetSeconds = offsetSeconds;
        this.playbackVolume = playbackVolume;
        this.currTimeInMS = currTimeInMS;
    }
}
