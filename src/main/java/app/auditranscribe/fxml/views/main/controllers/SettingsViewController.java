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

import app.auditranscribe.audio.FFmpegHandler;
import app.auditranscribe.fxml.IconHelper;
import app.auditranscribe.fxml.Popups;
import app.auditranscribe.fxml.views.AbstractViewController;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.data_files.DataFiles;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * Controller for the settings view.
 */
public class SettingsViewController extends AbstractViewController {
    // Attributes
    private String lastValidFFmpegPath;

    // FXML elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private TabPane settingsTabPane;

    // "Audio" tab
    @FXML
    private TextField ffmpegPathTextField;

    @FXML
    private Button selectFFmpegBinaryButton;

    // "Input/Output" tab

    // "Transcription" tab

    // "Miscellaneous" tab

    // Bottom
    @FXML
    private Button resetToDefaultsButton, cancelButton, okButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Add methods to text fields
        ffmpegPathTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            // Handle check for FFmpeg path only when unfocused
            if (!newValue) {
                // Get the absolute path to the FFmpeg binary
                String ffmpegBinaryPath = ffmpegPathTextField.getText();

                // Check if the FFmpeg binary is valid
                if (FFmpegHandler.checkFFmpegPath(ffmpegBinaryPath)) {
                    lastValidFFmpegPath = ffmpegBinaryPath;
                    log(Level.INFO, "FFmpeg binary path updated to '" + ffmpegBinaryPath + "'");
                } else {
                    // Reset the value of the text field to the last valid FFmpeg path
                    ffmpegPathTextField.setText(lastValidFFmpegPath);

                    // Show a warning message
                    Popups.showWarningAlert(
                            null, "Invalid FFmpeg Binary Path",
                            "The provided path does not seem to point to a valid FFmpeg binary."
                    );

                    // Report failure
                    log(Level.WARNING, "Selected FFmpeg binary path '" + ffmpegBinaryPath + "' invalid");
                }
            }
        });

        // Set up tabs' buttons
        selectFFmpegBinaryButton.setOnAction(event -> {
            // Define file extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                    "FFmpeg binary",
                    "*.exe", "*"
            );

            // Get the file
            File possibleFFmpegBinary = IOMethods.openFileDialog(
                    rootPane.getScene().getWindow(), extFilter
            );

            // Check if the FFmpeg binary is valid
            if (possibleFFmpegBinary != null) {
                ffmpegPathTextField.setText(possibleFFmpegBinary.getAbsolutePath());
            } else {
                Popups.showInformationAlert(rootPane.getScene().getWindow(), "Info", "No file selected.");
            }
        });

        // Set up bottom buttons
        resetToDefaultsButton.setOnAction(event -> resetSettingsToDefaults());
        cancelButton.setOnAction(event -> closeSettingsPage());
        okButton.setOnAction(event -> {
            applySettings();
            closeSettingsPage();
        });

        // Todo add more
        log("Settings view ready to be shown");
    }

    // Public methods

    @Override
    public void setThemeOnScene() {
        updateThemeCSS(rootPane);

        // Set graphics
        IconHelper.setSVGOnButton(selectFFmpegBinaryButton, 15, 30, "folder-line");
    }

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
            Stage settingsStage = new Stage();
            settingsStage.initModality(Modality.APPLICATION_MODAL);
            settingsStage.setTitle("Settings");
            settingsStage.setScene(scene);
            settingsStage.setResizable(false);

            // Show the stage
            settingsStage.show();
        } catch (IOException e) {
            logException(e);
            throw new RuntimeException(e);
        }
    }

    // Private methods

    /**
     * Helper method that sets up the fields for the settings view.
     */
    private void setUpFields() {
        // Update the last valid FFmpeg path, and set up the FFmpeg binary path text field
        lastValidFFmpegPath = DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath;
        ffmpegPathTextField.setText(lastValidFFmpegPath);

        // Todo add others
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
        // Update settings' values
        DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath = ffmpegPathTextField.getText();

//        DataFiles.SETTINGS_DATA_FILE.data.autosaveInterval = autosaveIntervalSpinner.getValue();
//        DataFiles.SETTINGS_DATA_FILE.data.logFilePersistence = logFilePersistenceSpinner.getValue();
//
//        DataFiles.SETTINGS_DATA_FILE.data.colourScaleEnumOrdinal = colourScaleChoiceBox.getValue().ordinal();
//        DataFiles.SETTINGS_DATA_FILE.data.windowFunctionEnumOrdinal = windowFunctionChoiceBox.getValue().ordinal();
//        DataFiles.SETTINGS_DATA_FILE.data.noteQuantizationUnitEnumOrdinal =
//                noteQuantizationChoiceBox.getValue().ordinal();
//
//        DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal = themeChoiceBox.getValue().ordinal();
//        DataFiles.SETTINGS_DATA_FILE.data.checkForUpdateInterval = checkForUpdateIntervalSpinner.getValue();

        // Apply settings to the settings file
        DataFiles.SETTINGS_DATA_FILE.saveFile();
    }

    /**
     * Helper method that sets the values of the current tab to defaults.
     */
    private void resetSettingsToDefaults() {
        // Get currently selected tab
        int selectedTabIndex = settingsTabPane.getSelectionModel().getSelectedIndex();
        String selectedTabName;

        switch (selectedTabIndex) {
            case 0 -> {  // "Audio" tab
                selectedTabName = "Audio";

                ffmpegPathTextField.setText(DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath);
            }
            case 1 -> {  // "Input/Output" tab
                selectedTabName = "Input/Output";
            }
            case 2 -> {  // "Transcription" tab
                selectedTabName = "Transcription";
            }
            default -> {  // "Miscellaneous" tab
                selectedTabName = "Miscellaneous";
            }
        }

        // Report that reset to defaults was successful
        String infoStr = "Reset '" + selectedTabName + "' tab settings to defaults";
        Popups.showInformationAlert(rootPane.getScene().getWindow(), "Reset to Defaults", infoStr + ".");
        log(infoStr);
    }
}
