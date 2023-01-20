/*
 * ProjectInfoDataObject0x00080001.java
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

package app.auditranscribe.io.audt_file.v0x00080001.data_encapsulators;

import app.auditranscribe.io.audt_file.v0x00070001.data_encapsulators.ProjectInfoDataObject0x00070001;

/**
 * Data object that stores the project's info.<br>
 * <b>Note</b>: This is different from the <code>ProjectData</code> class. This is a data object
 * storing the project's info, whereas the <code>ProjectData</code> object stores all the different
 * data encapsulators.
 */
public class ProjectInfoDataObject0x00080001 extends ProjectInfoDataObject0x00070001 {
    /**
     * Initialization method for the project info data object.
     *
     * @param projectName        Name of the project.
     * @param musicKeyIndex      The index of the music key in the dropdown menu shown in the
     *                           application.
     * @param timeSignatureIndex The index of the time signature in the dropdown menu shown in the application.
     * @param bpm                Number of beats per minute.
     * @param offsetSeconds      Number of seconds offset from the start of the audio.
     * @param playbackVolume     Volume to play back at.
     * @param currTimeInMS       Current playback time of the audio in <b>milliseconds</b>.
     */
    public ProjectInfoDataObject0x00080001(
            String projectName, int musicKeyIndex, int timeSignatureIndex, double bpm, double offsetSeconds,
            double playbackVolume, int currTimeInMS
    ) {
        super(projectName, musicKeyIndex, timeSignatureIndex, bpm, offsetSeconds, playbackVolume, currTimeInMS);
    }
}
