/*
 * ProjectInfoDataObject0x00090002.java
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

package app.auditranscribe.io.audt_file.v0x000B0001.data_encapsulators;

import app.auditranscribe.io.audt_file.base.data_encapsulators.ProjectInfoDataObject;
import app.auditranscribe.music.MusicKey;
import app.auditranscribe.music.TimeSignature;

/**
 * Data object that stores the project's info.<br>
 * <b>Note</b>: This is different from the <code>ProjectData</code> class. This is a data object
 * storing the project's info, whereas the <code>ProjectData</code> object stores all the different
 * data encapsulators.
 */
public class ProjectInfoDataObject0x000B0001 extends ProjectInfoDataObject {
    /**
     * Initialization method for the project info data object.
     *
     * @param projectName    Name of the project.
     * @param musicKey       Music key of the project.
     * @param timeSignature  Time signature of the project.
     * @param bpm            Number of beats per minute.
     * @param offsetSeconds  Number of seconds offset from the start of the audio.
     * @param playbackVolume Volume to play back at.
     * @param currTimeInMS   Current playback time of the audio in <b>milliseconds</b>.
     */
    public ProjectInfoDataObject0x000B0001(
            String projectName, MusicKey musicKey, TimeSignature timeSignature, double bpm, double offsetSeconds,
            double playbackVolume, int currTimeInMS
    ) {
        this.projectName = projectName;
        this.musicKey = musicKey;
        this.timeSignature = timeSignature;
        this.bpm = bpm;
        this.offsetSeconds = offsetSeconds;
        this.playbackVolume = playbackVolume;
        this.currTimeInMS = currTimeInMS;
    }

    // Public methods
    @Override
    public int numBytesNeeded() {
        return 4 +   // Section ID
                (4 + projectName.getBytes().length) +  // String length + string bytes of project name
                2 +  // Music key UUID
                2 + 2 +  // Time signature (numerator + denominator = 2 + 2)
                8 +  // BPM
                8 +  // Offset seconds
                8 +  // Playback volume
                4 +  // Current time in milliseconds
                4;   // EOS delimiter
    }
}
