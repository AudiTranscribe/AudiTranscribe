/*
 * TranscriptionViewController.java
 * Description: Controller for the transcription view.
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
import app.auditranscribe.fxml.IconHelper;
import app.auditranscribe.fxml.views.main.scene_switching.SceneSwitchingData;
import app.auditranscribe.fxml.views.main.scene_switching.SceneSwitchingState;
import app.auditranscribe.generic.tuples.Pair;
import app.auditranscribe.io.audt_file.ProjectData;
import app.auditranscribe.system.OSMethods;
import app.auditranscribe.system.OSType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * Controller for the transcription view.
 */
public class TranscriptionViewController extends SwitchableViewController {
    // Constants
    public static final Pair<Integer, Integer> BPM_RANGE = new Pair<>(1, 512);  // In the format [min, max]
    private final Pair<Double, Double> OFFSET_RANGE = new Pair<>(-5., 5.);  // In the format [min, max]

    public final double SPECTROGRAM_ZOOM_SCALE_X = 2;
    public final double SPECTROGRAM_ZOOM_SCALE_Y = 5;
    public final int PX_PER_SECOND = 120;
    private final int BINS_PER_OCTAVE = 60;
    private final int SPECTROGRAM_HOP_LENGTH = 1024;  // Needs to be a power of 2
    private final double NUM_PX_PER_OCTAVE = 72;
    private final int MIN_NOTE_NUMBER = 0;  // C0
    private final int MAX_NOTE_NUMBER = 107;  // B8

    private final double IMAGE_BUTTON_LENGTH = 50;  // In pixels

    // Attributes
    private double audioVolume = 0.5;  // Percentage
    private int notesVolume = 80;  // MIDI velocity
    private boolean isAudioMuted = false;
    private boolean areNotesMuted = false;

    private double finalWidth;
    private double finalHeight;

    private boolean debugMode = false;

    // FXML elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private MenuBar menuBar;

    // Top bar
    @FXML
    private Button audioVolumeButton, notesVolumeButton;

    @FXML
    private Slider audioVolumeSlider, notesVolumeSlider;
    // Todo add the rest

    // Middle
    @FXML
    private ScrollPane spectrogramPane;

    // Bottom bar
    @FXML
    private Button scrollButton, editNotesButton, playButton, playStepBackwardButton;  // Todo rename

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Make macOS systems use the system menu bar
        if (OSMethods.getOS() == OSType.MAC) {
            menuBar.useSystemMenuBarProperty().set(true);
        }

        // Set methods on the volume sliders
        audioVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Update the audio volume value
            audioVolume = newValue.doubleValue();

            // Change the icon of the audio volume button from mute to non-mute
            if (isAudioMuted) {
                IconHelper.setSVGOnButton(
                        audioVolumeButton, 20, IMAGE_BUTTON_LENGTH, "volume-up-solid"
                );
                isAudioMuted = false;
            }

            // Todo: update audio volume

            // Update CSS
            updateVolumeSliderCSS(audioVolumeSlider, audioVolume);

            log(Level.FINE, "Changed audio volume from " + oldValue + " to " + newValue);
        });

        notesVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Update the notes volume value
            notesVolume = newValue.intValue();

            // Change the icon of the notes' volume button from off to on
            if (areNotesMuted) {
                IconHelper.setSVGOnButton(
                        notesVolumeButton, 15, 20, IMAGE_BUTTON_LENGTH, IMAGE_BUTTON_LENGTH,
                        "music-note-solid"
                );
                areNotesMuted = false;
            }

            // Update CSS
            updateVolumeSliderCSS(notesVolumeSlider, (double) (notesVolume - 33) / 94);

            log(Level.FINE, "Changed notes volume from " + oldValue + " to " + newValue);
        });
    }

    // Getter/setter methods
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;

        if (debugMode) {
            log(Level.INFO, "Debug mode enabled");
        }
    }

    @Override
    public SceneSwitchingState getSceneSwitchingState() {
        if (sceneSwitchingState == null) return SceneSwitchingState.SHOW_MAIN_SCENE;
        return sceneSwitchingState;
    }

    // Public methods
    @Override
    public void finishSetup() {
        // Update sliders' initial CSS
        // todo: move this to after spectrogram setup finishes
        updateVolumeSliderCSS(audioVolumeSlider, audioVolume);
        updateVolumeSliderCSS(notesVolumeSlider, (double) (notesVolume - 33) / 94);
    }

    @Override
    public void setThemeOnScene() {
        updateThemeCSS(rootPane);

        // Set graphics
        IconHelper.setSVGOnButton(audioVolumeButton, 20, IMAGE_BUTTON_LENGTH, "volume-up-solid");
        IconHelper.setSVGOnButton(
                notesVolumeButton, 15, 20, IMAGE_BUTTON_LENGTH, IMAGE_BUTTON_LENGTH,
                "music-note-solid"
        );

        IconHelper.setSVGOnButton(
                scrollButton, 15, 22.5, IMAGE_BUTTON_LENGTH, IMAGE_BUTTON_LENGTH,
                "map-marker-line"
        );
        IconHelper.setSVGOnButton(editNotesButton, 20, IMAGE_BUTTON_LENGTH, "pencil-line");
        IconHelper.setSVGOnButton(playButton, 20, IMAGE_BUTTON_LENGTH, "play-solid");
        IconHelper.setSVGOnButton(
                playStepBackwardButton, 20, IMAGE_BUTTON_LENGTH, "step-backward-solid"
        );
    }

    // Todo add doc
    public void setAudioAndSpectrogramData(Audio audio, SceneSwitchingData data) {
        // Todo implement
    }

    // Todo add doc
    public void useExistingData(String audtFilePath, String audtFileName, ProjectData projectData) {
        // Todo implement
    }

    // Todo add doc
    public void handleSceneClosing() {
        // Todo implement
    }

    /**
     * Method that updates the scrolling of the page to center the playhead.
     *
     * @param newPosX New X position.
     * @param width   Width of the scrollable spectrogram pane.
     */
    public void updateScrollPosition(double newPosX, double width) {
        // Get the 'half width' of the spectrogram area
        double spectrogramAreaHalfWidth = width / 2;

        // Set the H-value of the spectrogram pane
        if (newPosX <= spectrogramAreaHalfWidth) {
            // If the `newPosX` is within the first 'half width' of the initial screen, do not scroll
            spectrogramPane.setHvalue(0);

        } else if (newPosX >= finalWidth - spectrogramAreaHalfWidth) {
            // If the `newPosX` is within the last 'half width' of the entire spectrogram area, keep the scrolling to
            // the end
            spectrogramPane.setHvalue(1);
        } else {
            // Otherwise, update the H-value accordingly so that the view is centered on the playhead
            spectrogramPane.setHvalue(
                    (newPosX - spectrogramAreaHalfWidth) / (finalWidth - 2 * spectrogramAreaHalfWidth)
            );
        }
    }

    // Private methods

    /**
     * Method that sets the volume slider's CSS.
     *
     * @param volumeSlider Volume slider that needs updating.
     * @param fillAmount   The amount of the volume slider that is filled. Must be a double between
     *                     0 and 1 inclusive.
     */
    private void updateVolumeSliderCSS(Slider volumeSlider, double fillAmount) {
        // Generate the style of the volume slider for the current volume value
        String style = String.format(
                "-fx-background-color: linear-gradient(" +
                        "to right, -slider-filled-colour %f%%, -slider-unfilled-colour %f%%" +
                        ");",
                fillAmount * 100, fillAmount * 100
        );

        // Apply the style to the volume slider's track (if available)
        StackPane track = (StackPane) volumeSlider.lookup(".track");
        if (track != null) track.setStyle(style);
    }
}
