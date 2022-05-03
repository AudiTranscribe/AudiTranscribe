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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import site.overwrite.auditranscribe.io.PropertyFile;
import site.overwrite.auditranscribe.utils.FileUtils;

import java.io.File;
import java.io.IOException;
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
                    // Todo: validate that the file is an audio file

                    // Get the current stage
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setUserData(file);

                    // Close the current stage
                    stage.close();

                    // Get the spectrogram view scene
                    FXMLLoader fxmlLoader = new FXMLLoader(FileUtils.getFileURL("views/fxml/spectrogram-view.fxml"));
                    Scene scene;
                    try {
                        scene = new Scene(fxmlLoader.load());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    // After initialization set the audio file for the spectrogram
                    ((SpectrogramViewController) fxmlLoader.getController()).setAudioFile(file);

                    // Set the new scene
                    stage.setScene(scene);

                    // Set new scene properties
                    stage.setMaximized(true);
                    stage.setFullScreen(true);
                    stage.setTitle(file.getName());

                    // Show the new scene
                    stage.show();

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
