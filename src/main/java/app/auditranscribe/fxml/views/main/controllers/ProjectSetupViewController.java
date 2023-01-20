/*
 * ProjectSetupViewController.java
 * Description: Controller for the project setup view.
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

import app.auditranscribe.fxml.Popups;
import app.auditranscribe.fxml.views.AbstractViewController;
import app.auditranscribe.fxml.views.main.ProjectHandler;
import app.auditranscribe.fxml.views.main.scene_switching.SceneSwitchingData;
import app.auditranscribe.generic.tuples.Pair;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.misc.spinners.CustomDoubleSpinnerValueFactory;
import app.auditranscribe.music.MusicKey;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the project setup view.
 */
public class ProjectSetupViewController extends AbstractViewController {
    // Attributes
    private File audioFile;

    final ToggleGroup bpmGroup = new ToggleGroup();
    final ToggleGroup musicKeyGroup = new ToggleGroup();

    public boolean shouldProceed = false;

    // FXML elements
    @FXML
    private AnchorPane rootPane;

    // Top section
    @FXML
    private TextField projectNameField;

    @FXML
    private Button selectAudioFileButton;

    @FXML
    private Label audioFileLabel;

    // BPM section
    @FXML
    private RadioButton bpmEstimateAutomatically, bpmSpecifyManually;

    @FXML
    private Spinner<Double> bpmManualSpinner;

    // Music key section
    @FXML
    private RadioButton musicKeyEstimateAutomatically, musicKeySpecifyManually;

    @FXML
    private ChoiceBox<MusicKey> musicKeyChoiceBox;

    // Bottom section
    @FXML
    private Button cancelButton, createButton;

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
        for (MusicKey musicKey : MusicKey.values()) musicKeyChoiceBox.getItems().add(musicKey);

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
        musicKeyChoiceBox.setValue(MusicKey.getMusicKey("C Major"));

        log("Project setup view ready to be shown");
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
            return new Pair<>(false, musicKeyChoiceBox.getValue().toString());
        }
    }


    // Public methods
    @Override
    public void setThemeOnScene() {
        updateThemeCSS(rootPane);
    }

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
                    IOMethods.getFileURL("fxml/views/main/project-setup-view.fxml")
            );
            Scene scene = new Scene(fxmlLoader.load());

            // Get the view controller
            ProjectSetupViewController controller = fxmlLoader.getController();

            // Set the theme of the scene
            controller.setThemeOnScene();

            // Set stage properties
            Stage projectSetupStage = new Stage();
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
     * Helper method that validates the values entered.
     *
     * @return A boolean. Returns <code>true</code> if all values are valid and <code>false</code>
     * otherwise.
     */
    private boolean validateValues() {
        if (projectNameField.getText().strip().length() == 0) {
            log("No project name specified");
            projectNameField.getStyleClass().add("error");
        }

        if (audioFile == null) {
            log("No audio file selected");
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
        File file = ProjectHandler.openFileDialog(window, extFilter);

        // Verify that the user actually chose a file
        if (file == null) {
            Popups.showInformationAlert(rootPane.getScene().getWindow(), "Info", "No file selected.");
        } else {
            String fileName = file.getName();

            audioFile = file;
            audioFileLabel.setText(fileName);

            // If the project name has not been set, we use the name of the file as a project name
            projectNameField.setText(fileName.substring(0, fileName.lastIndexOf('.')));
        }
    }

    /**
     * Helper method that closes the project setup view.
     */
    private void closeProjectSetupView() {
        ((Stage) rootPane.getScene().getWindow()).close();
    }
}
