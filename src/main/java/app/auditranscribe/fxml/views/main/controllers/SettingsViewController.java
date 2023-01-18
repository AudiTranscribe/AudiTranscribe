/*
 * SettingsViewController.java
 * Description: Controller for the settings view.
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

import app.auditranscribe.fxml.views.AbstractViewController;
import app.auditranscribe.io.IOMethods;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the settings view.
 */
public class SettingsViewController extends AbstractViewController {
    // FXML elements
    @FXML
    private AnchorPane rootPane;

    // "Audio" tab

    // "Input/Output" tab

    // "Transcription" tab

    // "Miscellaneous" tab

    // Bottom
    @FXML
    private Button resetToDefaultsButton, cancelButton, okButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set up bottom buttons
        resetToDefaultsButton.setOnAction(event -> resetSettingsToDefaults());
        cancelButton.setOnAction(event -> closeSettingsPage());
        okButton.setOnAction(event -> {
            applySettings();
            closeSettingsPage();
        });

        // Todo add
        log("Settings view ready to be shown");
    }

    // Public methods

    /**
     * Method that shows the settings window.
     */
    public static void showSettingsWindow() {
        try {
            // Load the FXML file into the scene
            FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL("fxml/views/main/settings-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Get the view controller
            SettingsViewController controller = fxmlLoader.getController();

            // Set the theme of the scene
            controller.setThemeOnScene();

            // Set choice boxes' values
            controller.setUpFields();

            // Set stage properties
            Stage preferencesStage = new Stage();
            preferencesStage.initModality(Modality.APPLICATION_MODAL);
            preferencesStage.setTitle("Settings");
            preferencesStage.setScene(scene);
            preferencesStage.setResizable(false);

            // Show the stage
            preferencesStage.show();
        } catch (IOException e) {
            logException(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setThemeOnScene() {
        updateThemeCSS(rootPane);
    }

    // Private methods

    /**
     * Helper method that sets up the fields for the settings view.
     */
    private void setUpFields() {
        // Todo implement
        log("setUpFields");
    }

    /**
     * Helper method that closes the settings view.
     */
    private void closeSettingsPage() {
        ((Stage) rootPane.getScene().getWindow()).close();
        log("Closing settings page");
    }

    /**
     * Helper method that updates the settings file with the new settings.
     */
    private void applySettings() {
        // Todo implement
        log("applySettings");
    }

    /**
     * Helper method that sets the values of the current tab to defaults.
     */
    private void resetSettingsToDefaults() {
        // Todo set values of current tab to defaults, but don't save yet
        log("resetSettingsToDefaults");
    }
}
