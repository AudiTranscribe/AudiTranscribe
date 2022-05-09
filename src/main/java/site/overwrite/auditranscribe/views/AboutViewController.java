/*
 * AboutViewController.java
 *
 * Created on 2022-05-08
 * Updated on 2022-05-09
 *
 * Description: View controller for the "about" window.
 */

package site.overwrite.auditranscribe.views;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import site.overwrite.auditranscribe.io.PropertyFile;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AboutViewController implements Initializable {
    // Attributes
    Desktop desktop = Desktop.getDesktop();

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    // FXML Elements
    @FXML
    public Label versionLabel;

    @FXML
    public Hyperlink websiteHyperlink, licencesHyperlink;

    // Initialization method
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Get the project properties file
            PropertyFile projectPropertiesFile = new PropertyFile("project.properties");

            // Update the version label with the version number
            versionLabel.setText("Version: " + projectPropertiesFile.getProperty("version"));

            // Set hyperlink methods
            websiteHyperlink.setOnAction(actionEvent -> openURLInBrowser("https://auditranscribe.app"));
            licencesHyperlink.setOnAction(actionEvent -> openURLInBrowser("https://auditranscribe.app/licences"));

            // Report that the "about" view is ready to be shown
            logger.log(Level.INFO, "About view ready to be shown");

        } catch (IOException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    // Helper methods

    /**
     * Helper method that opens the desired URL in the browser.
     * @param url   URL to open in the browser.
     */
    private void openURLInBrowser(String url) {
        try {
            desktop.browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
