/*
 * ProjectInfoDataObject.java
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

package app.auditranscribe.io.audt_file.base.data_encapsulators;

import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import app.auditranscribe.music.MusicKey;
import app.auditranscribe.music.TimeSignature;

import java.util.Objects;

/**
 * Data object that stores the project's info.<br>
 * <b>Note</b>: This is different from the <code>ProjectData</code> class. This is a data object
 * storing the project's info, whereas the <code>ProjectData</code> object stores all the different
 * data encapsulators.
 */
public abstract class ProjectInfoDataObject extends AbstractAUDTDataObject {
    // Constants
    public static final int SECTION_ID = 4;

    // Attributes
    public String projectName;
    public MusicKey musicKey;
    public TimeSignature timeSignature;
    public double bpm;
    public double offsetSeconds;
    public double playbackVolume;
    public int currTimeInMS;

    // Public methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectInfoDataObject that = (ProjectInfoDataObject) o;
        return Objects.equals(projectName, that.projectName) &&
                musicKey == that.musicKey &&
                timeSignature == that.timeSignature &&
                Double.compare(that.bpm, bpm) == 0 &&
                Double.compare(that.offsetSeconds, offsetSeconds) == 0 &&
                Double.compare(that.playbackVolume, playbackVolume) == 0 &&
                currTimeInMS == that.currTimeInMS;
    }

    @Override
    @ExcludeFromGeneratedCoverageReport
    public int hashCode() {
        return Objects.hash(
                projectName, musicKey.name, timeSignature.displayText(), bpm, offsetSeconds, playbackVolume,
                currTimeInMS
        );
    }
}
