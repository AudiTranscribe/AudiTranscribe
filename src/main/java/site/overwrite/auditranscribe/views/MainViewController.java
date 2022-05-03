/*
 * MainViewController.java
 *
 * Created on 2022-02-09
 * Updated on 2022-05-03
 *
 * Description: Contains the main view's controller class.
 */

package site.overwrite.auditranscribe.views;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.io.PropertyFile;
import site.overwrite.auditranscribe.utils.FileUtils;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainViewController implements Initializable {
    // Attributes
    final FileChooser fileChooser = new FileChooser();

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    // FXML Elements
    @FXML
    private Label versionLabel;

    @FXML
    private Button newProjectButton, openProjectButton;

    @FXML
    private TextField searchTextField;

    @FXML
    private ListView<String> projectsListView;

    // Initialization method
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Get the project properties file
            PropertyFile projectPropertiesFile = new PropertyFile("project.properties");

            // Update the version label with the version number
            versionLabel.setText("Version " + projectPropertiesFile.getProperty("version"));

            // Add methods to buttons
            newProjectButton.setOnAction(event -> {
                // Get current window
                Window window = ((Node) event.getSource()).getScene().getWindow();

                // Ask user to choose a file
                File file = fileChooser.showOpenDialog(window);

                // Verify that the user choose a file
                if (file != null) {
                    try {
                        // Try and read the file as an audio file
                        Audio audio = new Audio(file);  // Failure to read will throw an exception

                        // Get the current stage
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
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
                    logger.log(Level.INFO, "No file selected");
                }
            });

            // Report that the main view is ready to be shown
            logger.log(Level.INFO, "Main view ready to be shown");

        } catch (IOException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

    }
}
