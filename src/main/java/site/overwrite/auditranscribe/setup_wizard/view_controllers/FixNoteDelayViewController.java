/*
 * FixNoteDelayViewController.java
 *
 * Created on 2022-07-21
 * Updated on 2022-07-23
 *
 * Description: View controller that helps the user fix any note playback delays.
 */

package site.overwrite.auditranscribe.setup_wizard.view_controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.AudioProcessingMode;
import site.overwrite.auditranscribe.exceptions.audio.AudioTooLongException;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.misc.Theme;
import site.overwrite.auditranscribe.misc.spinners.CustomDoubleSpinnerValueFactory;
import site.overwrite.auditranscribe.plotting.PlottingStuffHandler;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * View controller that helps the user fix any note playback delays.
 */
public class FixNoteDelayViewController implements Initializable {
    // Constants
    private final String AUDIO_FILE = IOMethods.joinPaths("setup-wizard-files", "audio", "Breakfast.wav");

    // Attributes
    private boolean isPlaying = false;

    private Audio audio;
    private Line playheadLine;

    // FXML elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private Pane spectrogramPane;

    @FXML
    private Spinner<Double> notePlayingDelayOffsetSpinner;

    @FXML
    private Button togglePlaybackButton, setNotePlaybackDelayButton;

    // Initialization method
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Create the audio object for playback
        try {
            audio = new Audio(
                    new File(IOMethods.getAbsoluteFilePath(AUDIO_FILE)),
                    "Breakfast.wav",
                    AudioProcessingMode.PLAYBACK_ONLY
            );
        } catch (UnsupportedAudioFileException | IOException | AudioTooLongException e) {
            throw new RuntimeException(e);
        }

        // Add playhead line to the spectrogram pane
        playheadLine = PlottingStuffHandler.createPlayheadLine(spectrogramPane.getPrefHeight());  // Pref height should be accurate
        spectrogramPane.getChildren().add(playheadLine);

        // Set spinner factory and methods
        notePlayingDelayOffsetSpinner.setValueFactory(new CustomDoubleSpinnerValueFactory(
                -1, 1, 0.2, 0.01, 2
        ));

        // Add methods on buttons
        togglePlaybackButton.setOnAction((event) -> {
            // Todo: add
        });

        setNotePlaybackDelayButton.setOnAction(event -> ((Stage) rootPane.getScene().getWindow()).close());
    }

    // Public methods

    /**
     * Method that sets the scene's theme.
     *
     * @param theme Theme to set.
     */
    public void setThemeOnScene(Theme theme) {
        rootPane.getStylesheets().clear();  // Reset the stylesheets first before adding new ones

        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/base.css"));
        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/" + theme.cssFile));
    }

    /**
     * Method that gets the note playing delay offset set by the user and returns it.
     *
     * @return A double, representing the note playing delay value that the user has set.
     */
    public double getNotePlayingDelayOffset() {
        return notePlayingDelayOffsetSpinner.getValue();
    }
}
