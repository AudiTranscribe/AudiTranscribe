/*
 * ProjectDataObject.java
 *
 * Created on 2022-05-07
 * Updated on 2022-05-10
 *
 * Description: Data object that stores all the project's data.
 */

package site.overwrite.auditranscribe.io.audt_file.data_encapsulators;

import java.util.Objects;

/**
 * Data object that stores all the project's data.
 */
public class ProjectDataObject extends AbstractDataObject {
    // Attributes
    public QTransformDataObject qTransformData;
    public AudioDataObject audioData;
    public GUIDataObject guiData;

    /**
     * Initialization method for the project data object.
     *
     * @param qTransformData A <code>QTransformDataObject</code> that stores all the Q-Transform data.
     * @param audioData      A <code>AudioDataObject</code> that stores all the audio data.
     * @param guiData        A <code>GUIDataObject</code> that stores all the GUI data.
     */
    public ProjectDataObject(QTransformDataObject qTransformData, AudioDataObject audioData, GUIDataObject guiData) {
        this.qTransformData = qTransformData;
        this.audioData = audioData;
        this.guiData = guiData;
    }

    // Overwritten methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectDataObject that = (ProjectDataObject) o;
        return (
                qTransformData.equals(that.qTransformData) &&
                        audioData.equals(that.audioData) &&
                        guiData.equals(that.guiData)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(qTransformData, audioData, guiData);
    }
}
