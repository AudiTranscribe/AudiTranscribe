/*
 * ProjectIOHandlers.java
 *
 * Created on 2022-05-04
 * Updated on 2022-07-11
 *
 * Description: Methods that handle the IO operations for an AudiTranscribe project.
 */

package site.overwrite.auditranscribe.main_views.helpers;

import javafx.stage.*;
import site.overwrite.auditranscribe.exceptions.io.audt_file.InvalidFileVersionException;
import site.overwrite.auditranscribe.io.audt_file.AUDTFileConstants;
import site.overwrite.auditranscribe.io.audt_file.base.AUDTFileWriter;
import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.GUIDataObject;
import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.MusicNotesDataObject;
import site.overwrite.auditranscribe.io.audt_file.ProjectData;

import java.io.File;
import java.io.IOException;

// Main class

/**
 * Methods that handle the IO operations for an AudiTranscribe project.
 */
public final class ProjectIOHandlers {
    private ProjectIOHandlers() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that handles the saving of an AudiTranscribe project.
     *
     * @param filepath          <b>Absolute</b> path to the AUDT file.
     * @param numSkippableBytes Number of bytes to skip before writing the data.
     * @param guiData           <code>GUIDataObject</code> object that contains the GUI data.
     * @param musicNotesData    <code>MusicNotesDataObject</code> object that contains the music data.
     * @throws IOException If the writing to file encounters an error.
     */
    public static void saveProject(
            String filepath, int numSkippableBytes, GUIDataObject guiData, MusicNotesDataObject musicNotesData
    ) throws IOException {
        try {
            // Declare the file writer object
            AUDTFileWriter fileWriter = AUDTFileWriter.getWriter(
                    AUDTFileConstants.FILE_VERSION_NUMBER, filepath, numSkippableBytes
            );

            // Write data to the file
            fileWriter.writeGUIData(guiData);
            fileWriter.writeMusicNotesData(musicNotesData);

            fileWriter.writeBytesToFile();
        } catch (InvalidFileVersionException ignored) {  // Impossible for the version to be wrong
        }
    }

    /**
     * Method that handles the saving of an AudiTranscribe project.
     *
     * @param filepath    <b>Absolute</b> path to the AUDT file.
     * @param projectData Data object that stores all the data for the project.
     * @throws IOException If the writing to file encounters an error.
     */
    public static void saveProject(String filepath, ProjectData projectData) throws IOException {
        try {
            // Declare the file writer object
            AUDTFileWriter fileWriter = AUDTFileWriter.getWriter(AUDTFileConstants.FILE_VERSION_NUMBER, filepath);

            // Write data to the file
            fileWriter.writeUnchangingDataProperties(projectData.unchangingDataProperties);
            fileWriter.writeQTransformData(projectData.qTransformData);
            fileWriter.writeAudioData(projectData.audioData);
            fileWriter.writeGUIData(projectData.guiData);
            fileWriter.writeMusicNotesData(projectData.musicNotesData);

            fileWriter.writeBytesToFile();
        } catch (InvalidFileVersionException ignored) {  // Impossible for the version to be wrong
        }
    }

    /**
     * Method that helps show a file dialog for the user to select a file on.
     *
     * @param window  WindowFunction to show the file dialog on.
     * @param filters Array of file filters to show in the file dialog.
     * @return A <code>File</code> object, representing the selected file.
     */
    public static File getFileFromFileDialog(Window window, FileChooser.ExtensionFilter... filters) {
        FileChooser fileChooser = new FileChooser();

        for (FileChooser.ExtensionFilter filter : filters) {
            fileChooser.getExtensionFilters().add(filter);
        }

        return fileChooser.showOpenDialog(window);
    }
}
