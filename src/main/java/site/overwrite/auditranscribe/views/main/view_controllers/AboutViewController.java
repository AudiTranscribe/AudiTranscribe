/*
 * AboutViewController.java
 * Description: View controller for the "about" window.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public Licence as published by the Free Software Foundation, either version 3 of the
 * Licence, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public Licence for more details.
 *
 * You should have received a copy of the GNU General Public Licence along with this program. If
 * not, see <https://www.gnu.org/licenses/>
 *
 * Copyright © AudiTranscribe Team
 */

package site.overwrite.auditranscribe.views.main.view_controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import site.overwrite.auditranscribe.generic.ClassWithLogging;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.PropertyFile;
import site.overwrite.auditranscribe.io.data_files.DataFiles;
import site.overwrite.auditranscribe.misc.Theme;
import site.overwrite.auditranscribe.utils.GUIUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class AboutViewController extends ClassWithLogging implements Initializable {
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
                    GUIUtils.openURLInBrowser("https://auditranscribe.app")
            );
            licencesHyperlink.setOnAction(actionEvent ->
                    GUIUtils.openURLInBrowser("https://auditranscribe.app/licences")
            );

            // Report that the "about" view is ready to be shown
            log(Level.INFO, "About view ready to be shown");

        } catch (IOException e) {
            logException(e);
            throw new RuntimeException(e);
        }
    }

    // Public methods

    /**
     * Method that shows the "about" window.
     */
    public static void showAboutWindow() {
        try {
            // Load the FXML file into the scene
            FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL("views/fxml/main/about-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Get the view controller
            AboutViewController controller = fxmlLoader.getController();

            // Set the theme of the scene
            controller.setThemeOnScene();

            // Set stage properties
            Stage aboutStage = new Stage();
            aboutStage.initStyle(StageStyle.UTILITY);
            aboutStage.initModality(Modality.APPLICATION_MODAL);
            aboutStage.setTitle("About AudiTranscribe");
            aboutStage.setScene(scene);
            aboutStage.setResizable(false);

            // Show the stage
            aboutStage.show();

            // Stop highlighting the hyperlink
            controller.rootPane.requestFocus();

        } catch (IOException e) {
            logException(e);
            throw new RuntimeException(e);
        }
    }

    // Private methods

    /**
     * Method that sets the theme for the scene.
     */
    public void setThemeOnScene() {
        // Get the theme
        Theme theme = Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal];

        // Set stylesheets
        rootPane.getStylesheets().clear();  // Reset the stylesheets first before adding new ones

        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/base.css"));
        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/" + theme.cssFile));

        // Set graphics
        bannerImage.setImage(new Image(IOMethods.getFileURLAsString(
                "images/logo-and-banner/banner-" + theme.shortName + ".png"
        )));
    }
}
