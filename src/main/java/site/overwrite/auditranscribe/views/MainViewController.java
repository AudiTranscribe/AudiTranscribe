/*
 * MainViewController.java
 *
 * Created on 2022-02-09
 * Updated on 2022-05-02
 *
 * Description: Contains the main view's controller class.
 */

package site.overwrite.auditranscribe.views;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import site.overwrite.auditranscribe.io.PropertyFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainViewController implements Initializable {
    // Attributes
    final FileChooser fileChooser = new FileChooser();
    private final Desktop desktop = Desktop.getDesktop();

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

    // Initialization function
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
                    System.out.println(file);
                } else {
                    logger.log(Level.INFO, "No file selected");
                }
            });

        } catch (IOException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

    }
}
