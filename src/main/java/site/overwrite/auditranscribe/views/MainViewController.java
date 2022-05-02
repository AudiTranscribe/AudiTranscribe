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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import site.overwrite.auditranscribe.io.PropertyFile;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {
    // FXML Elements
    @FXML
    private Label versionLabel;

    @FXML
    private Button newProjectButton, openProjectButton;

    @FXML
    private TextField searchTextField;

    @FXML
    private ListView<String> projectsListView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Get the project properties file
            PropertyFile projectPropertiesFile = new PropertyFile("project.properties");

            // Update the version label with the version number
            versionLabel.setText("Version " + projectPropertiesFile.getProperty("version"));

        } catch (IOException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

    }
}
