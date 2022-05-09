/*
 * MainViewController.java
 *
 * Created on 2022-02-09
 * Updated on 2022-05-09
 *
 * Description: Contains the main view's controller class.
 */

package site.overwrite.auditranscribe.views;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import site.overwrite.auditranscribe.io.ProjectIOHandlers;
import site.overwrite.auditranscribe.io.PropertyFile;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainViewController implements Initializable {
    // Attributes
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
            newProjectButton.setOnAction(ProjectIOHandlers::newProject);
            openProjectButton.setOnAction(ProjectIOHandlers::openProject);

            // Report that the main view is ready to be shown
            logger.log(Level.INFO, "Main view ready to be shown");

        } catch (IOException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
