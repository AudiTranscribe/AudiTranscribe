/*
 * PreferencesViewController.java
 * Description: Contains the preferences view's controller class.
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

package site.overwrite.auditranscribe.main_views;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import site.overwrite.auditranscribe.audio.FFmpegHandler;
import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.data_files.DataFiles;
import site.overwrite.auditranscribe.misc.MyLogger;
import site.overwrite.auditranscribe.misc.Theme;
import site.overwrite.auditranscribe.misc.spinners.CustomDoubleSpinnerValueFactory;
import site.overwrite.auditranscribe.misc.spinners.CustomIntegerSpinnerValueFactory;
import site.overwrite.auditranscribe.spectrogram.ColourScale;
import site.overwrite.auditranscribe.misc.Popups;
import site.overwrite.auditranscribe.main_views.helpers.ProjectIOHandlers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class PreferencesViewController implements Initializable {
    // Attributes
    private String lastValidFFmpegPath;

    // FXML Elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private ChoiceBox<ColourScale> colourScaleChoiceBox;

    @FXML
    private ChoiceBox<WindowFunction> windowFunctionChoiceBox;

    @FXML
    private ChoiceBox<Theme> themeChoiceBox;

    @FXML
    private Spinner<Integer> autosaveIntervalSpinner, logFilePersistenceSpinner, checkForUpdateIntervalSpinner;

    @FXML
    private Spinner<Double> notePlayingDelayOffsetSpinner;

    @FXML
    private Button selectFFmpegBinaryButton;

    @FXML
    private TextField ffmpegBinaryPathTextField;

    @FXML
    private Button cancelButton, applyButton, okButton;

    // Initialization method
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set choice box selections
        for (Theme theme : Theme.values()) themeChoiceBox.getItems().add(theme);

        for (ColourScale colourScale : ColourScale.values()) colourScaleChoiceBox.getItems().add(colourScale);
        for (WindowFunction windowFunction : WindowFunction.values())
            windowFunctionChoiceBox.getItems().add(windowFunction);

        // Add methods to buttons
        selectFFmpegBinaryButton.setOnAction(event -> {
            // Define file extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                    "FFmpeg binary",
                    "*.exe", "*"
            );

            // Get the file
            File possibleFFmpegBinary = ProjectIOHandlers.getFileFromFileDialog(
                    rootPane.getScene().getWindow(), extFilter
            );

            // Check if the FFmpeg binary is valid
            if (possibleFFmpegBinary != null) {
                // Update the value of the FFmpeg path text field
                ffmpegBinaryPathTextField.setText(possibleFFmpegBinary.getAbsolutePath());
            } else {
                Popups.showInformationAlert("Info", "No file selected.");
            }
        });

        cancelButton.setOnAction(event -> closePreferencesPane());

        applyButton.setOnAction(event -> applySettings());

        okButton.setOnAction(event -> {
            applySettings();
            closePreferencesPane();
        });

        // Add methods to text fields
        ffmpegBinaryPathTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            // Handle check for FFmpeg path only when unfocused
            if (!newValue) {
                // Get the absolute path to the FFmpeg binary
                String ffmpegBinaryPath = ffmpegBinaryPathTextField.getText();

                // Check if the FFmpeg binary is valid
                if (FFmpegHandler.checkFFmpegPath(ffmpegBinaryPath)) {
                    // Update the last valid FFmpeg path
                    lastValidFFmpegPath = ffmpegBinaryPath;

                    // Enable the apply button
                    applyButton.setDisable(false);

                    // Report success
                    MyLogger.log(
                            Level.INFO,
                            "FFmpeg binary path updated to: " + ffmpegBinaryPath,
                            this.getClass().toString()
                    );
                } else {
                    // Reset the value of the text field to the last valid FFmpeg path
                    ffmpegBinaryPathTextField.setText(lastValidFFmpegPath);

                    // Show a warning message
                    Popups.showWarningAlert(
                            "Invalid FFmpeg Binary Path",
                            "The provided path does not seem to point to a valid FFmpeg binary."
                    );

                    // Report failure
                    MyLogger.log(
                            Level.WARNING,
                            "Selected FFmpeg binary path \"" + ffmpegBinaryPath + "\" invalid",
                            this.getClass().toString()
                    );
                }
            }
        });

        // Report that the preferences view is ready to be shown
        MyLogger.log(Level.INFO, "Preferences view ready to be shown", this.getClass().toString());
    }

    // Public methods

    /**
     * Method that sets the theme for the scene.<br>
     * Note that this method has to be called after the setting file has been set.
     *
     * @param theme The theme to set.
     */
    public void setThemeOnScene(Theme theme) {
        // Set stylesheets
        rootPane.getStylesheets().clear();  // Reset the stylesheets first before adding new ones

        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/base.css"));
        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/" + theme.cssFile));
    }

    /**
     * Method that sets the theme for the scene.<br>
     * Note that this method has to be called after the setting file has been set.
     */
    public void setThemeOnScene() {
        setThemeOnScene(Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal]);
    }

    /**
     * Method that sets up the fields.
     */
    public void setUpFields() {
        // Arrays that store the fields that just need to disable the apply button
        ChoiceBox<?>[] choiceBoxes = new ChoiceBox[]{colourScaleChoiceBox, windowFunctionChoiceBox};
        Spinner<?>[] spinners = new Spinner[]{
                notePlayingDelayOffsetSpinner, autosaveIntervalSpinner, logFilePersistenceSpinner,
                checkForUpdateIntervalSpinner
        };

        // Set choice box values
        themeChoiceBox.setValue(Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal]);

        colourScaleChoiceBox.setValue(ColourScale.values()[DataFiles.SETTINGS_DATA_FILE.data.colourScaleEnumOrdinal]);
        windowFunctionChoiceBox.setValue(
                WindowFunction.values()[DataFiles.SETTINGS_DATA_FILE.data.windowFunctionEnumOrdinal]
        );

        // Add methods to choice boxes
        for (ChoiceBox<?> choiceBox : choiceBoxes) {
            choiceBox.setOnAction(event -> applyButton.setDisable(false));
        }

        themeChoiceBox.setOnAction(event -> {
            applyButton.setDisable(false);
            setThemeOnScene(themeChoiceBox.getValue());
        });

        // Set spinner factories and methods
        notePlayingDelayOffsetSpinner.setValueFactory(new CustomDoubleSpinnerValueFactory(
                -1, 1, DataFiles.SETTINGS_DATA_FILE.data.notePlayingDelayOffset, 0.01, 2
        ));
        autosaveIntervalSpinner.setValueFactory(new CustomIntegerSpinnerValueFactory(
                1, Integer.MAX_VALUE, DataFiles.SETTINGS_DATA_FILE.data.autosaveInterval, 1
        ));
        logFilePersistenceSpinner.setValueFactory(new CustomIntegerSpinnerValueFactory(
                1, Integer.MAX_VALUE, DataFiles.SETTINGS_DATA_FILE.data.logFilePersistence, 1
        ));
        checkForUpdateIntervalSpinner.setValueFactory(new CustomIntegerSpinnerValueFactory(
                1, 720, DataFiles.SETTINGS_DATA_FILE.data.checkForUpdateInterval, 1
        ));

        for (Spinner<?> spinner : spinners) {
            spinner.valueProperty().addListener((observable, oldValue, newValue) -> applyButton.setDisable(false));
        }

        // Update the last valid FFmpeg path, and set up the FFmpeg binary path text field
        lastValidFFmpegPath = DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath;
        ffmpegBinaryPathTextField.setText(lastValidFFmpegPath);
    }

    /**
     * Method that shows the preferences window.
     */
    public static void showPreferencesWindow() {
        try {
            // Load the FXML file into the scene
            FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL(
                    "views/fxml/main/preferences-view.fxml"
            ));
            Scene scene = new Scene(fxmlLoader.load());

            // Get the view controller
            PreferencesViewController controller = fxmlLoader.getController();

            // Set the theme of the scene
            controller.setThemeOnScene();

            // Set choice boxes' values
            controller.setUpFields();

            // Set stage properties
            Stage preferencesStage = new Stage();
            preferencesStage.initStyle(StageStyle.UTILITY);
            preferencesStage.setTitle("Settings and Preferences");
            preferencesStage.setScene(scene);
            preferencesStage.setResizable(false);

            // Show the stage
            preferencesStage.show();
        } catch (IOException e) {
            MyLogger.logException(e);
            throw new RuntimeException(e);
        }
    }

    // Private methods

    /**
     * Helper method that closes the preferences' pane window.
     */
    private void closePreferencesPane() {
        ((Stage) rootPane.getScene().getWindow()).close();
    }

    /**
     * Helper method that updates the settings file with the new settings.
     */
    private void applySettings() {
        // Update settings' values
        DataFiles.SETTINGS_DATA_FILE.data.notePlayingDelayOffset = notePlayingDelayOffsetSpinner.getValue();
        DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath = ffmpegBinaryPathTextField.getText();

        DataFiles.SETTINGS_DATA_FILE.data.autosaveInterval = autosaveIntervalSpinner.getValue();
        DataFiles.SETTINGS_DATA_FILE.data.logFilePersistence = logFilePersistenceSpinner.getValue();

        DataFiles.SETTINGS_DATA_FILE.data.colourScaleEnumOrdinal = colourScaleChoiceBox.getValue().ordinal();
        DataFiles.SETTINGS_DATA_FILE.data.windowFunctionEnumOrdinal = windowFunctionChoiceBox.getValue().ordinal();

        DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal = themeChoiceBox.getValue().ordinal();
        DataFiles.SETTINGS_DATA_FILE.data.checkForUpdateInterval = checkForUpdateIntervalSpinner.getValue();

        // Apply settings to the settings file
        DataFiles.SETTINGS_DATA_FILE.saveFile();

        // Disable the apply button again
        applyButton.setDisable(true);
    }
}
