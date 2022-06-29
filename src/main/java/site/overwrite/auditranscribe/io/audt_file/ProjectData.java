/*
 * ProjectData.java
 *
 * Created on 2022-05-07
 * Updated on 2022-06-29
 *
 * Description: Class that stores all the project's data.
 */

package site.overwrite.auditranscribe.io.audt_file;

import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.*;

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
