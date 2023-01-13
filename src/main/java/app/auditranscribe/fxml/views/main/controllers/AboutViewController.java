/*
 * AboutViewController.java
 * Description: Controller for the about view.
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

package app.auditranscribe.fxml.views.main.controllers;

import app.auditranscribe.fxml.Theme;
import app.auditranscribe.fxml.views.AbstractViewController;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.PropertyFile;
import app.auditranscribe.io.exceptions.NoSuchPropertyException;
import app.auditranscribe.utils.GUIUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * Controller for the about view.
 */
public class AboutViewController extends AbstractViewController {
    // FXML elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private ImageView bannerImage;

    @FXML
    private Label versionLabel;

    @FXML
    private Hyperlink websiteHyperlink, licencesHyperlink;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Get the project properties file
            PropertyFile projectPropertiesFile = new PropertyFile("project.properties");

            // Update the version label with the version number
            versionLabel.setText(projectPropertiesFile.getProperty("version"));

            // Set hyperlink methods
            websiteHyperlink.setOnAction(actionEvent ->
                    GUIUtils.openURLInBrowser("https://auditranscribe.app")
            );
            licencesHyperlink.setOnAction(actionEvent ->
                    GUIUtils.openURLInBrowser("https://auditranscribe.app/licences")
            );

            // Report that the "about" view is ready to be shown
            log(Level.INFO, "About view ready to be shown");

        } catch (IOException | NoSuchPropertyException e) {
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
            FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL("fxml/views/main/about-view.fxml"));
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

    // Overridden methods
    @Override
    public void setThemeOnScene() {
        Theme theme = updateThemeCSS(rootPane);

        // Set graphics
        bannerImage.setImage(new Image(IOMethods.getFileURLAsString(
                "images/logo-and-banner/banner-" + theme.shortName + ".png"
        )));
    }
}
