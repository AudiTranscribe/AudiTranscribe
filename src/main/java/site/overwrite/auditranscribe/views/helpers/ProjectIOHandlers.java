/*
 * ProjectIOHandlers.java
 *
 * Created on 2022-05-04
 * Updated on 2022-06-08
 *
 * Description: Methods that handle the IO operations for an AudiTranscribe project.
 */

package site.overwrite.auditranscribe.views.helpers;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.*;
import org.apache.commons.compress.utils.FileNameUtils;
import org.javatuples.Pair;
import site.overwrite.auditranscribe.audio.ffmpeg.AudioConverter;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.AudioDataObject;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.GUIDataObject;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.ProjectDataObject;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.QTransformDataObject;
import site.overwrite.auditranscribe.exceptions.FailedToReadDataException;
import site.overwrite.auditranscribe.exceptions.IncorrectFileFormatException;
import site.overwrite.auditranscribe.io.audt_file.AUDTFileReader;
import site.overwrite.auditranscribe.io.audt_file.AUDTFileWriter;
import site.overwrite.auditranscribe.io.json_files.file_classes.SettingsFile;
import site.overwrite.auditranscribe.views.MainViewController;
import site.overwrite.auditranscribe.views.TranscriptionViewController;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// Main class

/**
 * Methods that handle the IO operations for an AudiTranscribe project.
 */
public class ProjectIOHandlers {
    // Attributes
    private static final Logger logger = Logger.getLogger(ProjectIOHandlers.class.getName());

    // Public methods

    /**
     * Method that handles the creation of a new AudiTranscribe project.
     *
     * @param mainStage          Main stage.
     * @param transcriptionStage Stage that contains the transcription scene.
     * @param file               File to open.
     * @param settingsFile       The <code>SettingsFile</code> object that handles the reading and
     *                           writing of settings.
     * @param allAudio           List of all opened <code>Audio</code> objects.
     * @param mainViewController Controller object of the main class.
     */
    public static void newProject(
            Stage mainStage, Stage transcriptionStage, File file, SettingsFile settingsFile,
            List<Audio> allAudio, MainViewController mainViewController
    ) {
        // Verify that the user choose a file
        if (file != null) {
            try {
                // Get the extension of the provided file
                String fileExt = "." + FileNameUtils.getExtension(file.getName()).toLowerCase();

                // Check if the file is supported
                if (!AudioConverter.EXTENSION_TO_CODEC.containsKey(fileExt)) {
                    throw new UnsupportedAudioFileException("The file extension is not supported.");
                }

                // Attempt creation of temporary folder if it doesn't exist
                IOMethods.createFolder(IOConstants.TEMP_FOLDER);
                logger.log(Level.FINE, "Temporary folder: " + IOConstants.TEMP_FOLDER);

                // Get the base path for the auxiliary files
                String baseName = IOConstants.TEMP_FOLDER + file.getName().replace(fileExt, "");

                // Check if the original file is a WAV file
                AudioConverter audioConverter = new AudioConverter(settingsFile.data.ffmpegInstallationPath);

                File auxiliaryWAVFile = new File(
                        audioConverter.convertAudio(file, baseName + "-auxiliary-wav.wav")
                );
                File auxiliaryMP3File = new File(
                        audioConverter.convertAudio(file, baseName + "-auxiliary-mp3.mp3")
                );

                // Try and read the auxiliary files as an `Audio` object
                // (Failure to read will throw exceptions)
                Audio audio = new Audio(auxiliaryWAVFile, auxiliaryMP3File, file.getName());

                // Delete auxiliary files
                boolean successfullyDeleted = auxiliaryWAVFile.delete();
                if (successfullyDeleted) {
                    logger.log(Level.FINE, "Successfully deleted auxiliary WAV file.");
                } else {
                    logger.log(Level.WARNING, "Failed to delete auxiliary WAV file.");
                }

                successfullyDeleted = auxiliaryMP3File.delete();
                if (successfullyDeleted) {
                    logger.log(Level.FINE, "Successfully deleted auxiliary MP3 file.");
                } else {
                    logger.log(Level.WARNING, "Failed to delete auxiliary MP3 file.");
                }

                // Get the current scene and the spectrogram view controller
                Pair<Scene, TranscriptionViewController> stageSceneAndController = getController(transcriptionStage);
                Scene scene = stageSceneAndController.getValue0();
                TranscriptionViewController controller = stageSceneAndController.getValue1();

                // Update the `settingsFile` attribute
                controller.setSettingsFile(settingsFile);

                // Set the theme of the scene
                controller.setThemeOnScene();

                // Set the project data for the existing project
                controller.setAudioAndSpectrogramData(audio);
                controller.finishSetup(mainStage, allAudio, mainViewController);

                // Set the scene for the transcription page
                transcriptionStage.setScene(scene);

                // Set new scene properties
                transcriptionStage.setMaximized(true);
                transcriptionStage.setResizable(true);
                transcriptionStage.setTitle(file.getName());

                // Set width and height of the new scene
                Rectangle2D screenBounds = Screen.getPrimary().getBounds();
                transcriptionStage.setMinWidth(screenBounds.getWidth());
                transcriptionStage.setMinHeight(screenBounds.getHeight());

                // Hide main stage (if necessary) and show transcription stage (if necessary)
                if (mainStage.isShowing()) mainStage.hide();
                if (!transcriptionStage.isShowing()) {
                    transcriptionStage.showAndWait();
                    controller.handleSceneClosing();
                    mainViewController.refreshProjectsListView();
                    mainViewController.stopAllAudioObjects();
                    mainStage.show();  // Show the main scene upon the spectrogram scene's closure
                }

            } catch (UnsupportedAudioFileException | IOException e) {
                Popups.showExceptionAlert(
                        "Failed to read '" + file.getName() + "' as an audio file.",
                        "The program failed to read '" + file.getName() +
                                "' as an audio file. Please check if " + "this is a valid audio file.",
                        e
                );
                e.printStackTrace();
            }

        } else {
            Popups.showInformationAlert("Info", "No file selected.");
        }
    }

    /**
     * Method that handles the opening of an existing AudiTranscribe project.
     *
     * @param mainStage          Main stage.
     * @param transcriptionStage Stage that contains the transcription scene.
     * @param file               File to open.
     * @param settingsFile       The <code>SettingsFile</code> object that handles the reading and
     *                           writing of settings.
     * @param allAudio           List of all opened <code>Audio</code> objects.
     * @param mainViewController Controller object of the main class.
     */
    public static void openProject(
            Stage mainStage, Stage transcriptionStage, File file, SettingsFile settingsFile,
            List<Audio> allAudio, MainViewController mainViewController
    ) {
        // Verify that the user choose a file
        if (file != null) {
            try {
                // Try and read the file as an AUDT file
                String audtFilePath = file.getAbsolutePath();
                String audtFileName = file.getName();
                AUDTFileReader reader = new AUDTFileReader(audtFilePath);

                // Read the data from the file
                QTransformDataObject qTransformData = reader.readQTransformData();
                AudioDataObject audioData = reader.readAudioData();
                GUIDataObject guiData = reader.readGUIData();

                // Pass these data into a `ProjectDataObject`
                ProjectDataObject projectDataObject = new ProjectDataObject(qTransformData, audioData, guiData);

                // Get the current scene and the spectrogram view controller
                Pair<Scene, TranscriptionViewController> stageSceneAndController = getController(transcriptionStage);
                Scene scene = stageSceneAndController.getValue0();
                TranscriptionViewController controller = stageSceneAndController.getValue1();

                // Update the `settingsFile` attribute
                controller.setSettingsFile(settingsFile);

                // Set the theme of the scene
                controller.setThemeOnScene();

                // Set the project data for the existing project
                controller.useExistingData(audtFilePath, audtFileName, projectDataObject);
                controller.finishSetup(mainStage, allAudio, mainViewController);

                // Set the scene for the transcription page
                transcriptionStage.setScene(scene);

                // Set new scene properties
                transcriptionStage.setMaximized(true);
                transcriptionStage.setResizable(true);
                transcriptionStage.setTitle(audioData.audioFileName);

                // Set width and height of the new scene
                Rectangle2D screenBounds = Screen.getPrimary().getBounds();
                transcriptionStage.setMinWidth(screenBounds.getWidth());
                transcriptionStage.setMinHeight(screenBounds.getHeight());

                // Update scroll position
                controller.updateScrollPosition(
                        projectDataObject.guiData.currTimeInMS / 1000. *
                                controller.PX_PER_SECOND *
                                controller.SPECTROGRAM_ZOOM_SCALE_X,
                        screenBounds.getWidth()
                );

                // Hide main stage (if necessary) and show transcription stage (if necessary)
                if (mainStage.isShowing()) mainStage.hide();
                if (!transcriptionStage.isShowing()) {
                    transcriptionStage.showAndWait();
                    controller.handleSceneClosing();
                    mainViewController.refreshProjectsListView();
                    mainViewController.stopAllAudioObjects();
                    mainStage.show();  // Show the main scene upon the spectrogram scene's closure
                }

            } catch (IOException | IncorrectFileFormatException | FailedToReadDataException e) {
                Popups.showExceptionAlert(
                        "Failed to read '" + file.getName() + "' as an AUDT ile.",
                        "The program failed to read '" + file.getName() +
                                "' as an AUDT file. Please check if " + "this is a valid AUDT file.",
                        e
                );
                e.printStackTrace();
            }
        } else {
            Popups.showInformationAlert("Info", "No file selected.");
        }
    }

    /**
     * Method that handles the saving of an AudiTranscribe project.
     *
     * @param filepath          <b>Absolute</b> path to the AUDT file.
     * @param projectDataObject Data object that stores all the data for the project.
     * @throws IOException If the writing to file encounters an error.
     */
    public static void saveProject(
            String filepath, ProjectDataObject projectDataObject
    ) throws IOException {
        // Declare the file writer object
        AUDTFileWriter fileWriter = new AUDTFileWriter(filepath);

        // Write data to the file
        fileWriter.writeQTransformData(projectDataObject.qTransformData);
        fileWriter.writeAudioData(projectDataObject.audioData);
        fileWriter.writeGUIData(projectDataObject.guiData);

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

    // Private methods

    /**
     * Helper method that gets the spectrogram scene and spectrogram view controller.
     *
     * @param transcriptionStage Stage that contains the transcription scene.
     * @return Spectrogram scene object and the <code>TranscriptionViewController</code> object.
     * @throws IOException If the spectrogram view FXML file cannot be found.
     */
    private static Pair<Scene, TranscriptionViewController> getController(
            Stage transcriptionStage
    ) throws IOException {
        // Unset full screen and maximized first
        transcriptionStage.setMaximized(false);
        transcriptionStage.setFullScreen(false);

        // Get the FXML loader for the spectrogram view
        FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL("views/fxml/transcription-view.fxml"));

        // Get the spectrogram view scene
        Scene scene = new Scene(fxmlLoader.load());

        // Get the spectrogram view controller
        TranscriptionViewController controller = fxmlLoader.getController();

        // Return the scene and controller
        return new Pair<>(scene, controller);
    }
}
