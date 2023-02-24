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

import app.auditranscribe.audio.Audio;
import app.auditranscribe.audio.FFmpegHandler;
import app.auditranscribe.fxml.IconHelper;
import app.auditranscribe.fxml.Popups;
import app.auditranscribe.fxml.Theme;
import app.auditranscribe.fxml.plotting.ColourScale;
import app.auditranscribe.fxml.views.AbstractViewController;
import app.auditranscribe.io.IOConstants;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.data_files.DataFiles;
import app.auditranscribe.io.data_files.data_encapsulators.SettingsData;
import app.auditranscribe.misc.CustomLogger;
import app.auditranscribe.fxml.spinners.CustomIntegerSpinnerValueFactory;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import app.auditranscribe.music.NoteUnit;
import app.auditranscribe.signal.windowing.SignalWindow;
import app.auditranscribe.utils.GUIUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.sound.sampled.Mixer;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * Controller for the settings view.
 */
@ExcludeFromGeneratedCoverageReport
public class SettingsViewController extends AbstractViewController {
    // Attributes
    private String lastValidFFmpegPath;

    private List<Mixer.Info> audioDevices;

    private CustomIntegerSpinnerValueFactory autosaveIntervalSpinnerFactory, logFilePersistenceSpinnerFactory;

    // FXML elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private TabPane settingsTabPane;

    // "General" tab
    @FXML
    private ChoiceBox<Theme> themeChoiceBox;

    // "Audio" tab
    @FXML
    private TextField ffmpegPathTextField;

    @FXML
    private Button selectFFmpegBinaryButton;

    @FXML
    private ChoiceBox<MixerInfoDisplay> audioDeviceChoiceBox;

    @FXML
    private ChoiceBox<Integer> playbackBufferSizeChoiceBox;

    // "Input/Output" tab
    @FXML
    private Spinner<Integer> autosaveIntervalSpinner, logFilePersistenceSpinner;

    @FXML
    private Button deleteLogsButton, openDataFolderButton;

    // "Transcription" tab
    @FXML
    private ChoiceBox<ColourScale> colourScaleChoiceBox;

    @FXML
    private ChoiceBox<SignalWindow> windowFunctionChoiceBox;

    @FXML
    private ChoiceBox<NoteUnit> noteQuantizationChoiceBox;

    // Bottom
    @FXML
    private Button resetToDefaultsButton, cancelButton, okButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get available audio devices
        audioDevices = Audio.listOutputAudioDevices();

        // Add choice box options
        for (Theme theme : Theme.values())
            themeChoiceBox.getItems().add(theme);

        for (Mixer.Info info : audioDevices)
            audioDeviceChoiceBox.getItems().add(new MixerInfoDisplay(info));
        for (Integer bufferSize : Audio.VALID_PLAYBACK_BUFFER_SIZES)
            playbackBufferSizeChoiceBox.getItems().add(bufferSize);

        for (ColourScale colourScale : ColourScale.values())
            colourScaleChoiceBox.getItems().add(colourScale);
        for (SignalWindow windowFunction : SignalWindow.values())
            windowFunctionChoiceBox.getItems().add(windowFunction);
        for (NoteUnit noteQuantizationUnit : NoteUnit.values())
            noteQuantizationChoiceBox.getItems().add(noteQuantizationUnit);

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
            File possibleFFmpegBinary = GUIUtils.openFileDialog(
                    rootPane.getScene().getWindow(), extFilter
            );

            // Check if the FFmpeg binary is valid
            if (possibleFFmpegBinary != null) {
                ffmpegPathTextField.setText(possibleFFmpegBinary.getAbsolutePath());
            } else {
                Popups.showInformationAlert(rootPane.getScene().getWindow(), "Info", "No file selected.");
            }
        });

        deleteLogsButton.setOnAction(event -> {
            // Get the files in the logging folder
            File[] files = new File(CustomLogger.logsFolder).listFiles();

            // Exclude current log and its lock file
            if (files != null) {
                int numDeleted = 0;
                for (File file : files) {
                    if (!file.getName().equals(CustomLogger.currentLogName) && !file.getName().endsWith(".lck")) {
                        IOMethods.delete(file);
                        log(Level.FINE, "Deleted log '" + file.getName() + "'");
                        numDeleted++;
                    }
                }

                String title = "";
                String content = "No logs to delete.";

                if (numDeleted != 0) {
                    title = "Deleted Logs";
                    content = "Deleted " + numDeleted + " " + (numDeleted == 1 ? "log" : "logs") +
                            " from the logs folder.";
                }

                Popups.showInformationAlert(rootPane.getScene().getWindow(), title, content);
            }
        });

        openDataFolderButton.setOnAction(event -> GUIUtils.openFolderInGUI(IOConstants.APP_DATA_FOLDER_PATH));

        // Set up bottom buttons
        resetToDefaultsButton.setOnAction(event -> resetSettingsToDefaults());
        cancelButton.setOnAction(event -> closeSettingsPage());
        okButton.setOnAction(event -> {
            applySettings();
            closeSettingsPage();
        });

        log("Settings view ready to be shown");
    }

    // Public methods
    @Override
    public void setThemeOnScene(Theme theme) {
        updateThemeCSS(rootPane, theme);
        setGraphics(theme);
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
            settingsStage.showAndWait();

            // Remove view from active
            controller.removeControllerFromActive();

        } catch (IOException e) {
            logException(e);
            throw new RuntimeException(e);
        }
    }

    // Protected methods
    @Override
    protected void setGraphics(Theme theme) {
        IconHelper.setSVGOnButton(selectFFmpegBinaryButton, 15, 30, "folder-line");
    }

    // Private methods

    /**
     * Helper method that sets up the fields for the settings view.
     */
    private void setUpFields() {
        // Update the last valid FFmpeg path, and set up the FFmpeg binary path text field
        lastValidFFmpegPath = DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath;
        ffmpegPathTextField.setText(lastValidFFmpegPath);

        // Set choice box values
        themeChoiceBox.setValue(Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal]);

        audioDeviceChoiceBox.setValue(new MixerInfoDisplay(Audio.getOutputAudioDevice(
                audioDevices,
                DataFiles.SETTINGS_DATA_FILE.data.audioDeviceInfo
        )));
        playbackBufferSizeChoiceBox.setValue(DataFiles.SETTINGS_DATA_FILE.data.playbackBufferSize);

        colourScaleChoiceBox.setValue(
                ColourScale.values()[DataFiles.SETTINGS_DATA_FILE.data.colourScaleEnumOrdinal]
        );
        windowFunctionChoiceBox.setValue(
                SignalWindow.values()[DataFiles.SETTINGS_DATA_FILE.data.windowFunctionEnumOrdinal]
        );
        noteQuantizationChoiceBox.setValue(
                NoteUnit.values()[DataFiles.SETTINGS_DATA_FILE.data.noteQuantizationUnitEnumOrdinal]
        );

        // Set spinner factories and methods
        autosaveIntervalSpinnerFactory = new CustomIntegerSpinnerValueFactory(
                1, Integer.MAX_VALUE, DataFiles.SETTINGS_DATA_FILE.data.autosaveInterval, 1,
                "", " min"
        );
        autosaveIntervalSpinner.setValueFactory(autosaveIntervalSpinnerFactory);

        logFilePersistenceSpinnerFactory = new CustomIntegerSpinnerValueFactory(
                1, Integer.MAX_VALUE, DataFiles.SETTINGS_DATA_FILE.data.logFilePersistence, 1,
                "", " day(s)"
        );
        logFilePersistenceSpinner.setValueFactory(logFilePersistenceSpinnerFactory);

        // Set choice box methods
        themeChoiceBox.setOnAction(event -> AbstractViewController.updateActiveViewsThemes(themeChoiceBox.getValue()));
    }

    /**
     * Helper method that closes the settings view.
     */
    private void closeSettingsPage() {
        // Close the settings page
        ((Stage) rootPane.getScene().getWindow()).close();
        log("Closing settings page");

        // Reset themes back to original
        AbstractViewController.updateActiveViewsThemes(
                Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal]
        );
    }

    /**
     * Helper method that updates the settings file with the new settings.
     */
    private void applySettings() {
        // Update settings' values
        DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal = themeChoiceBox.getValue().ordinal();

        DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath = ffmpegPathTextField.getText();
        DataFiles.SETTINGS_DATA_FILE.data.audioDeviceInfo = audioDeviceChoiceBox.getValue().getMixerInfoAsMap();
        DataFiles.SETTINGS_DATA_FILE.data.playbackBufferSize = playbackBufferSizeChoiceBox.getValue();

        DataFiles.SETTINGS_DATA_FILE.data.autosaveInterval = autosaveIntervalSpinner.getValue();
        DataFiles.SETTINGS_DATA_FILE.data.logFilePersistence = logFilePersistenceSpinner.getValue();

        DataFiles.SETTINGS_DATA_FILE.data.colourScaleEnumOrdinal = colourScaleChoiceBox.getValue().ordinal();
        DataFiles.SETTINGS_DATA_FILE.data.windowFunctionEnumOrdinal = windowFunctionChoiceBox.getValue().ordinal();
        DataFiles.SETTINGS_DATA_FILE.data.noteQuantizationUnitEnumOrdinal =
                noteQuantizationChoiceBox.getValue().ordinal();

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
            default -> {  // "General" tab
                selectedTabName = "General";

                themeChoiceBox.setValue(Theme.values()[SettingsData.THEME_ENUM_ORDINAL]);
            }
            case 1 -> {  // "Audio" tab
                selectedTabName = "Audio";

                lastValidFFmpegPath = DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath;
                ffmpegPathTextField.setText(lastValidFFmpegPath);

                audioDeviceChoiceBox.setValue(new MixerInfoDisplay(Audio.getOutputAudioDevice(
                        audioDevices, SettingsData.AUDIO_DEVICE_INFO
                )));
                playbackBufferSizeChoiceBox.setValue(SettingsData.PLAYBACK_BUFFER_SIZE);
            }
            case 2 -> {  // "Input/Output" tab
                selectedTabName = "Input/Output";

                autosaveIntervalSpinnerFactory.setValue(SettingsData.AUTOSAVE_INTERVAL);
                logFilePersistenceSpinnerFactory.setValue(SettingsData.LOG_FILE_PERSISTENCE);
            }
            case 3 -> {  // "Transcription" tab
                selectedTabName = "Transcription";

                colourScaleChoiceBox.setValue(ColourScale.values()[SettingsData.COLOUR_SCALE_ENUM_ORDINAL]);
                windowFunctionChoiceBox.setValue(SignalWindow.values()[SettingsData.WINDOW_FUNCTION_ENUM_ORDINAL]);
                noteQuantizationChoiceBox.setValue(NoteUnit.values()[SettingsData.NOTE_QUANTIZATION_UNIT_ENUM_ORDINAL]);
            }
        }

        // Report that reset to defaults was successful
        String infoStr = "Reset '" + selectedTabName + "' tab settings to defaults";
        Popups.showInformationAlert(rootPane.getScene().getWindow(), "Reset to Defaults", infoStr + ".");
        log(infoStr);
    }

    // Helper classes
    @ExcludeFromGeneratedCoverageReport
    static class MixerInfoDisplay {
        private final Mixer.Info info;

        /**
         * Initializes a new <code>MixerInfoDisplay</code> instance.
         *
         * @param info Mixer info object.
         */
        public MixerInfoDisplay(Mixer.Info info) {
            this.info = info;
        }

        // Public methods

        /**
         * Method that gets the mixer info as a map for saving.
         *
         * @return A <code>Map</code>, with both key and value being strings.
         */
        public Map<String, String> getMixerInfoAsMap() {
            return Map.of(
                    "name", info.getName(),
                    "vendor", info.getVendor(),
                    "version", info.getVersion()
            );
        }

        @Override
        public String toString() {
            return info.getName();
        }
    }
}
