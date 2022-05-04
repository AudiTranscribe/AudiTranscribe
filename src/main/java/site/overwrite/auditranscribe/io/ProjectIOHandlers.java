/*
 * ProjectIOHandlers.java
 *
 * Created on 2022-05-04
 * Updated on 2022-05-04
 *
 * Description: Methods that handle the IO operations for an AudiTranscribe project.
 */

package site.overwrite.auditranscribe.io;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.utils.FileUtils;
import site.overwrite.auditranscribe.views.SpectrogramViewController;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Methods that handle the IO operations for an AudiTranscribe project.
 */
public class ProjectIOHandlers {
    // Public methods

    /**
     * Helper method that handles the creation of a new AudiTranscribe project.
     *
     * @param actionEvent Event that triggered this function.
     */
    public static void newProject(ActionEvent actionEvent) {
        // Get current window
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();

        // Ask user to choose a file
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(window);

        // Verify that the user choose a file
        if (file != null) {
            try {
                // Try and read the file as an audio file
                Audio audio = new Audio(file);  // Failure to read will throw an exception

                // Get the current stage
                Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                stage.setUserData(file);

                // Close the current stage
                stage.close();

                // Get the FXML loader for the spectrogram view
                FXMLLoader fxmlLoader = new FXMLLoader(
                        FileUtils.getFileURL("views/fxml/spectrogram-view.fxml")
                );
                Scene scene;

                // Get the spectrogram view scene
                scene = new Scene(fxmlLoader.load());

                // After initialization set the audio file for the spectrogram
                ((SpectrogramViewController) fxmlLoader.getController()).setAudioFile(audio);

                // Set the new scene
                stage.setScene(scene);

                // Set new scene properties
                stage.setMaximized(true);
                stage.setFullScreen(true);
                stage.setResizable(false);
                stage.setTitle(file.getName());

                // Show the new scene
                stage.show();

            } catch (UnsupportedAudioFileException | IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initStyle(StageStyle.UTILITY);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to read '" + file.getName() + "' as a WAV file.");
                alert.setContentText(
                        "The program failed to read '" + file.getName() + "' as a WAV file. Please check if " +
                                "this is a valid WAV file."
                );

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

                // Set expandable Exception into the dialog pane.
                alert.getDialogPane().setExpandableContent(expContent);

                alert.showAndWait();
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
}
