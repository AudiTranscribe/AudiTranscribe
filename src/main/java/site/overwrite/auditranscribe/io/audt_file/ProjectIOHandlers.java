/*
 * ProjectIOHandlers.java
 *
 * Created on 2022-05-04
 * Updated on 2022-05-13
 *
 * Description: Methods that handle the IO operations for an AudiTranscribe project.
 */

package site.overwrite.auditranscribe.io.audt_file;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.*;
import org.javatuples.Triplet;
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
import site.overwrite.auditranscribe.views.SpectrogramViewController;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

// Main class

/**
 * Methods that handle the IO operations for an AudiTranscribe project.
 */
public class ProjectIOHandlers {
    // Public methods

    /**
     * Method that handles the creation of a new AudiTranscribe project.
     *
     * @param event Action event that triggered this method.
     */
    public static void newProject(ActionEvent event) {
        // Get current window
        Window window = ((Node) event.getSource()).getScene().getWindow();

        // Continue the process
        continueMakingNewProject(window);
    }

    /**
     * Method that handles the creation of a new AudiTranscribe project.
     *
     * @param event Key event that triggered this method.
     */
    public static void newProject(KeyEvent event) {
        // Get current window
        Window window = ((Scene) event.getSource()).getWindow();

        // Continue the process
        continueMakingNewProject(window);
    }

    /**
     * Method that handles the opening of an existing AudiTranscribe project.
     *
     * @param event Event that triggered this method.
     */
    public static void openProject(ActionEvent event) {
        // Get current window
        Window window = ((Node) event.getSource()).getScene().getWindow();

        // Continue the process
        continueOpeningProject(window);
    }

    /**
     * Method that handles the opening of an existing AudiTranscribe project.
     *
     * @param event Event that triggered this method.
     */
    public static void openProject(KeyEvent event) {
        // Get current window
        Window window = ((Scene) event.getSource()).getWindow();

        // Continue the process
        continueOpeningProject(window);
    }

    /**
     * Method that handles the saving of an AudiTranscribe project.
     *
     * @param filepath          Path to the AUDT file.
     * @param projectDataObject Data object that stores all the data for the project.
     * @throws IOException If the writing to file encounters an error.
     */
    public static void saveProject(String filepath, ProjectDataObject projectDataObject) throws IOException {
        // Declare the file writer object
        AUDTFileWriter fileWriter = new AUDTFileWriter(filepath);

        // Write data to the file
        fileWriter.writeQTransformData(projectDataObject.qTransformData);
        fileWriter.writeAudioData(projectDataObject.audioData);
        fileWriter.writeGUIData(projectDataObject.guiData);

        fileWriter.writeBytesToFile();
    }

    // Private methods

    /**
     * Helper method that shows an exception alert.
     *
     * @param headerText  Header text for the exception alert.
     * @param contentText Content text for the exception alert.
     * @param e           Exception that occurred.
     */
    private static void showExceptionAlert(String headerText, String contentText, Exception e) {
        // Create a new error alert
        Alert alert = new Alert(Alert.AlertType.ERROR);

        // Set the alert style to `UTILITY` so that it can be shown during fullscreen
        alert.initStyle(StageStyle.UTILITY);

        // Set texts
        alert.setTitle("Error");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        // Set exception texts
        Label label = new Label("The exception stacktrace was:");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);

        // Show the alert
        alert.showAndWait();
    }

    /**
     * Helper method that finishes the new project creation.
     *
     * @param window Current window.
     */
    private static void continueMakingNewProject(Window window) {
        // Ask user to choose a file
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(window);

        // Verify that the user choose a file
        if (file != null) {
            try {
                // Try and read the file as an audio file
                Audio audio = new Audio(file);  // Failure to read will throw an exception

                // Get the stage, scene and controller
                Triplet<Stage, Scene, SpectrogramViewController> stageSceneAndController = getController(window);
                Stage stage = stageSceneAndController.getValue0();
                Scene scene = stageSceneAndController.getValue1();
                SpectrogramViewController controller = stageSceneAndController.getValue2();

                // Set the project data for the existing project
                controller.setAudioAndSpectrogramData(audio);
                controller.finishSetup();

                // Set the new scene
                stage.setScene(scene);

                // Set new scene properties
                stage.setMaximized(true);
                stage.setResizable(true);
                stage.setTitle(file.getName());

                // Show the new scene
                stage.show();

                // Update the minimum width and height
                stage.setMinWidth(stage.getWidth());
                stage.setMinHeight(stage.getHeight());

            } catch (UnsupportedAudioFileException | IOException e) {
                showExceptionAlert(
                        "Failed to read '" + file.getName() + "' as a WAV file.",
                        "The program failed to read '" + file.getName() +
                                "' as a WAV file. Please check if " + "this is a valid WAV file.",
                        e
                );
                e.printStackTrace();
            }

        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Info");
            alert.setHeaderText(null);
            alert.setContentText("No file selected.");

            alert.showAndWait();
        }
    }

    /**
     * Helper method that finishes the opening of an AudiTranscribe project
     *
     * @param window Current window.
     */
    private static void continueOpeningProject(Window window) {
        // Ask user to choose a file
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(window);

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
                ProjectDataObject projectDataObject = new ProjectDataObject(
                        qTransformData, audioData, guiData
                );

                // Get the stage, scene and controller
                Triplet<Stage, Scene, SpectrogramViewController> stageSceneAndController = getController(window);
                Stage stage = stageSceneAndController.getValue0();
                Scene scene = stageSceneAndController.getValue1();
                SpectrogramViewController controller = stageSceneAndController.getValue2();

                // Set the project data for the existing project
                controller.useExistingData(audtFilePath, audtFileName, projectDataObject);
                controller.finishSetup();

                // Set the new scene
                stage.setScene(scene);

                // Set new scene properties
                stage.setMaximized(true);
                stage.setResizable(true);
                stage.setTitle(projectDataObject.guiData.audioFileName);

                // Show the new scene
                stage.show();

                // Update the minimum width and height
                stage.setMinWidth(stage.getWidth());
                stage.setMinHeight(stage.getHeight());

                // Update scroll position
                // (Annoyingly we have to do this AFTER the stage is shown)
                controller.updateScrollPosition(
                        projectDataObject.guiData.currTimeInMS / 1000. *
                                controller.PX_PER_SECOND *
                                controller.SPECTROGRAM_ZOOM_SCALE_X
                );

            } catch (IOException | IncorrectFileFormatException | FailedToReadDataException e) {
                showExceptionAlert(
                        "Failed to read '" + file.getName() + "' as an AUDT ile.",
                        "The program failed to read '" + file.getName() +
                                "' as an AUDT file. Please check if " + "this is a valid AUDT file.",
                        e
                );
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initStyle(StageStyle.UTILITY);
            alert.setTitle("Info");
            alert.setHeaderText(null);
            alert.setContentText("No file selected.");

            alert.showAndWait();
        }
    }

    /**
     * Helper method that gets the stage, scene and view controller of the spectrogram view.
     *
     * @param window Root window.
     * @return Stage, scene and view controller.
     * @throws IOException If the spectrogram view FXML file cannot be found.
     */
    private static Triplet<Stage, Scene, SpectrogramViewController> getController(Window window) throws IOException {
        // Get the current stage
        Stage stage = (Stage) window;

        // Unset full screen and maximized first
        stage.setMaximized(false);
        stage.setFullScreen(false);

        // Close the current stage
        stage.close();

        // Get the FXML loader for the spectrogram view
        FXMLLoader fxmlLoader = new FXMLLoader(
                IOMethods.getFileURL("views/fxml/spectrogram-view.fxml")
        );

        // Get the spectrogram view scene
        Scene scene = new Scene(fxmlLoader.load());

        // Get the spectrogram view controller
        SpectrogramViewController controller = fxmlLoader.getController();

        // Return the scene and controller
        return new Triplet<>(stage, scene, controller);
    }
}
