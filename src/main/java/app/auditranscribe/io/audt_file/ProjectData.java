/*
 * ProjectData.java
 * Description: Stores all of a project's data.
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

package app.auditranscribe.io.audt_file;

import app.auditranscribe.io.audt_file.base.data_encapsulators.*;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;

import java.util.Objects;

/**
 * Stores all of a project's data.
 */
public class ProjectData {
    // Attributes
    public UnchangingDataPropertiesObject unchangingDataProperties;
    public QTransformDataObject qTransformData;
    public AudioDataObject audioData;
    public ProjectInfoDataObject projectInfoData;
    public MusicNotesDataObject musicNotesData;

    /**
     * Initialization method for the project data object.
     *
     * @param unchangingData  A <code>UnchangingDataPropertiesObject</code> that stores all the
     *                        properties of the unchanging data.
     * @param qTransformData  A <code>QTransformDataObject</code> that stores all the Q-Transform
     *                        data.
     * @param audioData       A <code>AudioDataObject</code> that stores all the audio data.
     * @param projectInfoData A <code>ProjectInfoDataObject</code> that stores all the project info
     *                        data.
     * @param musicNotesData  A <code>MusicNotesDataObject</code> that stores all the music notes'
     *                        data.
     */
    public ProjectData(
            UnchangingDataPropertiesObject unchangingData, QTransformDataObject qTransformData,
            AudioDataObject audioData, ProjectInfoDataObject projectInfoData, MusicNotesDataObject musicNotesData
    ) {
        this.unchangingDataProperties = unchangingData;
        this.qTransformData = qTransformData;
        this.audioData = audioData;
        this.projectInfoData = projectInfoData;
        this.musicNotesData = musicNotesData;
    }

    // Public methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectData that = (ProjectData) o;
        return Objects.equals(unchangingDataProperties, that.unchangingDataProperties) &&
                Objects.equals(qTransformData, that.qTransformData) &&
                Objects.equals(audioData, that.audioData) &&
                Objects.equals(projectInfoData, that.projectInfoData) &&
                Objects.equals(musicNotesData, that.musicNotesData);
    }

    @Override
    @ExcludeFromGeneratedCoverageReport
    public int hashCode() {
        return Objects.hash(unchangingDataProperties, qTransformData, audioData, projectInfoData, musicNotesData);
    }
}
