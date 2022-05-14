/*
 * ProjectIOHandlers.java
 *
 * Created on 2022-05-04
 * Updated on 2022-05-14
 *
 * Description: Methods that handle the IO operations for an AudiTranscribe project.
 */

package site.overwrite.auditranscribe.views;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.*;
import org.javatuples.Pair;
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
     * @param mainStage          Main stage.
     * @param transcriptionStage Stage that contains the transcription scene.
     * @param file               File to open.
     */
    public static void newProject(Stage mainStage, Stage transcriptionStage, File file) {
        // Verify that the user choose a file
        if (file != null) {
            try {
                // Try and read the file as an audio file
                Audio audio = new Audio(file);  // Failure to read will throw an exception

                // Get the current scene and the spectrogram view controller
                Pair<Scene, SpectrogramViewController> stageSceneAndController = getController(transcriptionStage);
                Scene scene = stageSceneAndController.getValue0();
                SpectrogramViewController controller = stageSceneAndController.getValue1();

                // Set the project data for the existing project
                controller.setAudioAndSpectrogramData(audio);
                controller.finishSetup(mainStage);

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
                    mainStage.show();  // Show the main scene upon the spectrogram scene's closure
                }

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
     * Method that handles the opening of an existing AudiTranscribe project.
     *
     * @param mainStage          Main stage.
     * @param transcriptionStage Stage that contains the transcription scene.
     * @param file               File to open.
     */
    public static void openProject(Stage mainStage, Stage transcriptionStage, File file) {
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
                Pair<Scene, SpectrogramViewController> stageSceneAndController = getController(transcriptionStage);
                Scene scene = stageSceneAndController.getValue0();
                SpectrogramViewController controller = stageSceneAndController.getValue1();

                // Set the project data for the existing project
                controller.useExistingData(audtFilePath, audtFileName, projectDataObject);
                controller.finishSetup(mainStage);

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
                    mainStage.show();  // Show the main scene upon the spectrogram scene's closure
                }

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
     * Method that handles the saving of an AudiTranscribe project.
     *
     * @param filepath          <b>Absolute</b> path to the AUDT file.
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

    /**
     * Method that gets the window of the event.
     *
     * @param actionEvent Event caller.
     * @return WindowFunction.
     */
    public static Window getWindow(ActionEvent actionEvent) {
        return ((Node) actionEvent.getSource()).getScene().getWindow();
    }

    /**
     * Method that gets the window of the event.
     *
     * @param keyEvent Event caller.
     * @return WindowFunction.
     */
    public static Window getWindow(KeyEvent keyEvent) {
        return ((Scene) keyEvent.getSource()).getWindow();
    }

    /**
     * Method that gets the window of the event.
     *
     * @param mouseEvent Event caller.
     * @return WindowFunction.
     */
    public static Window getWindow(MouseEvent mouseEvent) {
        return ((Node) mouseEvent.getSource()).getScene().getWindow();
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
     * Helper method that gets the spectrogram scene and spectrogram view controller.
     *
     * @param transcriptionStage Stage that contains the transcription scene.
     * @return Spectrogram scene object and the <code>SpectrogramViewController</code> object.
     * @throws IOException If the spectrogram view FXML file cannot be found.
     */
    private static Pair<Scene, SpectrogramViewController> getController(
            Stage transcriptionStage
    ) throws IOException {
        // Unset full screen and maximized first
        transcriptionStage.setMaximized(false);
        transcriptionStage.setFullScreen(false);

        // Get the FXML loader for the spectrogram view
        FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL("views/fxml/spectrogram-view.fxml"));

        // Get the spectrogram view scene
        Scene scene = new Scene(fxmlLoader.load());

        // Get the spectrogram view controller
        SpectrogramViewController controller = fxmlLoader.getController();

        // Return the scene and controller
        return new Pair<>(scene, controller);
    }
}
