/*
 * ProjectData.java
 *
 * Created on 2022-05-07
 * Updated on 2022-06-23
 *
 * Description: Class that stores all the project's data.
 */

package site.overwrite.auditranscribe.io.audt_file;

import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.*;

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
}
