/*
 * ProjectSetupViewController.java
 * Description: View controller that handles the project setup.
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

package app.auditranscribe.views.main.view_controllers;

import app.auditranscribe.generic.ClassWithLogging;
import app.auditranscribe.generic.tuples.Pair;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.data_files.DataFiles;
import app.auditranscribe.misc.Popups;
import app.auditranscribe.misc.Theme;
import app.auditranscribe.misc.spinners.CustomDoubleSpinnerValueFactory;
import app.auditranscribe.utils.MusicUtils;
import app.auditranscribe.views.main.ProjectIOHandlers;
import app.auditranscribe.views.main.scene_switching.SceneSwitchingData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class ProjectSetupViewController extends ClassWithLogging implements Initializable {
    // Attributes
    private File audioFile;

    final ToggleGroup bpmGroup = new ToggleGroup();
    final ToggleGroup musicKeyGroup = new ToggleGroup();

    public boolean shouldProceed = false;

    // FXML Elements
    @FXML
    private AnchorPane rootPane;

    // Project name section
    @FXML
    private TextField projectNameField;

    // Audio file section
    @FXML
    private Button selectAudioFileButton;

    @FXML
    private Label audioFileLabel;

    // BPM section
    @FXML
    private RadioButton bpmEstimateAutomatically, bpmSpecifyManually;

    @FXML
    private Spinner<Double> bpmManualSpinner;

    // Music Key section
    @FXML
    private RadioButton musicKeyEstimateAutomatically, musicKeySpecifyManually;

    @FXML
    private ChoiceBox<String> musicKeyChoiceBox;

    // Bottom section
    @FXML
    private Button cancelButton, createButton;

    // Initialization method
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Add radio buttons to groups
        bpmEstimateAutomatically.setToggleGroup(bpmGroup);
        bpmSpecifyManually.setToggleGroup(bpmGroup);

        musicKeyEstimateAutomatically.setToggleGroup(musicKeyGroup);
        musicKeySpecifyManually.setToggleGroup(musicKeyGroup);

        // Set button methods
        selectAudioFileButton.setOnAction((event) -> {
            handleSelectAudioFile();
            if (audioFile != null) {
                selectAudioFileButton.getStyleClass().remove("error");
            } else {
                selectAudioFileButton.getStyleClass().add("error");
            }
        });
        cancelButton.setOnAction((event) -> {
            shouldProceed = false;
            closeProjectSetupView();
        });
        createButton.setOnAction((event) -> {
            if (validateValues()) {
                shouldProceed = true;
                closeProjectSetupView();
            }
        });

        // Set text field methods
        projectNameField.textProperty().addListener((observable, oldVal, newVal) -> {
            if (newVal.length() != 0) {
                projectNameField.getStyleClass().remove("error");
            } else {
                projectNameField.getStyleClass().add("error");
            }
        });

        // Set spinner factories
        bpmManualSpinner.setValueFactory(new CustomDoubleSpinnerValueFactory(
                TranscriptionViewController.BPM_RANGE.value0(),
                TranscriptionViewController.BPM_RANGE.value1(),
                120,
                0.1,
                2
        ));

        // Add choice box selections
        for (String musicKey : MusicUtils.MUSIC_KEYS) musicKeyChoiceBox.getItems().add(musicKey);

        // Set radio button group methods
        bpmGroup.selectedToggleProperty().addListener((observable, oldVal, newVal) -> {
            if (bpmGroup.getSelectedToggle() != null) {
                // Disable the spinner if needed
                bpmManualSpinner.setDisable(bpmGroup.getSelectedToggle() == bpmEstimateAutomatically);
            }
        });

        musicKeyGroup.selectedToggleProperty().addListener((observable, oldVal, newVal) -> {
            if (musicKeyGroup.getSelectedToggle() != null) {
                // Disable the choice box if needed
                musicKeyChoiceBox.setDisable(musicKeyGroup.getSelectedToggle() == musicKeyEstimateAutomatically);
            }
        });

        // Final setup
        bpmManualSpinner.setDisable(true);  // Initially we select the automatic BPM estimation

        musicKeyChoiceBox.setDisable(true);  // Same with the music key choice box
        musicKeyChoiceBox.setValue("C Major");
    }

    // Getter/Setter methods

    /**
     * Gets the project name.
     */
    public String getProjectName() {
        return projectNameField.getText();
    }

    /**
     * Gets the BPM preference settings for the project.
     *
     * @return A pair.<br>
     * The first value specifies whether automatic BPM estimation should be done.<br>
     * The second value is the manual BPM value that was specified.
     */
    public Pair<Boolean, Double> getBPMPreference() {
        if (bpmGroup.getSelectedToggle() == bpmEstimateAutomatically) {
            return new Pair<>(true, -1.);  // -1 is used to signal `null`
        } else {
            return new Pair<>(false, bpmManualSpinner.getValue());
        }
    }

    /**
     * Gets the music key preference settings for the project.
     *
     * @return A pair.<br>
     * The first value specifies whether automatic music key estimation should be done.<br>
     * The second value is the manual music key that was specified.
     */
    public Pair<Boolean, String> getMusicKeyPreference() {
        if (musicKeyGroup.getSelectedToggle() == musicKeyEstimateAutomatically) {
            return new Pair<>(true, null);
        } else {
            return new Pair<>(false, musicKeyChoiceBox.getValue());
        }
    }

    // Public methods

    /**
     * Method that shows the project setup view.
     *
     * @return A pair. The first value indicates whether the setup should proceed. The second is the
     * scene switching data.
     */
    public static Pair<Boolean, SceneSwitchingData> showProjectSetupView() {
        try {
            // Load the FXML file into the scene
            FXMLLoader fxmlLoader = new FXMLLoader(
                    IOMethods.getFileURL("views/fxml/main/project-setup-view.fxml")
            );
            Scene scene = new Scene(fxmlLoader.load());

            // Get the view controller
            ProjectSetupViewController controller = fxmlLoader.getController();

            // Set the theme of the scene
            controller.setThemeOnScene();

            // Set stage properties
            Stage projectSetupStage = new Stage();
            projectSetupStage.initStyle(StageStyle.UTILITY);
            projectSetupStage.initModality(Modality.APPLICATION_MODAL);
            projectSetupStage.setTitle("Project Setup");
            projectSetupStage.setScene(scene);
            projectSetupStage.setResizable(false);

            // Show the stage
            projectSetupStage.showAndWait();

            // Set the scene switching data
            SceneSwitchingData data = new SceneSwitchingData();

            // Set data
            if (controller.shouldProceed) {
                // Obtain data from controller first
                Pair<Boolean, Double> bpmPair = controller.getBPMPreference();
                boolean shouldEstimateBPM = bpmPair.value0();
                double manualBPM = bpmPair.value1();

                Pair<Boolean, String> musicKeyPair = controller.getMusicKeyPreference();
                boolean shouldEstimateMusicKey = musicKeyPair.value0();
                String manualMusicKey = musicKeyPair.value1();

                // Then set on the scene switching data
                data.projectName = controller.getProjectName();
                data.file = controller.audioFile;

                data.isProjectSetup = true;

                data.estimateBPM = shouldEstimateBPM;
                data.manualBPM = manualBPM;

                data.estimateMusicKey = shouldEstimateMusicKey;
                data.musicKeyString = manualMusicKey;
            }

            // Return the formed data
            return new Pair<>(controller.shouldProceed, data);

        } catch (IOException e) {
            logException(e);
            throw new RuntimeException(e);
        }
    }

    // Private methods

    /**
     * Method that sets the theme for the scene.
     */
    private void setThemeOnScene() {
        // Get the theme
        Theme theme = Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal];

        // Set stylesheets
        rootPane.getStylesheets().clear();  // Reset the stylesheets first before adding new ones

        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/base.css"));
        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/" + theme.cssFile));
    }

    /**
     * Helper method that validates the values entered.
     *
     * @return A boolean: <code>true</code> if all values are valid and <code>false</code>
     * otherwise.
     */
    private boolean validateValues() {
        if (projectNameField.getText().length() == 0) {
            log(Level.INFO, "No project name specified");
            projectNameField.getStyleClass().add("error");
        }

        if (audioFile == null) {
            log(Level.INFO, "No audio file selected");
            selectAudioFileButton.getStyleClass().add("error");
        }

        return (projectNameField.getText().length() != 0 && audioFile != null);
    }

    /**
     * Helper method that handles the selection of an audio file.
     */
    private void handleSelectAudioFile() {
        // Get the current window
        Window window = rootPane.getScene().getWindow();

        // Get user to select an audio file
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Audio files (*.wav, *.mp3, *.flac, *.aif, *.aiff)",
                "*.wav", "*.mp3", "*.flac", "*.aif", "*.aiff"
        );
        File file = ProjectIOHandlers.openFileDialog(window, extFilter);

        // Verify that the user actually chose a file
        if (file == null) {
            Popups.showInformationAlert(rootPane.getScene().getWindow(), "Info", "No file selected.");
        } else {
            audioFile = file;
            audioFileLabel.setText(file.getName());
        }
    }

    /**
     * Helper method that closes the project setup view.
     */
    private void closeProjectSetupView() {
        ((Stage) rootPane.getScene().getWindow()).close();
    }
}
