/*
 * ProjectData.java
 * Description: Class that stores all the project's data.
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

package site.overwrite.auditranscribe.io.audt_file;

import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.*;

import java.util.Objects;

/**
 * Class that stores all the project's data.
 */
public class ProjectData {
    // Attributes
    public UnchangingDataPropertiesObject unchangingDataProperties;
    public QTransformDataObject qTransformData;
    public AudioDataObject audioData;
    public GUIDataObject guiData;
    public MusicNotesDataObject musicNotesData;

    /**
     * Initialization method for the project data object.
     *
     * @param unchangingDataProperties A <code>UnchangingDataPropertiesObject</code> that stores all
     *                                 the properties of the unchanging data.
     * @param qTransformData           A <code>QTransformDataObject</code> that stores all the
     *                                 Q-Transform data.
     * @param audioData                A <code>AudioDataObject</code> that stores all the audio
     *                                 data.
     * @param guiData                  A <code>GUIDataObject</code> that stores all the GUI data.
     * @param musicNotesData           A <code>MusicNotesDataObject</code> that stores all the music
     *                                 notes' data.
     */
    public ProjectData(
            UnchangingDataPropertiesObject unchangingDataProperties, QTransformDataObject qTransformData,
            AudioDataObject audioData, GUIDataObject guiData, MusicNotesDataObject musicNotesData
    ) {
        this.unchangingDataProperties = unchangingDataProperties;
        this.qTransformData = qTransformData;
        this.audioData = audioData;
        this.guiData = guiData;
        this.musicNotesData = musicNotesData;
    }

    // Overwritten methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;
        ProjectData that = (ProjectData) o;
        return (
                unchangingDataProperties.equals(that.unchangingDataProperties) &&
                        qTransformData.equals(that.qTransformData) &&
                        audioData.equals(that.audioData) &&
                        guiData.equals(that.guiData) &&
                        musicNotesData.equals(that.musicNotesData)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(unchangingDataProperties, qTransformData, audioData, guiData, musicNotesData);
    }
}
