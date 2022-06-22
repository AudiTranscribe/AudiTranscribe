/*
 * AboutViewController.java
 *
 * Created on 2022-05-08
 * Updated on 2022-06-22
 *
 * Description: View controller for the "about" window.
 */

package site.overwrite.auditranscribe.views.main_views;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.PropertyFile;
import site.overwrite.auditranscribe.io.json_files.file_classes.SettingsFile;
import site.overwrite.auditranscribe.misc.Theme;
import site.overwrite.auditranscribe.utils.MiscUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AboutViewController implements Initializable {
    // Attributes
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private SettingsFile settingsFile;

    // FXML Elements
    @FXML
    private Pane rootPane;

    @FXML
    private ImageView bannerImage;

    @FXML
    private Label versionLabel;

    @FXML
    private Hyperlink websiteHyperlink, licencesHyperlink;

    // Initialization method
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Get the project properties file
            PropertyFile projectPropertiesFile = new PropertyFile("project.properties");

            // Update the version label with the version number
            versionLabel.setText("Version: " + projectPropertiesFile.getProperty("version"));

            // Set hyperlink methods
            websiteHyperlink.setOnAction(actionEvent ->
                    MiscUtils.openURLInBrowser("https://auditranscribe.app")
            );
            licencesHyperlink.setOnAction(actionEvent ->
                    MiscUtils.openURLInBrowser("https://auditranscribe.app/licences")
            );

            // Report that the "about" view is ready to be shown
            logger.log(Level.INFO, "About view ready to be shown");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Setter methods
    public void setSettingsFile(SettingsFile settingsFile) {
        this.settingsFile = settingsFile;
    }

    // Public methods

    /**
     * Method that sets the theme for the scene.
     */
    public void setThemeOnScene() {
        // Get the theme
        Theme theme = Theme.values()[settingsFile.data.themeEnumOrdinal];

        // Set stylesheets
        rootPane.getStylesheets().clear();  // Reset the stylesheets first before adding new ones

        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/base.css"));
        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/" + theme.cssFile));

        // Set graphics
        bannerImage.setImage(new Image(IOMethods.getFileURLAsString(
                "images/logo-and-banner/banner-" + theme.shortName + ".png"
        )));
    }

    /**
     * Method that shows the "about" window.
     *
     * @param settingsFile The <code>SettingsFile</code> object that handles the reading and writing
     *                     of settings.
     */
    public static void showAboutWindow(SettingsFile settingsFile) {
        try {
            // Load the FXML file into the scene
            FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL("views/fxml/main-views/about-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Get the view controller
            AboutViewController controller = fxmlLoader.getController();

            // Update the `settingsFile` attribute
            controller.setSettingsFile(settingsFile);

            // Set the theme of the scene
            controller.setThemeOnScene();

            // Set stage properties
            Stage aboutStage = new Stage();
            aboutStage.initStyle(StageStyle.UTILITY);
            aboutStage.setTitle("About AudiTranscribe");
            aboutStage.setScene(scene);
            aboutStage.setResizable(false);

            // Show the stage
            aboutStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
