/*
 * ProjectInfoDataObject0x00050002.java
 * Description: Data object that stores the project's info.
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

import app.auditranscribe.io.audt_file.AUDTFileHelpers;
import app.auditranscribe.io.audt_file.base.data_encapsulators.ProjectInfoDataObject;

/**
 * Data object that stores the project's info.<br>
 * <b>Note</b>: This is different from the <code>ProjectData</code> class. This is a data object
 * storing the project's info, whereas the <code>ProjectData</code> object stores all the different
 * data encapsulators.
 */
public class ProjectInfoDataObject0x00050002 extends ProjectInfoDataObject {
    // Attributes
    public int timeSignatureIndex;
    public int musicKeyIndex;

    /**
     * Initialization method for the project info data object.
     *
     * @param musicKeyIndex      The index of the music key in the dropdown menu shown in the
     *                           application.
     * @param timeSignatureIndex The index of the time signature in the dropdown menu shown in the application.
     * @param bpm                Number of beats per minute.
     * @param offsetSeconds      Number of seconds offset from the start of the audio.
     * @param playbackVolume     Volume to play back at.
     * @param currTimeInMS       Current playback time of the audio in <b>milliseconds</b>.
     */
    public ProjectInfoDataObject0x00050002(
            int musicKeyIndex, int timeSignatureIndex, double bpm, double offsetSeconds, double playbackVolume,
            int currTimeInMS
    ) {
        this.musicKeyIndex = musicKeyIndex;
        this.timeSignatureIndex = timeSignatureIndex;
        this.bpm = bpm;
        this.offsetSeconds = offsetSeconds;
        this.playbackVolume = playbackVolume;
        this.currTimeInMS = currTimeInMS;

        this.projectName = null;  // We will need to update this later
        this.musicKey = AUDTFileHelpers.oldMusicKeyIndexToMusicKey(musicKeyIndex);
        this.timeSignature = AUDTFileHelpers.oldTimeSignatureIndexToTimeSignature(timeSignatureIndex);
    }

    // Getter/setter methods

    /**
     * Sets the project name.<br>
     * This helps bridge the gap between the old system (where we used <code>audioFileName</code> as
     * the project name) and the new system.
     *
     * @param projectName The project name to set.
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;  // We are setting the superclass' attribute
    }

    // Public methods
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
}
