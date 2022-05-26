/*
 * ProjectIOHandlers.java
 *
 * Created on 2022-05-04
 * Updated on 2022-05-26
 *
 * Description: Methods that handle the IO operations for an AudiTranscribe project.
 */

package site.overwrite.auditranscribe.views.helpers;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.*;
import org.javatuples.Pair;
import site.overwrite.auditranscribe.CustomTask;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.AudioDataObject;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.GUIDataObject;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.ProjectDataObject;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.QTransformDataObject;
import site.overwrite.auditranscribe.io.audt_file.exceptions.FailedToReadDataException;
import site.overwrite.auditranscribe.io.audt_file.exceptions.IncorrectFileFormatException;
import site.overwrite.auditranscribe.io.audt_file.file_handers.AUDTFileReader;
import site.overwrite.auditranscribe.io.audt_file.file_handers.AUDTFileWriter;
import site.overwrite.auditranscribe.views.MainViewController;
import site.overwrite.auditranscribe.views.TranscriptionViewController;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

// Main class

/**
 * Methods that handle the IO operations for an AudiTranscribe project.
 */
public class ProjectIOHandlers {
    // Public methods

    /**
     * Method that handles the creation of a new AudiTranscribe project.
     *
     * @param mainStage          Main stage.
     * @param transcriptionStage Stage that contains the transcription scene.
     * @param file               File to open.
     * @param mainViewController Controller object of the main class.
     */
    public static void newProject(
            Stage mainStage, Stage transcriptionStage, File file, MainViewController mainViewController
    ) {
        // Verify that the user choose a file
        if (file != null) {
            try {
                // Try and read the file as an audio file
                Audio audio = new Audio(file);  // Failure to read will throw an exception

                // Get the current scene and the spectrogram view controller
                Pair<Scene, TranscriptionViewController> stageSceneAndController = getController(transcriptionStage);
                Scene scene = stageSceneAndController.getValue0();
                TranscriptionViewController controller = stageSceneAndController.getValue1();

                // Set the project data for the existing project
                controller.setAudioAndSpectrogramData(audio);
                controller.finishSetup(mainStage, mainViewController);

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
                    mainStage.show();  // Show the main scene upon the spectrogram scene's closure
                }

            } catch (UnsupportedAudioFileException | IOException e) {
                AlertMessages.showExceptionAlert(
                        "Failed to read '" + file.getName() + "' as a WAV file.",
                        "The program failed to read '" + file.getName() +
                                "' as a WAV file. Please check if " + "this is a valid WAV file.",
                        e
                );
                e.printStackTrace();
            }

        } else {
            AlertMessages.showInformationAlert("Info", "No file selected.");
        }
    }

    /**
     * Method that handles the opening of an existing AudiTranscribe project.
     *
     * @param mainStage          Main stage.
     * @param transcriptionStage Stage that contains the transcription scene.
     * @param file               File to open.
     * @param mainViewController Controller object of the main class.
     */
    public static void openProject(
            Stage mainStage, Stage transcriptionStage, File file, MainViewController mainViewController
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

                // Set the project data for the existing project
                controller.useExistingData(audtFilePath, audtFileName, projectDataObject);
                controller.finishSetup(mainStage, mainViewController);

                // Set the scene for the transcription page
                transcriptionStage.setScene(scene);

                // Set new scene properties
                transcriptionStage.setMaximized(true);
                transcriptionStage.setResizable(true);
                transcriptionStage.setTitle(guiData.audioFileName);

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
                    mainStage.show();  // Show the main scene upon the spectrogram scene's closure
                }

            } catch (IOException | IncorrectFileFormatException | FailedToReadDataException e) {
                AlertMessages.showExceptionAlert(
                        "Failed to read '" + file.getName() + "' as an AUDT ile.",
                        "The program failed to read '" + file.getName() +
                                "' as an AUDT file. Please check if " + "this is a valid AUDT file.",
                        e
                );
                e.printStackTrace();
            }
        } else {
            AlertMessages.showInformationAlert("Info", "No file selected.");
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
            String filepath, ProjectDataObject projectDataObject, CustomTask<?> task
    ) throws IOException {
        // Declare the file writer object
        AUDTFileWriter fileWriter = new AUDTFileWriter(filepath, task);

        // Write data to the file
        fileWriter.writeQTransformData(projectDataObject.qTransformData);
        fileWriter.writeAudioData(projectDataObject.audioData);
        fileWriter.writeGUIData(projectDataObject.guiData);

        fileWriter.writeBytesToFile();
    }

    /**
     * Method that helps show a file dialog for the user to select a file on.
     *
     * @param window WindowFunction to show the file dialog on.
     * @return A <code>File</code> object, representing the selected file.
     */
    public static File getFileFromFileDialog(Window window) {
        FileChooser fileChooser = new FileChooser();
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
