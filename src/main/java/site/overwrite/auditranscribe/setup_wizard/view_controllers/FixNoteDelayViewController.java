/*
 * FixNoteDelayViewController.java
 * Description: View controller that helps the user fix any note playback delays.
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

package site.overwrite.auditranscribe.setup_wizard.view_controllers;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.AudioProcessingMode;
import site.overwrite.auditranscribe.audio.exceptions.AudioTooLongException;
import site.overwrite.auditranscribe.generic.ClassWithLogging;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.misc.Popups;
import site.overwrite.auditranscribe.misc.Theme;
import site.overwrite.auditranscribe.misc.spinners.CustomDoubleSpinnerValueFactory;
import site.overwrite.auditranscribe.music.notes.MIDIInstrument;
import site.overwrite.auditranscribe.music.notes.NotePlayerSequencer;
import site.overwrite.auditranscribe.plotting.PlottingStuffHandler;
import site.overwrite.auditranscribe.utils.UnitConversionUtils;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * View controller that helps the user fix any note playback delays.
 */
public class FixNoteDelayViewController extends ClassWithLogging implements Initializable {
    // Constants
    private final double[] NOTE_ONSET_TIMES = {
            0.5, 0.75, 1, 1.25, 1.5, 3,
            3.5, 3.75, 4, 4.25, 4.5
    };
    private final double[] NOTE_DURATIONS = {
            0.25, 0.25, 0.25, 0.25, 1.5, 0.5,
            0.25, 0.25, 0.25, 0.25, 2
    };
    private final String[] NOTE_STRINGS = {
            "C#6", "B#5", "G#5", "E#5", "D#5", "B#4",
            "C#5", "B#4", "G#5", "F#5", "E#5"
    };
    private final MIDIInstrument INSTRUMENT = MIDIInstrument.PIANO;

    public static final double OFFSET_OF_OFFSET = 0.1;

    public static final double LINE_MISALIGNMENT_OFFSET_CONSTANT = 2.5;  // To correct weird line misalignment

    // Attributes
    private final DoubleProperty playheadX = new SimpleDoubleProperty(0);

    private boolean isPlaying = false;
    private double duration;

    private Audio audio;
    private NotePlayerSequencer sequencer;

    // FXML elements
    @FXML
    private AnchorPane rootPane, spectrogramPane;

    @FXML
    private Spinner<Double> notePlayingDelayOffsetSpinner;

    @FXML
    private Button togglePlaybackButton, setNotePlaybackDelayButton;

    // Initialization method
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get the width and height of the spectrogram
        double width = spectrogramPane.getPrefWidth();
        double height = spectrogramPane.getPrefHeight();

        // Create a note player sequencer for note playback
        setupNotePlayerSequencer();

        // Add playhead line to the spectrogram pane
        Line playheadLine = PlottingStuffHandler.createPlayheadLine(
                LINE_MISALIGNMENT_OFFSET_CONSTANT, height - LINE_MISALIGNMENT_OFFSET_CONSTANT
        );
        spectrogramPane.getChildren().add(playheadLine);

        // Bind properties
        playheadLine.startXProperty().bind(playheadX);
        playheadLine.endXProperty().bind(playheadX);

        // Set spinner factory and methods
        notePlayingDelayOffsetSpinner.setValueFactory(new CustomDoubleSpinnerValueFactory(
                -1, 1, 0, 0.01, 2
        ));

        // Add methods on buttons
        togglePlaybackButton.setOnAction((event) -> {
            // Update duration attribute
            // (We do it here so that audio is initialized first)
            duration = audio.getDuration();

            if (isPlaying) {
                // By pressing the button we want to stop the audio
                stopAudio();

                // Reset playhead line position
                playheadX.set(0);

                // Update text on the button
                togglePlaybackButton.setText("Play Test Audio");
            } else {
                // By pressing the button we want to play the audio
                playAudio();

                // Update text on the button
                togglePlaybackButton.setText("Stop Test Audio");
            }

            // Toggle the `isPlaying` flag
            isPlaying = !isPlaying;

            // Disable spinner if playing audio
            notePlayingDelayOffsetSpinner.setDisable(isPlaying);
        });

        setNotePlaybackDelayButton.setOnAction(event -> ((Stage) rootPane.getScene().getWindow()).close());

        // Set up scheduler to update the playhead line and detect the end of the audio
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0, runnable -> {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setDaemon(true);  // Make it so that it can shut down gracefully by placing it in background
            return thread;
        });
        scheduler.scheduleAtFixedRate(() -> {
            // Nothing really changes if the audio is not playing
            if (isPlaying) {
                // Get current audio time
                double currTime = audio.getCurrAudioTime();

                // Update playhead line position
                playheadX.set(currTime / duration * width);

                // Check if the current time has exceeded
                if (currTime >= duration) {
                    // Stop the audio
                    stopAudio();
                    Platform.runLater(() -> togglePlaybackButton.setText("Play Test Audio"));
                    isPlaying = false;
                }
            }
        }, 0, 50, TimeUnit.MILLISECONDS);

        log(Level.INFO, "Showing fix note delay view");
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
     * Method that sets the audio resource for the audio object.
     *
     * @param audioResourcePath <b>Absolute</b> path to the audio resource.
     */
    public void setAudioResource(String audioResourcePath) {
        // Create the audio object for playback
        try {
            audio = new Audio(new File(audioResourcePath), AudioProcessingMode.WITH_PLAYBACK);
        } catch (UnsupportedAudioFileException | IOException | AudioTooLongException e) {
            logException(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Method that gets the note playing delay offset set by the user and returns it.<br>
     * Note that there is a further offset applied to the returned value. This is to account for any
     * delays arising from the actual program having more 'stuff' to process than in the setup
     * wizard.
     *
     * @return A double, representing the note playing delay value that the user has set.
     */
    public double getNotePlayingDelayOffset() {
        return notePlayingDelayOffsetSpinner.getValue() + OFFSET_OF_OFFSET;
    }

    // Private methods

    /**
     * Helper method that sets up the note player sequencer.
     */
    private void setupNotePlayerSequencer() {
        // Convert notes to note numbers
        int[] noteNumbers = new int[NOTE_STRINGS.length];
        for (int i = 0; i < NOTE_STRINGS.length; i++) {
            noteNumbers[i] = UnitConversionUtils.noteToNoteNumber(NOTE_STRINGS[i]);
        }

        // Create the sequencer
        sequencer = new NotePlayerSequencer(0);

        // Check if the sequencer is available
        if (sequencer.isSequencerAvailable()) {
            // Set velocities
            sequencer.setOnVelocity(94);
            sequencer.setOffVelocity(64);

            // Set BPM
            sequencer.setBPM(60);

            // Set instrument
            sequencer.setInstrument(INSTRUMENT);

            // Set the notes
            sequencer.setNotesOnTrack(NOTE_ONSET_TIMES, NOTE_DURATIONS, noteNumbers, false);

            // Start & Stop the sequencer to correctly set timings
            sequencer.play(0, false);
            sequencer.stop();

        } else {
            Popups.showWarningAlert(
                    "Sequencer not available",
                    "The note player sequencer is not available on your system. This part of the setup " +
                            "wizard will not function. Simply skip to the next part."
            );
            log(Level.WARNING, "Note sequencer not available");
        }
    }

    /**
     * Helper method that starts the playback of the audio and the notes.
     */
    private void playAudio() {
        // Get the offset time
        double offsetTime = notePlayingDelayOffsetSpinner.getValue();

        // Reset the current time for the audio object and the note player sequencer
        audio.setAudioPlaybackTime(0);
        sequencer.setCurrTime(offsetTime);

        // Play the things
        audio.setPlaybackVolume(0.5);
        audio.play();
        sequencer.play(offsetTime, false);
    }

    /**
     * Helper method that stops the playback of the audio and the notes.
     */
    private void stopAudio() {
        audio.stop();
        audio.pause();
        sequencer.stop();
    }
}
