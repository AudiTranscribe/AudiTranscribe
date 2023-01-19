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
import app.auditranscribe.audio.exceptions.AudioTooLongException;
import app.auditranscribe.audio.exceptions.FFmpegNotFoundException;
import app.auditranscribe.fxml.IconHelper;
import app.auditranscribe.fxml.Popups;
import app.auditranscribe.fxml.views.main.scene_switching.SceneSwitchingData;
import app.auditranscribe.fxml.views.main.scene_switching.SceneSwitchingState;
import app.auditranscribe.generic.tuples.Pair;
import app.auditranscribe.io.audt_file.ProjectData;
import app.auditranscribe.io.audt_file.base.data_encapsulators.AudioDataObject;
import app.auditranscribe.io.audt_file.base.data_encapsulators.QTransformDataObject;
import app.auditranscribe.io.data_files.DataFiles;
import app.auditranscribe.misc.CustomTask;
import app.auditranscribe.misc.spinners.CustomDoubleSpinnerValueFactory;
import app.auditranscribe.music.BPMEstimator;
import app.auditranscribe.music.MusicKey;
import app.auditranscribe.music.MusicKeyEstimator;
import app.auditranscribe.music.TimeSignature;
import app.auditranscribe.plotting.ColourScale;
import app.auditranscribe.plotting.Spectrogram;
import app.auditranscribe.signal.windowing.SignalWindow;
import app.auditranscribe.system.OSMethods;
import app.auditranscribe.system.OSType;
import app.auditranscribe.utils.MathUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
    private Audio audio;
    private double audioDuration = 0;  // Will be updated upon scene initialization
    private double sampleRate;

    private double audioVolume = 0.5;  // Percentage
    private int notesVolume = 80;  // MIDI velocity
    private boolean isAudioMuted = false;
    private boolean areNotesMuted = false;

    private double finalWidth;
    private double finalHeight;

    private boolean debugMode = false;

    private String projectName;

    // FXML elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private MenuBar menuBar;

    // Top
    @FXML
    private Button audioVolumeButton, notesVolumeButton;

    @FXML
    private Slider audioVolumeSlider, notesVolumeSlider;

    @FXML
    private ChoiceBox<MusicKey> musicKeyChoice;

    @FXML
    private ChoiceBox<TimeSignature> timeSignatureChoice;

    @FXML
    private Spinner<Double> bpmSpinner, offsetSpinner;

    @FXML
    private HBox progressBarHBox;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label progressLabel;

    // Middle
    @FXML
    private ScrollPane leftScrollPane, spectrogramScrollPane, bottomScrollPane;

    @FXML
    private AnchorPane leftAnchorPane, spectrogramAnchorPane, bottomAnchorPane;

    @FXML
    private Pane notePane, barNumberPane, clickableProgressPane, colouredProgressPane;

    @FXML
    private ImageView spectrogramImage;

    // Bottom
    @FXML
    private Button scrollButton, editNotesButton, playButton, rewindToBeginningButton;

    @FXML
    private Label currTimeLabel, totalTimeLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Make macOS systems use the system menu bar
        if (OSMethods.getOS() == OSType.MAC) {
            menuBar.useSystemMenuBarProperty().set(true);
        }

        // Set spinners' factories
        bpmSpinner.setValueFactory(new CustomDoubleSpinnerValueFactory(
                BPM_RANGE.value0(), BPM_RANGE.value1(), 120, 0.1, 2
        ));
        offsetSpinner.setValueFactory(new CustomDoubleSpinnerValueFactory(
                OFFSET_RANGE.value0(), OFFSET_RANGE.value1(), 0, 0.01, 2
        ));

        // Set choice boxes' choices
        for (MusicKey musicKey : MusicKey.values()) musicKeyChoice.getItems().add(musicKey);
        for (TimeSignature signature : TimeSignature.values()) timeSignatureChoice.getItems().add(signature);

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
                rewindToBeginningButton, 20, IMAGE_BUTTON_LENGTH, "step-backward-solid"
        );
    }

    /**
     * Method that sets the audio and spectrogram data for the transcription view controller.<br>
     * This method uses the actual audio file to do the setting of the data.
     *
     * @param anAudio An <code>Audio</code> object that contains audio data.
     * @param data    Scene switching data that controls whether certain tasks will be executed (e.g.
     *                BPM estimation task).
     */
    public void setAudioAndSpectrogramData(Audio anAudio, SceneSwitchingData data) {
        // Set attributes
        audio = anAudio;
        audioDuration = anAudio.getDuration();
        sampleRate = anAudio.getSampleRate();

        projectName = data.projectName;

        // Generate spectrogram image based on audio
        CustomTask<WritableImage> spectrogramTask = new CustomTask<WritableImage>() {
            @Override
            protected WritableImage call() {
                // Todo somehow implement saving of magnitudes
                Spectrogram spectrogram = new Spectrogram(
                        audio, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER, BINS_PER_OCTAVE, SPECTROGRAM_HOP_LENGTH,
                        PX_PER_SECOND, NUM_PX_PER_OCTAVE, this
                );
                return spectrogram.generateSpectrogram(
                        SignalWindow.values()[DataFiles.SETTINGS_DATA_FILE.data.windowFunctionEnumOrdinal],
                        ColourScale.values()[DataFiles.SETTINGS_DATA_FILE.data.colourScaleEnumOrdinal]
                );
            }
        };

        // Create an estimation task to estimate both the BPM and the music key
        CustomTask<Pair<Double, String>> estimationTask = new CustomTask<>("Estimation Task") {
            @Override
            protected Pair<Double, String> call() {
                // First estimate BPM
                this.setMessage("Estimating BPM...");
                double bpm;
                if (sceneSwitchingData.estimateBPM) {
                    bpm = BPMEstimator.estimate(audio.getMonoSamples(), sampleRate).get(0);  // Take first element
                } else {
                    bpm = sceneSwitchingData.manualBPM;  // Use provided BPM
                }

                // Next estimate key
                this.setMessage("Estimating key...");
                String key;
                if (sceneSwitchingData.estimateMusicKey) {
                    // Get the top 4 most likely keys
                    List<Pair<MusicKey, Double>> mostLikelyKeys = MusicKeyEstimator.getMostLikelyKeysWithCorrelation(
                            audio.getMonoSamples(), sampleRate, 4, this
                    );

                    // Get most likely key and its correlation
                    Pair<MusicKey, Double> mostLikelyKeyPair = mostLikelyKeys.get(0);
                    MusicKey mostLikelyKey = mostLikelyKeyPair.value0();
                    double mostLikelyKeyCorr = mostLikelyKeyPair.value1();

                    // Get other likely keys
                    List<Pair<MusicKey, Double>> otherLikelyKeys = new ArrayList<>();
                    for (Pair<MusicKey, Double> pair : mostLikelyKeys.subList(1, 4)) {
                        if (pair.value1() >= 0.9 * mostLikelyKeyCorr) {
                            otherLikelyKeys.add(pair);
                        }
                    }

                    // Inform user if there are other likely keys
                    if (otherLikelyKeys.size() != 0) {
                        // Form the string to show user
                        StringBuilder sb = new StringBuilder();
                        for (Pair<MusicKey, Double> pair : otherLikelyKeys) {
                            sb.append(pair.value0().name).append(": ").append(MathUtils.round(pair.value1(), 3))
                                    .append("\n");
                        }

                        // Show alert
                        Platform.runLater(() -> Popups.showInformationAlert(
                                rootPane.getScene().getWindow(),
                                "Music Key Estimation Found Other Possible Keys",
                                "Most likely music key, with decreasing correlation:\n" +
                                        mostLikelyKey.name + ": " + MathUtils.round(mostLikelyKeyCorr, 3) + "\n" +
                                        sb + "\n" +
                                        "We will select " + mostLikelyKey.name + " as the key of the audio file."
                        ));
                    }

                    // Return the most likely key
                    key = mostLikelyKey.name;
                } else {
                    key = sceneSwitchingData.musicKeyString;
                }

                // Now return them both as a pair
                return new Pair<>(bpm, key);
            }
        };

        // Todo implement
//        // Set up tasks
//        setupSpectrogramTask(spectrogramTask, "Generating spectrogram...");
//        setupEstimationTask(estimationTask);
//
//        // Start the tasks
//        startTasks(spectrogramTask, estimationTask);
    }

    /**
     * Method that sets the audio and spectrogram data for the transcription view controller.<br>
     * This method uses existing data (provided in <code>qTransformData</code> and
     * <code>audioData</code>) to do the setting of the data.
     *
     * @param qTransformData The Q-Transform data that will be used to set the spectrogram data.
     * @param audioData      The audio data that will be used for audio playback.
     * @throws IOException                   If the audio file path that was provided in
     *                                       <code>audioData</code> points to a file that is invalid
     *                                       (or does not exist).
     * @throws FFmpegNotFoundException       If the FFmpeg binary could not be found.
     * @throws UnsupportedAudioFileException If the audio file path that was provided in
     *                                       <code>audioData</code> points to an invalid audio file.
     * @throws AudioTooLongException         If the audio file is too long.
     */
    public void setAudioAndSpectrogramData(
            QTransformDataObject qTransformData, AudioDataObject audioData
    ) throws IOException, FFmpegNotFoundException, UnsupportedAudioFileException, AudioTooLongException {
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
            spectrogramScrollPane.setHvalue(0);

        } else if (newPosX >= finalWidth - spectrogramAreaHalfWidth) {
            // If the `newPosX` is within the last 'half width' of the entire spectrogram area, keep the scrolling to
            // the end
            spectrogramScrollPane.setHvalue(1);
        } else {
            // Otherwise, update the H-value accordingly so that the view is centered on the playhead
            spectrogramScrollPane.setHvalue(
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
