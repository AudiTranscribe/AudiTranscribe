/*
 * ProjectIOHandlers.java
 *
 * Created on 2022-05-04
 * Updated on 2022-06-22
 *
 * Description: Methods that handle the IO operations for an AudiTranscribe project.
 */

package site.overwrite.auditranscribe.views.helpers;

import javafx.stage.*;
import site.overwrite.auditranscribe.io.audt_file.ProjectData;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.*;
import site.overwrite.auditranscribe.io.audt_file.AUDTFileWriter;

import java.io.File;
import java.io.IOException;

// Main class

/**
 * Methods that handle the IO operations for an AudiTranscribe project.
 */
public class ProjectIOHandlers {
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
        // Declare the file writer object
        AUDTFileWriter fileWriter = new AUDTFileWriter(filepath, numSkippableBytes);

        // Write data to the file
        fileWriter.writeGUIData(guiData);
        fileWriter.writeMusicNotesData(musicNotesData);

        fileWriter.writeBytesToFile();
    }

    /**
     * Method that handles the saving of an AudiTranscribe project.
     *
     * @param filepath    <b>Absolute</b> path to the AUDT file.
     * @param projectData Data object that stores all the data for the project.
     * @throws IOException If the writing to file encounters an error.
     */
    public static void saveProject(String filepath, ProjectData projectData) throws IOException {
        // Declare the file writer object
        AUDTFileWriter fileWriter = new AUDTFileWriter(filepath);

        // Write data to the file
        fileWriter.writeUnchangingDataProperties(projectData.unchangingDataProperties);
        fileWriter.writeQTransformData(projectData.qTransformData);
        fileWriter.writeAudioData(projectData.audioData);
        fileWriter.writeGUIData(projectData.guiData);
        fileWriter.writeMusicNotesData(projectData.musicNotesData);

        fileWriter.writeBytesToFile();
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
