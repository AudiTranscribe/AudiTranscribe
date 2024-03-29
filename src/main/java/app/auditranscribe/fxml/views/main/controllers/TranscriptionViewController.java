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
import app.auditranscribe.audio.FFmpeg;
import app.auditranscribe.fxml.IconHelper;
import app.auditranscribe.fxml.Popups;
import app.auditranscribe.fxml.Theme;
import app.auditranscribe.fxml.plotting.ColourScale;
import app.auditranscribe.fxml.plotting.PlottingHelper;
import app.auditranscribe.fxml.plotting.PlottingHelpers;
import app.auditranscribe.fxml.plotting.Spectrogram;
import app.auditranscribe.fxml.views.main.ProjectHandler;
import app.auditranscribe.fxml.views.main.SceneSwitcher;
import app.auditranscribe.generic.tuples.Pair;
import app.auditranscribe.generic.tuples.Quadruple;
import app.auditranscribe.io.IOConstants;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.audt_file.AUDTFileConstants;
import app.auditranscribe.io.audt_file.ProjectData;
import app.auditranscribe.io.audt_file.base.data_encapsulators.*;
import app.auditranscribe.io.audt_file.v0x000500.data_encapsulators.UnchangingDataPropertiesObject0x000500;
import app.auditranscribe.io.audt_file.v0x000B00.data_encapsulators.AudioDataObject0x000B00;
import app.auditranscribe.io.audt_file.v0x000B00.data_encapsulators.ProjectInfoDataObject0x000B00;
import app.auditranscribe.io.audt_file.v0x000C00.data_encapsulators.MusicNotesDataObject0x000C00;
import app.auditranscribe.io.data_files.DataFiles;
import app.auditranscribe.io.db.ProjectsDB;
import app.auditranscribe.misc.CustomTask;
import app.auditranscribe.fxml.spinners.CustomDoubleSpinnerValueFactory;
import app.auditranscribe.music.*;
import app.auditranscribe.music.playback.MIDIInstrument;
import app.auditranscribe.music.playback.NotePlayerSynth;
import app.auditranscribe.signal.windowing.SignalWindow;
import app.auditranscribe.system.OSMethods;
import app.auditranscribe.system.OSType;
import app.auditranscribe.utils.*;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Controller for the transcription view.
 */
public class TranscriptionViewController extends SwitchableViewController {
    // Constants
    public static final Pair<Double, Double> BPM_RANGE = new Pair<>(1., 512.);  // In the format [min, max]
    public static final Pair<Double, Double> OFFSET_RANGE = new Pair<>(-5., 5.);  // In the format [min, max]

    public static final MusicKey DEFAULT_MUSIC_KEY = MusicKey.C_MAJOR;
    public static final double DEFAULT_BPM = 120;
    public static final TimeSignature DEFAULT_TIME_SIGNATURE = TimeSignature.FOUR_FOUR;
    public static final double DEFAULT_OFFSET = 0;

    public final double SPECTROGRAM_ZOOM_SCALE_X = 2;
    public final double SPECTROGRAM_ZOOM_SCALE_Y = 5;
    public final int PX_PER_SECOND = 120;
    private final int BINS_PER_OCTAVE = 60;
    private final int SPECTROGRAM_HOP_LENGTH = 1024;  // Needs to be a power of 2
    private final double NUM_PX_PER_OCTAVE = 72;
    private final int MIN_NOTE_NUMBER = 0;  // C0
    private final int MAX_NOTE_NUMBER = 107;  // B8

    private final int BPM_ESTIMATION_MAX_DURATION = 10;  // In seconds
    private final int KEY_ESTIMATION_MAX_DURATION = 30;  // In seconds

    private final long UPDATE_PLAYBACK_SCHEDULER_PERIOD = 10;  // In milliseconds; todo: allow customisation

    private final boolean FANCY_NOTE_LABELS = true;  // Use fancy accidentals for note labels

    private final double IMAGE_BUTTON_LENGTH = 50;  // In pixels

    public final int NOTE_PLAYING_MIDI_CHANNEL_NUM = 0;
    private final MIDIInstrument NOTE_INSTRUMENT = MIDIInstrument.PIANO;
    private final int NOTE_ON_VELOCITY = 96;  // Within the range [0, 127]
    private final int NOTE_OFF_VELOCITY = 10;   // Within the range [0, 127]
    private final long NOTE_ON_DURATION = 75;  // In milliseconds
    private final long NOTE_OFF_DURATION = 925;  // In milliseconds

    public final double VOLUME_VALUE_DELTA_ON_KEY_PRESS = 0.05;  // Amount to change the volume by
    private final KeyCodeCombination SAVE_PROJECT_COMBINATION =
            new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN);
    private final KeyCodeCombination DEBUG_COMBINATION =
            new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);

    private final SignalWindow GENERATE_SPECTROGRAM_WINDOW = SignalWindow.HANN_WINDOW;

    // Attributes
    private int numSkippableBytes;

    private String audtFilePath;
    private String audtFileName;
    public int fileVersion;
    private ProjectsDB projectsDB;

    private Audio audio;
    private final DoubleProperty playheadX = new SimpleDoubleProperty(0);
    private double currTime = 0;
    private double audioDuration = 0;  // Will be updated upon scene initialization
    private double sampleRate;

    private Spectrogram spectrogram;

    private double finalWidth;
    private double finalHeight;

    private double audioVolume = 1;  // Percentage from 0 to 200%
    private int octaveNum = 4;  // Currently highlighted octave number
    private boolean audioMuted = false;

    private boolean scrollToPlayhead = false;

    private MusicKey musicKey = DEFAULT_MUSIC_KEY;
    private double bpm = DEFAULT_BPM;
    private TimeSignature timeSignature = DEFAULT_TIME_SIGNATURE;
    private int beatsPerBar = timeSignature.beatsPerBar;
    private double offset = DEFAULT_OFFSET;

    private boolean hasUnsavedChanges = true;
    private boolean changedProjectName = false;

    private boolean paused = true;
    private boolean usingSlowedAudio = false;

    private NotePlayerSynth notePlayerSynth;

    private boolean debugMode = false;
    private DebugViewController debugViewController = null;

    private boolean isEverythingReady = false;

    private String projectName;

    private CustomDoubleSpinnerValueFactory bpmSpinnerFactory, offsetSpinnerFactory;

    private Label[] noteLabels;
    private Line[] beatLines;
    private StackPane[] barNumberEllipses;
    private Line playheadLine;

    public Queue<CustomTask<?>> ongoingTasks = new LinkedList<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0, runnable -> {
        Thread thread = Executors.defaultThreadFactory().newThread(runnable);
        thread.setDaemon(true);  // Make it so that it can shut down gracefully by placing it in background
        return thread;
    });

    // FXML elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private MenuBar menuBar;

    @FXML
    private MenuItem newProjectMenuItem, openProjectMenuItem, renameProjectMenuItem, saveProjectMenuItem,
            saveAsMenuItem, settingsMenuItem, docsMenuItem, aboutMenuItem;

    @FXML
    private VBox mainVBox;

    // Top
    @FXML
    private Button audioVolumeButton;

    @FXML
    private Slider audioVolumeSlider;

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
    private Label progressLabel, freeMemoryLabel, maxMemoryLabel;

    // Middle
    @FXML
    private ScrollPane leftScrollPane, spectrogramScrollPane, bottomScrollPane;

    @FXML
    private AnchorPane leftAnchorPane, spectrogramAnchorPane, bottomAnchorPane;

    @FXML
    private Pane notePane, barNumberPane, clickableProgressPane;

    @FXML
    private Rectangle colouredProgressRectangle;

    @FXML
    private ImageView spectrogramImage;

    private Rectangle currentOctaveRectangle;

    // Bottom
    @FXML
    private Button scrollButton, playButton, rewindToBeginningButton, toggleSlowedAudioButton;

    @FXML
    private Label currTimeLabel, totalTimeLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Make macOS systems use the system menu bar
        if (OSMethods.getOS() == OSType.MAC) {
            menuBar.useSystemMenuBarProperty().set(true);
        }

        // Update note players
        try {
            notePlayerSynth = new NotePlayerSynth(NOTE_INSTRUMENT, NOTE_PLAYING_MIDI_CHANNEL_NUM);
        } catch (MidiUnavailableException ignored) {  // We will notify the user that MIDI unavailable later
        }

        // Set spinners' factories
        bpmSpinnerFactory = new CustomDoubleSpinnerValueFactory(
                BPM_RANGE.value0(), BPM_RANGE.value1(), 120, 0.1, 2
        );
        bpmSpinner.setValueFactory(bpmSpinnerFactory);

        offsetSpinnerFactory = new CustomDoubleSpinnerValueFactory(
                OFFSET_RANGE.value0(), OFFSET_RANGE.value1(), 0, 0.01, 2
        );
        offsetSpinner.setValueFactory(offsetSpinnerFactory);

        // Set choice boxes' choices
        for (MusicKey musicKey : MusicKey.values()) musicKeyChoice.getItems().add(musicKey);
        for (TimeSignature signature : TimeSignature.values()) timeSignatureChoice.getItems().add(signature);

        // Set methods on the volume sliders
        audioVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Update the audio volume value
            audioVolume = newValue.doubleValue();

            // Change the icon of the audio volume button if needed
            if (audioVolume == audioVolumeSlider.getMin()) {
                // Hacky way to set the mute icon
                audioMuted = false;
                toggleAudioMuteButton();
            } else if (audioMuted) {
                IconHelper.setSVGOnButton(
                        audioVolumeButton, 20, IMAGE_BUTTON_LENGTH, "volume-up-solid"
                );
                audioMuted = false;
            }

            // Update audio volume
            audio.setVolume(audioVolume);

            // Update CSS
            updateSliderCSS(audioVolumeSlider, audioVolume);

            log(Level.FINE, "Changed audio volume from " + oldValue + " to " + newValue);
        });

        // Set methods on spinners
        bpmSpinner.valueProperty().addListener(
                (observable, oldValue, newValue) -> updateBPMValue(newValue, false)
        );
        offsetSpinner.valueProperty().addListener(
                ((observable, oldValue, newValue) -> updateOffsetValue(newValue, false))
        );

        bpmSpinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {  // Lost focus
                updateBPMValue(bpmSpinner.getValue(), false);
            }
        });

        offsetSpinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {  // Lost focus
                updateOffsetValue(offsetSpinner.getValue(), false);
            }
        });

        // Set methods on choice box fields
        musicKeyChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(newValue, oldValue)) {
                updateMusicKeyValue(newValue, false);
            }
        });

        timeSignatureChoice.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    log(Level.FINE, "Changed time signature from " + oldValue + " to " + newValue);

                    // Update the `hasUnsavedChanges` flag
                    hasUnsavedChanges = true;

                    // Get the old and new beats per bar
                    int oldBeatsPerBar = 0;
                    if (oldValue != null) {
                        oldBeatsPerBar = oldValue.beatsPerBar;
                    }
                    int newBeatsPerBar = newValue.beatsPerBar;

                    // Update the beat lines and bar number ellipses, if the spectrogram is ready
                    if (isEverythingReady) {
                        beatLines = PlottingHelper.updateBeatLines(
                                spectrogramAnchorPane, beatLines, audioDuration, bpm, bpm, offset, offset,
                                finalHeight, oldBeatsPerBar, newBeatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
                        );

                        barNumberEllipses = PlottingHelper.updateBarNumberEllipses(
                                barNumberPane, barNumberEllipses, audioDuration, bpm, bpm, offset, offset,
                                barNumberPane.getPrefHeight(), oldBeatsPerBar, newBeatsPerBar, PX_PER_SECOND,
                                SPECTROGRAM_ZOOM_SCALE_X, spectrogramScrollPane.getWidth()
                        );
                    }

                    // Update the time signature index
                    timeSignature = newValue;

                    // Update the beats per bar
                    beatsPerBar = newBeatsPerBar;
                });

        // Add methods to buttons
        audioVolumeButton.setOnAction(event -> toggleAudioMuteButton());

        scrollButton.setOnAction(event -> toggleScrollButton());

        playButton.setOnAction(event -> togglePlayButton());
        rewindToBeginningButton.setOnAction(event -> {
            log(Level.FINE, "Pressed rewind to beginning button");

            paused = togglePaused(false);  // Pause the audio
            seekToTime(0);
            updateScrollPosition(0, finalWidth);
        });

        toggleSlowedAudioButton.setOnAction(event -> {
            // Update the text that is shown
            if (usingSlowedAudio) {
                // Now no longer using it
                toggleSlowedAudioButton.setText("1.0×");
            } else {
                // Starting to use it
                toggleSlowedAudioButton.setText("0.5×");
            }

            usingSlowedAudio = !usingSlowedAudio;
            audio.toggleSlowedAudio(usingSlowedAudio);

            log(
                    Level.FINE,
                    "Toggled audio slowdown; audio playback speed is now " + (usingSlowedAudio ? "0.5×" : "1.0×")
            );
        });

        // Add methods to menu items
        newProjectMenuItem.setOnAction(this::handleNewProject);
        openProjectMenuItem.setOnAction(this::handleOpenProject);
        renameProjectMenuItem.setOnAction(this::handleRenameProject);
        saveProjectMenuItem.setOnAction(event -> handleSavingProject(false, false));
        saveAsMenuItem.setOnAction(event -> handleSavingProject(false, true));
        settingsMenuItem.setOnAction(event -> SettingsViewController.showSettingsWindow());
        docsMenuItem.setOnAction(event -> GUIUtils.openURLInBrowser("https://docs.auditranscribe.app/"));
        aboutMenuItem.setOnAction(event -> AboutViewController.showAboutWindow());

        // Set spectrogram pane mouse event handler
        spectrogramAnchorPane.addEventHandler(MouseEvent.ANY, new EventHandler<>() {
            private boolean dragging = false;  // Whether the action is a drag action or not

            @Override
            public void handle(MouseEvent event) {
                if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    dragging = false;
                } else if (event.getEventType() == MouseEvent.DRAG_DETECTED) {
                    dragging = true;
                } else if (event.getEventType() == MouseEvent.MOUSE_CLICKED && !dragging && isEverythingReady) {
                    // Ensure that the click is within the pane
                    double clickX = event.getX();
                    double clickY = event.getY();

                    if (clickX >= spectrogramAnchorPane.getBoundsInParent().getMinX() &&
                            clickX <= spectrogramAnchorPane.getBoundsInParent().getMaxX() &&
                            clickY >= spectrogramAnchorPane.getBoundsInParent().getMinY() &&
                            clickY <= spectrogramAnchorPane.getBoundsInParent().getMaxY()
                    ) {
                        // Compute the frequency that the mouse click would correspond to
                        double estimatedFreq = PlottingHelpers.heightToFreq(
                                clickY,
                                UnitConversionUtils.noteNumberToFreq(MIN_NOTE_NUMBER),
                                UnitConversionUtils.noteNumberToFreq(MAX_NOTE_NUMBER),
                                spectrogramAnchorPane.getHeight()
                        );

                        // Now estimate the note number
                        int estimatedNoteNum = (int) Math.round(UnitConversionUtils.freqToNoteNumber(estimatedFreq));

                        // Play the note
                        log(
                                Level.FINE,
                                "Playing " + UnitConversionUtils.noteNumberToNote(
                                        estimatedNoteNum, musicKey, false
                                )
                        );
                        notePlayerSynth.playNoteForDuration(
                                estimatedNoteNum, NOTE_ON_VELOCITY, NOTE_OFF_VELOCITY, NOTE_ON_DURATION,
                                NOTE_OFF_DURATION
                        );
                    }
                }
            }
        });

        // Set clickable progress pane method
        clickableProgressPane.setOnMouseClicked(event -> {
            // Ensure that the click is within the pane
            double clickX = event.getX();
            double clickY = event.getY();

            if (clickX >= clickableProgressPane.getBoundsInParent().getMinX() &&
                    clickX <= clickableProgressPane.getBoundsInParent().getMaxX() &&
                    clickY >= clickableProgressPane.getBoundsInParent().getMinY() &&
                    clickY <= clickableProgressPane.getBoundsInParent().getMaxY()
            ) {
                // Convert the click position to seek time
                double seekTime = clickX / SPECTROGRAM_ZOOM_SCALE_X / PX_PER_SECOND;

                // Seek to that time
                seekToTime(seekTime);
            }
        });

        // Schedule available memory updating
        scheduler.scheduleAtFixedRate(() -> Platform.runLater(() -> {
            // Get the presumed free memory available
            // (See https://stackoverflow.com/a/12807848)
            long allocatedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;

            // Update the free memory label
            freeMemoryLabel.setText(MathUtils.round(presumableFreeMemory / 1e6, 2) + " MB");
        }), 1, 1, TimeUnit.SECONDS);

        // Set the maximum memory available
        long maxMemory = Runtime.getRuntime().maxMemory();
        maxMemoryLabel.setText(
                (maxMemory == Long.MAX_VALUE ? "∞" : MathUtils.round(maxMemory / 1e6, 2)) + " MB"
        );

        // Get the projects database
        try {
            projectsDB = new ProjectsDB();
        } catch (SQLException e) {
            logException(e);
            throw new RuntimeException(e);
        }
    }

    // Getter/setter methods
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;

        if (debugMode) {
            log("Debug mode enabled");
        }
    }

    @Override
    public SceneSwitcher.State getSceneSwitchingState() {
        if (state == null) return SceneSwitcher.State.SHOW_MAIN_SCENE;
        return state;
    }

    // Public methods
    @Override
    public void finishSetup() {
        // Set choices
        musicKeyChoice.setValue(musicKey);
        timeSignatureChoice.setValue(timeSignature);

        // Update spinners' initial values
        updateBPMValue(bpm, true);
        updateOffsetValue(offset, true);

        bpmSpinnerFactory.setValue(bpm);
        offsetSpinnerFactory.setValue(offset);

        // Update labels
        totalTimeLabel.setText(UnitConversionUtils.secondsToTimeString(audioDuration));

        // Set keyboard button press/release methods
        mainVBox.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::keyPressEventHandler);
        mainVBox.getScene().addEventFilter(KeyEvent.KEY_RELEASED, this::keyReleasedEventHandler);

        // Report that the transcription view is ready to be shown
        log("Transcription view ready to be shown");
    }

    @Override
    public void setThemeOnScene(Theme theme) {
        updateThemeCSS(rootPane, theme);
        setGraphics(theme);
    }

    /**
     * Method that sets the audio and spectrogram data for the transcription view controller.<br>
     * This method uses the actual audio file to do the setting of the data.
     *
     * @param anAudio An <code>Audio</code> object that contains audio data.
     * @param data    Scene switching data that controls whether certain tasks will be executed (e.g.
     *                BPM estimation task).
     */
    public void setAudioAndSpectrogramData(Audio anAudio, SceneSwitcher.Data data) {
        // Set attributes
        audio = anAudio;
        audioDuration = anAudio.getDuration();
        sampleRate = anAudio.getSampleRate();

        projectName = data.projectName;

        // Initialize the spectrogram
        spectrogram = new Spectrogram(
                audio, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER, BINS_PER_OCTAVE, SPECTROGRAM_HOP_LENGTH,
                PX_PER_SECOND, NUM_PX_PER_OCTAVE
        );

        // Generate spectrogram image based on audio
        CustomTask<WritableImage> spectrogramTask = new CustomTask<>("Generate Spectrogram") {
            @Override
            protected WritableImage call() {
                spectrogram.setTask(this);
                return spectrogram.generateSpectrogram(
                        GENERATE_SPECTROGRAM_WINDOW,
                        ColourScale.values()[DataFiles.SETTINGS_DATA_FILE.data.colourScaleEnumOrdinal]
                );
            }
        };

        // Create an estimation task to estimate both the BPM and the music key
        CustomTask<Quadruple<MusicKey, Double, TimeSignature, Double>> estimationTask = new CustomTask<>(
                "Estimate BPM and key"
        ) {
            @Override
            protected Quadruple<MusicKey, Double, TimeSignature, Double> call() {
                // First, estimate BPM (since it does not have a proper progress bar, per se)
                this.updateMessage("Estimating BPM...");
                double bpm;
                if (data.estimateBPM) {
                    bpm = BPMEstimator.estimate(
                            audio.getSmallSample(BPM_ESTIMATION_MAX_DURATION),
                            sampleRate
                    ).get(0);  // Take first element
                } else {
                    bpm = data.manualBPM;  // Use provided BPM
                }

                // Then, estimate music key (since this does have a progress bar)
                this.updateMessage("Estimating key...");
                MusicKey key;
                if (data.estimateMusicKey) {
                    // Get the top 4 most likely keys
                    List<Pair<MusicKey, Double>> mostLikelyKeys = MusicKeyEstimator.getMostLikelyKeysWithCorrelation(
                            audio.getSmallSample(KEY_ESTIMATION_MAX_DURATION), sampleRate, 4, this
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
                    if (!otherLikelyKeys.isEmpty()) {
                        // Form the string to show user
                        StringBuilder sb = new StringBuilder();
                        for (Pair<MusicKey, Double> pair : otherLikelyKeys) {
                            sb.append(pair.value0().name).append(": ").append(MathUtils.round(pair.value1(), 3))
                                    .append("\n");
                        }

                        // Show alert
                        Platform.runLater(() -> Popups.showInformationAlert(
                                rootPane.getScene().getWindow(),
                                StageStyle.DECORATED,
                                "Music Key Estimation Found Multiple Possible Keys",
                                "Most likely music keys, sorted by decreasing correlation:\n" +
                                        mostLikelyKey.name + ": " + MathUtils.round(mostLikelyKeyCorr, 3) + "\n" +
                                        sb + "\n" +
                                        "We will select " + mostLikelyKey.name + " as the music key."
                        ));
                    }

                    // Return the most likely key
                    key = mostLikelyKey;
                } else {
                    key = data.musicKey;
                }

                // Now, return all required data
                return new Quadruple<>(key, bpm, data.timeSignature, data.offset);
            }
        };

        // Set up tasks
        setupSpectrogramTask(spectrogramTask, "Generating spectrogram...");
        setupEstimationTask(estimationTask);

        // Start the tasks
        startTasks(spectrogramTask, estimationTask);
    }

    /**
     * Method that sets the audio and spectrogram data for the transcription view controller.<br>
     * This method uses existing data (provided in <code>qTransformData</code> and
     * <code>audioData</code>) to do the setting of the data.
     *
     * @param qTransformData The Q-Transform data that will be used to set the spectrogram data.
     * @param audioData      The audio data that will be used for audio playback.
     * @throws IOException                    If the audio file path that was provided in
     *                                        <code>audioData</code> points to a file that is
     *                                        invalid (or does not exist).
     * @throws FFmpeg.BinaryNotFoundException If the FFmpeg binary could not be found.
     * @throws UnsupportedAudioFileException  If the audio file path that was provided in
     *                                        <code>audioData</code> points to an invalid audio
     *                                        file.
     * @throws Audio.TooLongException         If the audio file is too long.
     */
    public void setAudioAndSpectrogramData(
            QTransformDataObject qTransformData, AudioDataObject audioData
    ) throws IOException, FFmpeg.BinaryNotFoundException, UnsupportedAudioFileException, Audio.TooLongException {
        // Set attributes
        sampleRate = audioData.sampleRate;
        audioDuration = audioData.totalDurationInMS / 1000.;

        // Initialize a spectrogram for plotting
        spectrogram = new Spectrogram(
                MIN_NOTE_NUMBER, MAX_NOTE_NUMBER, BINS_PER_OCTAVE, SPECTROGRAM_HOP_LENGTH, PX_PER_SECOND,
                NUM_PX_PER_OCTAVE, sampleRate, audioDuration
        );
        spectrogram.qTransformDataObject = qTransformData;

        // Ensure that the temporary directory exists
        IOMethods.createFolder(IOConstants.TEMP_FOLDER_PATH);
        log(Level.FINE, "Temporary folder created: " + IOConstants.TEMP_FOLDER_PATH);

        // Initialize the FFmpeg handler (if not done already)
        FFmpeg.initFFmpegHandler(DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath);

        // Handle the MP3 bytes
        File auxOriginalWAVFile = generateWAVFileFromMP3(audioData.mp3Bytes);

        // Create the `Audio` object
        audio = new Audio(auxOriginalWAVFile, Audio.ProcessingMode.WITH_PLAYBACK);

        // Update the raw MP3 bytes of the audio object
        // (This is to reduce the time needed to save the file later)
        audio.setMP3Bytes(audioData.mp3Bytes);

        // Generate spectrogram image based on existing magnitude data
        CustomTask<WritableImage> spectrogramTask = new CustomTask<>("Load Spectrogram") {
            @Override
            protected WritableImage call() {
                spectrogram.setTask(this);
                return spectrogram.generateSpectrogram(
                        ColourScale.values()[DataFiles.SETTINGS_DATA_FILE.data.colourScaleEnumOrdinal]
                );
            }
        };

        // Set up the spectrogram task
        setupSpectrogramTask(spectrogramTask, "Loading spectrogram...");

        // Start the tasks
        startTasks(spectrogramTask);
    }

    /**
     * Method that makes the transcription view controller use the existing project data, that was
     * supposedly read from a file.
     *
     * @param audtFilePath <b>Absolute</b> path to the file that contained the data.
     * @param audtFileName The name of the AUDT file.
     * @param projectData  The project data.
     */
    public void useExistingData(String audtFilePath, String audtFileName, ProjectData projectData) {
        // Get number of skippable bytes
        numSkippableBytes = projectData.unchangingDataProperties.numSkippableBytes;

        // Set up project data
        projectName = projectData.projectInfoData.projectName;
        musicKey = projectData.projectInfoData.musicKey;
        timeSignature = projectData.projectInfoData.timeSignature;
        bpm = projectData.projectInfoData.bpm;
        offset = projectData.projectInfoData.offsetSeconds;
        audioVolume = projectData.projectInfoData.playbackVolume;
        currTime = projectData.projectInfoData.currTimeInMS / 1000.;

        // Set the AudiTranscribe file's file path and file name
        this.audtFilePath = audtFilePath;
        this.audtFileName = audtFileName;

        // Set up Q-Transform data and audio data
        try {
            setAudioAndSpectrogramData(projectData.qTransformData, projectData.audioData);
        } catch (IOException | UnsupportedAudioFileException e) {
            logException(e);
            Popups.showExceptionAlert(
                    rootPane.getScene().getWindow(),
                    "Error loading audio data",
                    "An error occurred when loading the audio data. Does the audio file " +
                            "still exist at the original location?",
                    e
            );
        } catch (FFmpeg.BinaryNotFoundException e) {
            logException(e);
            Popups.showExceptionAlert(
                    rootPane.getScene().getWindow(),
                    "Error finding FFmpeg",
                    "FFmpeg was not found. Please install it and try again.",
                    e
            );
        } catch (Audio.TooLongException e) {
            logException(e);
            Popups.showExceptionAlert(
                    rootPane.getScene().getWindow(),
                    "Error loading audio data",
                    "The audio file is too long. Please select a shorter audio file.",
                    e
            );
        }

        // Update beats per bar
        beatsPerBar = timeSignature.beatsPerBar;

        // Attempt to insert this project to the projects' database
        try {
            if (projectsDB.checkIfProjectDoesNotExist(audtFilePath)) {
                projectsDB.insertProjectRecord(audtFilePath, projectName);
            }
        } catch (SQLException e) {
            logException(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Method that handles the closing of the transcription scene.
     */
    public void handleSceneClosing() {
        this.removeControllerFromActive();
        scheduler.shutdown();

        if (audio != null) {  // We need to check if `null` in the case where the FFmpeg handler failed to initialize
            audio.stop();
            audio.deleteWAVFile();
        }
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

    // Protected methods
    @Override
    protected void setGraphics(Theme theme) {
        // Set icons
        IconHelper.setSVGOnButton(audioVolumeButton, 20, IMAGE_BUTTON_LENGTH, "volume-up-solid");

        IconHelper.setSVGOnButton(
                scrollButton, 15, 22.5, IMAGE_BUTTON_LENGTH, IMAGE_BUTTON_LENGTH,
                "map-marker-line"
        );
        IconHelper.setSVGOnButton(playButton, 20, IMAGE_BUTTON_LENGTH, "play-solid");
        IconHelper.setSVGOnButton(
                rewindToBeginningButton, 20, IMAGE_BUTTON_LENGTH, "step-backward-solid"
        );
    }

    // Operation methods

    /**
     * Helper method that seeks to the specified time.
     *
     * @param seekTime Time to seek to.
     */
    private void seekToTime(double seekTime) {
        // Update the `hasUnsavedChanges` flag
        hasUnsavedChanges = true;

        // Ensure that the `seekTime` stays within range
        if (seekTime < 0 && currTime <= 0) return;  // Do nothing in this case
        else if (seekTime < 0) seekTime = 0;

        if (seekTime > audioDuration && currTime >= audioDuration) return;  // Do nothing in this case
        else if (seekTime > audioDuration) {
            seekTime = audioDuration;

            // Handle weird case where the audio should switch from paused to play for a second to prevent the
            // double-clicking of the pause button on the next iteration
            if (paused) paused = togglePaused(true);
        }

        // Make audio seek to that time
        seekTime = MathUtils.round(seekTime, 3);
        if (!paused) {
            // Hacky solution but it works
            togglePaused(false);
            audio.seekToTime(seekTime);
            togglePaused(true);
        } else {
            audio.seekToTime(seekTime);
        }

        // Update the current time and current time label
        currTime = seekTime;
        currTimeLabel.setText(UnitConversionUtils.secondsToTimeString(seekTime));

        // Update the playhead X position
        playheadX.set(seekTime * PX_PER_SECOND * SPECTROGRAM_ZOOM_SCALE_X);

        log(Level.FINE, "Seeked to " + seekTime + " seconds");
    }

    /**
     * Helper method that toggles the paused state.
     *
     * @param isPaused Old paused state.
     * @return New paused state.
     */
    private boolean togglePaused(boolean isPaused) {
        // Change the icon
        String iconToUse;
        if (isPaused) {  // Is currently paused; want to make audio play
            // Change the icon of the play button from the play icon to the paused icon
            // (So that the user knows that the next interaction with button will pause audio)
            iconToUse = "pause-solid";

            // Unpause the audio (i.e. play the audio)
            audio.play();
        } else {  // Is currently playing; want to make audio pause
            // Change the icon of the play button from the paused icon to the play icon
            // (So that the user knows that the next interaction with button will play audio)
            iconToUse = "play-solid";

            // Pause the audio
            audio.pause();
        }

        Platform.runLater(() -> IconHelper.setSVGOnButton(playButton, 20, IMAGE_BUTTON_LENGTH, iconToUse));

        // Toggle disabled state of the toggle slowdown button
        toggleSlowedAudioButton.setDisable(isPaused);  // If currently paused, will block

        // Return the toggled version of the `isPaused` flag
        log(Level.FINE, "Toggled pause state; now is " + (!isPaused ? "paused" : "playing"));
        return !isPaused;
    }

    // Updaters

    /**
     * Helper method that helps update the needed things when the BPM value is to be updated.
     *
     * @param newBPM      New BPM value.
     * @param forceUpdate Whether to force an update to the BPM value.
     */
    private void updateBPMValue(double newBPM, boolean forceUpdate) {
        // Check if the BPM value is valid first
        if (!(newBPM >= BPM_RANGE.value0() && newBPM <= BPM_RANGE.value1())) return;

        // Update the `hasUnsavedChanges` flag
        hasUnsavedChanges = true;

        // Get the previous BPM value
        double oldBPM = forceUpdate ? -1 : bpm;

        // These can only be called when the spectrogram is ready to be shown
        if (isEverythingReady) {
            // Update the beat lines
            beatLines = PlottingHelper.updateBeatLines(
                    spectrogramAnchorPane, beatLines, audioDuration, oldBPM, newBPM, offset, offset, finalHeight,
                    beatsPerBar, beatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
            );

            // Update the bar number ellipses
            barNumberEllipses = PlottingHelper.updateBarNumberEllipses(
                    barNumberPane, barNumberEllipses, audioDuration, oldBPM, newBPM, offset, offset,
                    bottomAnchorPane.getPrefHeight(), beatsPerBar, beatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X,
                    spectrogramScrollPane.getWidth()
            );
        }

        // Update the BPM value
        if (!forceUpdate) {
            log(Level.FINE, "Updated BPM value from " + bpm + " to " + newBPM);
        } else {
            log(Level.FINE, "Force update BPM value to " + newBPM);
        }

        bpm = newBPM;
    }

    /**
     * Helper method that helps update the needed things when the offset value is to be updated.
     *
     * @param newOffset   New offset value.
     * @param forceUpdate Whether to force an update to the offset value.
     */
    private void updateOffsetValue(double newOffset, boolean forceUpdate) {
        // Check if the new offset value is valid first
        if (!(newOffset >= OFFSET_RANGE.value0() && newOffset <= OFFSET_RANGE.value1())) return;

        // Update the `hasUnsavedChanges` flag
        hasUnsavedChanges = true;

        // Get the previous offset value
        double oldOffset = forceUpdate ? OFFSET_RANGE.value0() - 1 : offset;  // Make it 1 less than permitted

        // These can only be called when the spectrogram is ready to be shown
        if (isEverythingReady) {
            // Update the beat lines
            beatLines = PlottingHelper.updateBeatLines(
                    spectrogramAnchorPane, beatLines, audioDuration, bpm, bpm, oldOffset, newOffset, finalHeight,
                    beatsPerBar, beatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
            );

            // Update the bar number ellipses
            barNumberEllipses = PlottingHelper.updateBarNumberEllipses(
                    barNumberPane, barNumberEllipses, audioDuration, bpm, bpm, oldOffset, newOffset,
                    bottomAnchorPane.getPrefHeight(), beatsPerBar, beatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X,
                    spectrogramScrollPane.getWidth()
            );
        }

        // Update the offset value
        if (!forceUpdate) {
            log(Level.FINE, "Updated offset value from " + offset + " to " + newOffset);
        } else {
            log(Level.FINE, "Force update offset value to " + newOffset);
        }

        offset = newOffset;
    }

    /**
     * Helper method that updates the needed things when the music key value changes.
     *
     * @param newMusicKey New music key value.
     * @param forceUpdate Whether to force an update to the note labels.
     */
    private void updateMusicKeyValue(MusicKey newMusicKey, boolean forceUpdate) {
        // Update the `hasUnsavedChanges` flag
        hasUnsavedChanges = true;

        // Update note pane and note labels
        if (isEverythingReady || forceUpdate) {
            noteLabels = PlottingHelper.addNoteLabels(
                    notePane, noteLabels, newMusicKey, finalHeight, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER,
                    FANCY_NOTE_LABELS
            );
        }

        // Update the music key value and music key index
        if (!forceUpdate) {
            log(Level.FINE, "Changed music key from " + musicKey + " to " + newMusicKey);
        } else {
            log(Level.FINE, "Forced changed music key from " + musicKey + " to " + newMusicKey);
        }

        musicKey = newMusicKey;
    }

    /**
     * Method that sets the slider's CSS style.
     *
     * @param slider Slider that needs updating.
     * @param value  Value of the slider.
     */
    private void updateSliderCSS(Slider slider, double value) {
        // Compute fill amount
        double fillAmount = (value - slider.getMin()) / (slider.getMax() - slider.getMin());

        // Generate the style of the slider for the current value
        String style = String.format(
                "-fx-background-color: linear-gradient(" +
                        "to right, -slider-filled-colour %f%%, -slider-unfilled-colour %f%%" +
                        ");",
                fillAmount * 100, fillAmount * 100
        );

        // Apply the style to the slider's track (if available)
        StackPane track = (StackPane) slider.lookup(".track");
        if (track != null) track.setStyle(style);
    }

    // IO handlers


    /**
     * Helper method that helps open a new project.
     *
     * @param event Event that triggered this function.
     */
    private void handleNewProject(Event event) {
        // Do not do anything if we are not ready
        if (!isEverythingReady) return;

        // Pause the current audio
        paused = togglePaused(false);

        // Deal with possible unsaved changes
        boolean canCloseWindow = handleUnsavedChanges();
        if (canCloseWindow) {
            // Get the scene switching data
            Pair<Boolean, SceneSwitcher.Data> pair = ProjectSetupViewController.showProjectSetupView();
            boolean shouldProceed = pair.value0();
            data = pair.value1();

            // Specify the scene switching state
            if (shouldProceed) {
                // Signal the creation of a new project
                state = SceneSwitcher.State.NEW_PROJECT;

                // Close this stage
                closeTranscriptionView();
            }
        }
    }

    /**
     * Helper method that helps open an existing project.
     *
     * @param event Event that triggered this function.
     */
    private void handleOpenProject(Event event) {
        // Do not do anything if we are not ready
        if (!isEverythingReady) return;

        // Pause the current audio
        paused = togglePaused(false);

        // Deal with possible unsaved changes
        boolean canCloseWindow = handleUnsavedChanges();
        if (canCloseWindow) {
            // Get the current window
            Window window = rootPane.getScene().getWindow();

            // Get user to select an AUDT file
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                    "AudiTranscribe files (*.audt)", "*.audt"
            );
            File file = GUIUtils.openFileDialog(window, extFilter);

            // Check that the user selected a file
            if (file == null) {
                Popups.showInformationAlert(rootPane.getScene().getWindow(), "Info", "No file selected.");
            } else {
                // Set the scene switching status and the selected file
                state = SceneSwitcher.State.OPEN_PROJECT;
                data.file = file;

                // Close this stage
                closeTranscriptionView();
            }
        }
    }

    /**
     * Helper method that helps with the renaming of the current project.
     *
     * @param event Event that triggered this function.
     */
    private void handleRenameProject(Event event) {
        // Ask user for new project name
        Optional<String> newProjectNameResponse = Popups.showTextInputDialog(
                rootPane.getScene().getWindow(),
                "Rename Project",
                "Enter New Project Name",
                "New project name:",
                projectName
        );

        // Do nothing if nothing was entered, or if the project name was not changed
        if ((newProjectNameResponse.isEmpty()) || (newProjectNameResponse.get().equals(projectName))) return;

        // Otherwise, get new project name proper
        String newProjectName = newProjectNameResponse.get();

        // Change stage title
        ((Stage) rootPane.getScene().getWindow()).setTitle(newProjectName);

        // Update attributes
        log(Level.INFO, "Changed project name from '" + projectName + "' to '" + newProjectName + "'");

        projectName = newProjectName;
        changedProjectName = true;
        hasUnsavedChanges = true;
    }

    /**
     * Helper method that handles the saving of the project.
     *
     * @param isAutosave      Whether this is an autosave or not.
     * @param forceChooseFile Boolean whether to force the user to choose a file.
     */
    private void handleSavingProject(boolean isAutosave, boolean forceChooseFile) {
        // Do not do anything if we are not ready
        if (!isEverythingReady) return;

        // Get the save destination
        String saveDest = getSaveDestination(forceChooseFile);

        if (saveDest == null) {
            Popups.showInformationAlert(
                    rootPane.getScene().getWindow(), "Info", "No destination specified."
            );
            return;
        }

        // Set up task to run in alternate thread
        CustomTask<Void> task = new CustomTask<>("Save Project") {
            @Override
            protected Void call() throws Exception {
                saveData(forceChooseFile, saveDest);
                return null;
            }
        };

        // Link the progress of the task with the progress bar
        progressBarHBox.setVisible(true);
        progressBar.progressProperty().bind(task.progressProperty());
        progressLabel.setText("Saving file...");

        // Methods to run after task succeeded
        task.setOnSucceeded(event -> {
            // Handle database operations
            try {
                // Update the project file list
                if (projectsDB.checkIfProjectDoesNotExist(audtFilePath)) {
                    // Insert the record into the database
                    projectsDB.insertProjectRecord(audtFilePath, projectName);
                }

                // If changed project name, also update
                if (changedProjectName) {
                    projectsDB.updateProjectName(audtFilePath, projectName);
                    changedProjectName = false;  // Revert once complete
                }
            } catch (SQLException e) {
                logException(e);
                throw new RuntimeException(e);
            }

            // Hide the progress box
            progressBarHBox.setVisible(false);
            progressBar.progressProperty().unbind();

            // Show popup upon saving completion, if it is not an autosave
            if (!isAutosave) {
                Popups.showInformationAlert(
                        rootPane.getScene().getWindow(),
                        "Saved Successfully",
                        "Project was saved successfully."
                );
            }
        });

        // Start new thread to save the file
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Helper method that handles the unsaved changes.
     *
     * @return A boolean, <code>true</code> if the window should be closed, and <code>false</code>
     * if not.
     */
    private boolean handleUnsavedChanges() {
        // Do not do anything if we are not ready
        if (!isEverythingReady) return false;

        // Now check if there are unsaved changes
        if (hasUnsavedChanges) {
            // Prompt user to save work first
            ButtonType dontSaveButExit = new ButtonType("Don't Save");
            ButtonType dontSaveDontExit = new ButtonType("Cancel");
            ButtonType saveAndExit = new ButtonType("Save");

            Optional<ButtonType> selectedButton = Popups.showMultiButtonAlert(
                    rootPane.getScene().getWindow(),
                    "",
                    "",
                    "Save changes to project before leaving?",
                    dontSaveButExit, dontSaveDontExit, saveAndExit
            );

            if (selectedButton.isPresent()) {
                if (selectedButton.get() == saveAndExit) {
                    String saveDest = getSaveDestination(false);

                    if (saveDest != null) {
                        try {
                            saveData(false, saveDest);
                            return true;  // Can exit
                        } catch (IOException | FFmpeg.BinaryNotFoundException e) {
                            logException(e);
                            Popups.showExceptionAlert(
                                    rootPane.getScene().getWindow(),
                                    "File Saving Failure",
                                    "AudiTranscribe failed to save the file.",
                                    e
                            );
                            return false;  // Cannot exit
                        }
                    } else {
                        Popups.showInformationAlert(
                                rootPane.getScene().getWindow(),
                                "Info",
                                "No destination specified."
                        );
                        return false;  // No file selected; cannot exit
                    }

                } else if (selectedButton.get() == dontSaveButExit) {
                    return true;  // We just want to exit
                } else if (selectedButton.get() == dontSaveDontExit) {
                    return false;  // Don't want to exit
                } else {
                    return false;  // Assume default is don't want to leave
                }
            } else {
                return false;  // Assume don't want to save and don't want to exit
            }
        } else {
            return true;  // If there are no unsaved changes, then closing the window is permitted
        }
    }

    /**
     * Helper method that helps save the data into an AUDT file.
     *
     * @param forceChooseFile Whether the file was forcibly chosen.
     * @param saveDest        The destination to save the file to.
     * @throws FFmpeg.BinaryNotFoundException If the FFmpeg binary could not be found.
     * @throws IOException                    If the saving to the AUDT file failed.
     */
    private void saveData(
            boolean forceChooseFile, String saveDest
    ) throws FFmpeg.BinaryNotFoundException, IOException {
        // Package project data for saving
        log("Packaging data for saving");

        ProjectInfoDataObject projectInfoData = new ProjectInfoDataObject0x000B00(
                projectName, musicKey, timeSignature, bpm, offset, audioVolume,
                (int) (currTime * 1000)
        );
        MusicNotesDataObject musicNotesData = new MusicNotesDataObject0x000C00();

        // Determine what mode of the writer should be used
        if (numSkippableBytes == 0 || forceChooseFile || fileVersion != AUDTFileConstants.FILE_VERSION_NUMBER) {
            // Obtain the MP3 bytes
            byte[] mp3Bytes;
            try {
                mp3Bytes = audio.wavBytesToMP3Bytes(DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath);
            } catch (IOException e) {
                logException(e);
                throw new RuntimeException(e);
            }

            // Package audio data for saving
            AudioDataObject audioData = new AudioDataObject0x000B00(mp3Bytes, sampleRate, (int) (audioDuration * 1000));

            // Calculate the number of skippable bytes
            numSkippableBytes = 32 +  // Header section
                    UnchangingDataPropertiesObject.NUM_BYTES_NEEDED +
                    spectrogram.qTransformDataObject.numBytesNeeded() +
                    audioData.numBytesNeeded();

            // Update the unchanging data properties
            UnchangingDataPropertiesObject unchangingDataProperties = new UnchangingDataPropertiesObject0x000500(
                    numSkippableBytes
            );

            // Package all the current data into a `ProjectData`
            ProjectData projectData = new ProjectData(
                    unchangingDataProperties, spectrogram.qTransformDataObject, audioData, projectInfoData,
                    musicNotesData
            );

            // Save the project
            ProjectHandler.saveProject(saveDest, projectData);

            // Update file version number
            if (!forceChooseFile) fileVersion = AUDTFileConstants.FILE_VERSION_NUMBER;
        } else {
            ProjectHandler.saveProject(saveDest, numSkippableBytes, projectInfoData, musicNotesData);
        }

        // Set flags
        hasUnsavedChanges = false;
        log("File saved successfully");
    }

    // Task handlers

    /**
     * Helper method that sets up the spectrogram generation task.
     *
     * @param task    The spectrogram task.
     * @param message Message to display at the side of the progress bar.
     */
    private void setupSpectrogramTask(CustomTask<WritableImage> task, String message) {
        // Set the task's message
        task.updateMessage(message);

        // Set task completion listener
        task.setOnSucceeded(event -> {
            // Define a variable for the image
            WritableImage image = task.getValue();

            // Get the final width and height
            finalWidth = image.getWidth() * SPECTROGRAM_ZOOM_SCALE_X;
            finalHeight = image.getHeight() * SPECTROGRAM_ZOOM_SCALE_Y;

            // Fix panes' properties
            leftScrollPane.setFitToWidth(true);
            leftAnchorPane.setPrefHeight(finalHeight);

            spectrogramAnchorPane.setPrefWidth(finalWidth);
            spectrogramAnchorPane.setPrefHeight(finalHeight);

            bottomScrollPane.setFitToHeight(true);
            bottomAnchorPane.setPrefWidth(finalWidth);

            clickableProgressPane.setPrefWidth(finalWidth);

            // Set scrolling for panes
            leftScrollPane.vvalueProperty().bindBidirectional(spectrogramScrollPane.vvalueProperty());
            bottomScrollPane.hvalueProperty().bindBidirectional(spectrogramScrollPane.hvalueProperty());

            // Add the playhead line
            playheadLine = PlottingHelper.createPlayheadLine(finalHeight);
            spectrogramAnchorPane.getChildren().add(playheadLine);

            // Bind properties
            colouredProgressRectangle.widthProperty().bind(playheadX);
            playheadLine.startXProperty().bind(playheadX);
            playheadLine.endXProperty().bind(playheadX);

            // Schedule playback functionality
            scheduler.scheduleAtFixedRate(() -> {
                // Nothing really changes if the audio is paused
                if (!paused) {
                    // Get the current audio time
                    currTime = audio.getCurrentTime();

                    // Update the current time label
                    Platform.runLater(() -> currTimeLabel.setText(UnitConversionUtils.secondsToTimeString(currTime)));

                    // Update the playhead X position
                    playheadX.set(currTime * PX_PER_SECOND * SPECTROGRAM_ZOOM_SCALE_X);

                    // Check if the current time has exceeded and is not paused
                    if (currTime >= audioDuration) {
                        log(Level.FINE, "Playback reached end of audio, will start from beginning upon play");

                        // Pause the audio
                        paused = togglePaused(false);

                        // Reset the audio to the start
                        audio.stop();
                        audio.resetPlaybackSystem();
                    }

                    // Update scrolling
                    if (scrollToPlayhead) {
                        updateScrollPosition(playheadX.doubleValue(), spectrogramScrollPane.getWidth());
                    }
                }
            }, 0, UPDATE_PLAYBACK_SCHEDULER_PERIOD, TimeUnit.MILLISECONDS);

            // Schedule debug view updating
            if (debugMode) {
                scheduler.scheduleAtFixedRate(() -> {
                    if (debugViewController != null) {
                        debugViewController.setListContent(getDebugInfo());
                    }
                }, 0, UPDATE_PLAYBACK_SCHEDULER_PERIOD, TimeUnit.MILLISECONDS);
            }

            // Schedule autosave functionality
            scheduler.scheduleAtFixedRate(() -> Platform.runLater(() -> {
                        if (audtFilePath != null) {
                            handleSavingProject(true, false);
                            log("Autosave project successful");
                        } else {
                            log("Autosave skipped since project was not loaded from file");
                        }
                    }),
                    DataFiles.SETTINGS_DATA_FILE.data.autosaveInterval,
                    DataFiles.SETTINGS_DATA_FILE.data.autosaveInterval,
                    TimeUnit.MINUTES
            );

            // Set image on the spectrogram area
            spectrogramImage.setFitHeight(finalWidth);
            spectrogramImage.setFitWidth(finalHeight);
            spectrogramImage.setImage(image);

            // Set the current octave rectangle
            currentOctaveRectangle = PlottingHelper.addCurrentOctaveRectangle(
                    notePane, finalHeight, octaveNum, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER
            );

            // Add note labels and note lines
            noteLabels = PlottingHelper.addNoteLabels(
                    notePane, noteLabels, musicKey, finalHeight, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER,
                    FANCY_NOTE_LABELS
            );
            PlottingHelper.addNoteLines(spectrogramAnchorPane, finalHeight, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER);

            // Add the beat lines and bar number ellipses
            beatLines = PlottingHelper.getBeatLines(
                    bpm, beatsPerBar, PX_PER_SECOND, finalHeight, audioDuration, offset, SPECTROGRAM_ZOOM_SCALE_X
            );
            PlottingHelper.addBeatLines(spectrogramAnchorPane, beatLines);

            barNumberEllipses = PlottingHelper.getBarNumberEllipses(
                    bpm, beatsPerBar, PX_PER_SECOND, bottomAnchorPane.getPrefHeight(), audioDuration, offset,
                    SPECTROGRAM_ZOOM_SCALE_X
            );
            PlottingHelper.addBarNumberEllipses(barNumberPane, barNumberEllipses);

            // Resize spectrogram image pane
            // (We do this at the end to ensure that the image is properly placed)
            spectrogramImage.setFitWidth(finalWidth);
            spectrogramImage.setFitHeight(finalHeight);

            // Resize spectrogram pane
            spectrogramScrollPane.setPrefWidth(finalWidth);
            spectrogramScrollPane.setPrefHeight(finalHeight);

            // Settle layout of the main pane
            mainVBox.layout();

            // Show the spectrogram from the middle
            spectrogramScrollPane.setVvalue(0.5);

            // Update volume slider
            audioVolumeSlider.setValue(audioVolume);
            updateSliderCSS(audioVolumeSlider, audioVolume);

            // Ensure main pane is in focus
            rootPane.requestFocus();

            // Mark the task as completed and report that the transcription view is ready to be shown
            markTaskAsCompleted(task);
            log("Spectrogram for '" + projectName + "' ready to be shown");
        });
    }

    /**
     * Helper method that sets up the estimation task.
     *
     * @param task The estimation task.
     */
    private void setupEstimationTask(CustomTask<Quadruple<MusicKey, Double, TimeSignature, Double>> task) {
        // Set task completion listener
        task.setOnSucceeded(event -> {
            // Get values
            Quadruple<MusicKey, Double, TimeSignature, Double> returnedPair = task.getValue();
            MusicKey newMusicKey = returnedPair.value0();
            double newBPM = MathUtils.round(returnedPair.value1(), 1);
            TimeSignature newTimeSignature = returnedPair.value2();
            double newOffset = returnedPair.value3();

            // Update values
            updateMusicKeyValue(newMusicKey, true);
            musicKeyChoice.setValue(newMusicKey);

            updateBPMValue(newBPM, true);
            bpmSpinnerFactory.setValue(newBPM);

            timeSignatureChoice.setValue(newTimeSignature);

            updateOffsetValue(newOffset, true);
            offsetSpinnerFactory.setValue(newOffset);

            // Mark the task as completed
            markTaskAsCompleted(task);
            log("Estimation task complete");
        });
    }

    /**
     * Helper method that starts all the transcription view tasks.
     *
     * @param tasks The tasks to start.
     */
    private void startTasks(CustomTask<?>... tasks) {
        // Create a task that starts the tasks
        CustomTask<Boolean> masterTask = new CustomTask<>() {
            @Override
            protected Boolean call() {
                // Convert the array of tasks into a list of tasks
                Collection<CustomTask<?>> taskList = List.of(tasks);

                taskList.forEach(task -> task.setOnFailed(event -> {
                    // Log the error
                    log(Level.SEVERE, "Task '" + task.name + "' failed.");
                    if (task.getException() instanceof Exception) {
                        logException((Exception) task.getException());
                    } else {
                        log(Level.SEVERE, task.getException().getMessage());
                    }

                    // Determine the header and content text to show
                    String headerText = "An Error Occurred";
                    String contentText = "Task \"" + task.name + "\" failed.";

                    if (task.getException() instanceof OutOfMemoryError) {
                        headerText = "Out Of Memory";
                        contentText = "Task \"" + task.name + "\" failed due to running out of memory.";
                    }

                    // Show error dialog
                    Popups.showExceptionAlert(
                            rootPane.getScene().getWindow(), headerText, contentText, task.getException()
                    );

                    // Clear progress bar area
                    progressBarHBox.setVisible(false);
                    progressBar.progressProperty().unbind();
                    progressLabel.textProperty().unbind();
                }));

                // Add all tasks to the ongoing tasks queue
                ongoingTasks.addAll(taskList);

                // Update the progress bar section
                markTaskAsCompleted(null);

                // Execute tasks one-by-one
                for (CustomTask<?> task : taskList) {
                    // Create a new thread to start the task
                    Thread thread = new Thread(task);
                    thread.setDaemon(true);
                    thread.start();

                    log("Started task: '" + task.name + "'");

                    // Await for completion
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        logException(e);
                        throw new RuntimeException(e);
                    }
                }

                log("All tasks complete");
                return true;
            }
        };
        masterTask.setOnSucceeded(event -> {
            // Check if all tasks are completed
            if (masterTask.getValue()) {
                // Update the `isEverythingReady` flag
                isEverythingReady = true;

                // Update the BPM value
                updateBPMValue(bpm, true);

                // Enable all disabled nodes
                Node[] disabledNodes = new Node[]{
                        // Top Hbox
                        audioVolumeButton, audioVolumeSlider, musicKeyChoice, bpmSpinner, timeSignatureChoice,
                        offsetSpinner,

                        // Bottom Hbox
                        scrollButton, playButton, rewindToBeginningButton, toggleSlowedAudioButton
                };

                for (Node node : disabledNodes) node.setDisable(false);

                // Initialize the source data line for the audio
                audio.play();
                audio.pause();

                // Update playhead position
                seekToTime(currTime);
                updateScrollPosition(
                        currTime * PX_PER_SECOND * SPECTROGRAM_ZOOM_SCALE_X,
                        spectrogramScrollPane.getWidth()
                );

                // Handle attempt to close the window
                rootPane.getScene().getWindow().setOnCloseRequest((windowEvent) -> {
                    // Deal with possible unsaved changes
                    boolean canCloseWindow = handleUnsavedChanges();
                    if (!canCloseWindow) windowEvent.consume();
                });

                // If we are using existing data (i.e., AUDT file path was already set), then initially there are no
                // unsaved changes
                if (audtFilePath != null) hasUnsavedChanges = false;
            }
        });

        // Start the thread
        Thread masterThread = new Thread(masterTask);
        masterThread.setDaemon(true);
        masterThread.start();
    }

    /**
     * Helper method that marks a task as completed and handles the ongoing tasks list.
     *
     * @param completedTask The completed task.
     */
    private void markTaskAsCompleted(CustomTask<?> completedTask) {
        // Get the current task that is being processed
        CustomTask<?> currentTask = ongoingTasks.peek();

        // Remove the completed task from the ongoing tasks queue
        if (completedTask != null) ongoingTasks.remove(completedTask);

        // Update the progress bar section if the completed task is the current task or if the current task is `null`
        if (completedTask == null || currentTask == completedTask) {
            // Check if there are any tasks left in the queue
            if (!ongoingTasks.isEmpty()) {
                // Get the next task in the queue
                currentTask = ongoingTasks.peek();

                // Update the progress section
                progressBar.progressProperty().bind(currentTask.progressProperty());
                progressLabel.textProperty().bind(currentTask.messageProperty());
            } else {
                progressBarHBox.setVisible(false);
                progressBar.progressProperty().unbind();
                progressLabel.textProperty().unbind();
            }
        }
    }

    // Button handlers

    /**
     * Helper method that toggles the play button.
     */
    private void togglePlayButton() {
        // Update the `hasUnsavedChanges` flag
        hasUnsavedChanges = true;

        // Toggle audio paused state
        if (currTime == audioDuration) {
            audio.seekToTime(0);
            currTime = 0;
        }
        paused = togglePaused(paused);

        log(Level.FINE, "User toggled play button; audio is now " + (paused ? "paused" : "playing"));
    }

    /**
     * Helper method that toggles the scroll button.
     */
    private void toggleScrollButton() {
        // Change the icon
        String iconToUse = "map-marker-solid";
        if (scrollToPlayhead) iconToUse = "map-marker-line";  // Want to change from filled to non-filled
        IconHelper.setSVGOnButton(
                scrollButton, 15, 22.5, IMAGE_BUTTON_LENGTH, IMAGE_BUTTON_LENGTH, iconToUse
        );

        // Toggle the `scrollToPlayhead` flag
        scrollToPlayhead = !scrollToPlayhead;

        log(Level.FINE, "Toggled scroll (scroll is now " + scrollToPlayhead + ")");
    }

    /**
     * Helper method that toggles the audio mute button.
     */
    private void toggleAudioMuteButton() {
        // Determine icon to use
        String iconToUse;

        if (audioMuted) {
            // Want to change from mute to non-mute
            iconToUse = "volume-up-solid";

            // Unmute the audio by setting the volume back to the value before the mute
            audio.setVolume(audioVolume);
        } else {
            // Want to change from non-mute to mute
            iconToUse = "volume-mute-solid";

            // Mute the audio by setting the volume to zero
            audio.setVolume(0);
        }

        // Change the icon
        IconHelper.setSVGOnButton(audioVolumeButton, 20, IMAGE_BUTTON_LENGTH, iconToUse);

        // Toggle the `isAudioMuted` flag
        audioMuted = !audioMuted;

        log(Level.FINE, "Toggled audio mute button (audio muted is now " + audioMuted + ")");
    }

    // Keyboard event handlers

    /**
     * Helper method that handles a keyboard key press event.
     *
     * @param keyEvent Key press event.
     */
    private void keyPressEventHandler(KeyEvent keyEvent) {
        // If the spectrogram is not ready, do not do anything
        if (!isEverythingReady) {
            keyEvent.consume();
            return;
        }

        // Get the key event's target
        Node target = (Node) keyEvent.getTarget();

        // Check if user is using any shortcuts
        if (SAVE_PROJECT_COMBINATION.match(keyEvent)) {  // Save current project
            handleSavingProject(false, false);
            keyEvent.consume();
            return;
        } else if (DEBUG_COMBINATION.match(keyEvent)) {  // Show debug view
            if (debugMode) debugViewController = DebugViewController.showDebugView(rootPane.getScene().getWindow());
            keyEvent.consume();
            return;
        }

        // Ignore if the target is a spinner
        if (target instanceof TextField || target instanceof Spinner) return;

        // Otherwise, get the key event's key code
        KeyCode code = keyEvent.getCode();

        // Handle key press input
        keyEvent.consume();

        switch (code) {
            // Non-note playing key press inputs
            case SPACE -> togglePlayButton();
            case UP -> audioVolumeSlider.setValue(audioVolumeSlider.getValue() + VOLUME_VALUE_DELTA_ON_KEY_PRESS);
            case DOWN -> audioVolumeSlider.setValue(audioVolumeSlider.getValue() - VOLUME_VALUE_DELTA_ON_KEY_PRESS);
            case M -> toggleAudioMuteButton();
            case LEFT -> seekToTime(currTime - 1);
            case RIGHT -> seekToTime(currTime + 1);
            case PERIOD -> toggleScrollButton();
            case MINUS -> {
                notePlayerSynth.silenceChannel();  // Stop any notes from playing
                if (octaveNum > 0) {
                    log(Level.FINE, "Playback octave raised to " + octaveNum);
                    PlottingHelper.updateCurrentOctaveRectangle(
                            currentOctaveRectangle, finalHeight, --octaveNum, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER
                    );
                }
            }
            case EQUALS -> {
                notePlayerSynth.silenceChannel();  // Stop any notes from playing
                if (octaveNum < 8) {
                    log(Level.FINE, "Playback octave lowered to " + octaveNum);
                    PlottingHelper.updateCurrentOctaveRectangle(
                            currentOctaveRectangle, finalHeight, ++octaveNum, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER
                    );
                }
            }

            // Note playing keyboard inputs
            case A -> notePlayerSynth.noteOn(octaveNum * 12, NOTE_ON_VELOCITY);               // C
            case W -> notePlayerSynth.noteOn(octaveNum * 12 + 1, NOTE_ON_VELOCITY);           // C#
            case S -> notePlayerSynth.noteOn(octaveNum * 12 + 2, NOTE_ON_VELOCITY);           // D
            case E -> notePlayerSynth.noteOn(octaveNum * 12 + 3, NOTE_ON_VELOCITY);           // D#
            case D -> notePlayerSynth.noteOn(octaveNum * 12 + 4, NOTE_ON_VELOCITY);           // E
            case F -> notePlayerSynth.noteOn(octaveNum * 12 + 5, NOTE_ON_VELOCITY);           // F
            case T -> notePlayerSynth.noteOn(octaveNum * 12 + 6, NOTE_ON_VELOCITY);           // F#
            case G -> notePlayerSynth.noteOn(octaveNum * 12 + 7, NOTE_ON_VELOCITY);           // G
            case Y -> notePlayerSynth.noteOn(octaveNum * 12 + 8, NOTE_ON_VELOCITY);           // G#
            case H -> notePlayerSynth.noteOn(octaveNum * 12 + 9, NOTE_ON_VELOCITY);           // A
            case U -> notePlayerSynth.noteOn(octaveNum * 12 + 10, NOTE_ON_VELOCITY);          // A#
            case J -> notePlayerSynth.noteOn(octaveNum * 12 + 11, NOTE_ON_VELOCITY);          // B
            case K -> notePlayerSynth.noteOn(octaveNum * 12 + 12, NOTE_ON_VELOCITY);          // C'
            case O -> notePlayerSynth.noteOn(octaveNum * 12 + 13, NOTE_ON_VELOCITY);          // C#'
            case L -> notePlayerSynth.noteOn(octaveNum * 12 + 14, NOTE_ON_VELOCITY);          // D'
            case P -> notePlayerSynth.noteOn(octaveNum * 12 + 15, NOTE_ON_VELOCITY);          // D#'
            case SEMICOLON -> notePlayerSynth.noteOn(octaveNum * 12 + 16, NOTE_ON_VELOCITY);  // E'
            case QUOTE -> notePlayerSynth.noteOn(octaveNum * 12 + 17, NOTE_ON_VELOCITY);      // F'
        }
    }

    /**
     * Helper method that handles a keyboard key released event.
     *
     * @param keyEvent Key released event.
     */
    private void keyReleasedEventHandler(KeyEvent keyEvent) {
        // If the spectrogram is not ready, do not do anything
        if (!isEverythingReady) {
            keyEvent.consume();
            return;
        }

        // If the key event is part of the save project combination, ignore
        if (SAVE_PROJECT_COMBINATION.match(keyEvent)) {
            keyEvent.consume();
            return;
        }

        // Handle key event
        KeyCode code = keyEvent.getCode();

        switch (code) {
            case A -> notePlayerSynth.noteOff(octaveNum * 12, NOTE_OFF_VELOCITY);               // C
            case W -> notePlayerSynth.noteOff(octaveNum * 12 + 1, NOTE_OFF_VELOCITY);           // C#
            case S -> notePlayerSynth.noteOff(octaveNum * 12 + 2, NOTE_OFF_VELOCITY);           // D
            case E -> notePlayerSynth.noteOff(octaveNum * 12 + 3, NOTE_OFF_VELOCITY);           // D#
            case D -> notePlayerSynth.noteOff(octaveNum * 12 + 4, NOTE_OFF_VELOCITY);           // E
            case F -> notePlayerSynth.noteOff(octaveNum * 12 + 5, NOTE_OFF_VELOCITY);           // F
            case T -> notePlayerSynth.noteOff(octaveNum * 12 + 6, NOTE_OFF_VELOCITY);           // F#
            case G -> notePlayerSynth.noteOff(octaveNum * 12 + 7, NOTE_OFF_VELOCITY);           // G
            case Y -> notePlayerSynth.noteOff(octaveNum * 12 + 8, NOTE_OFF_VELOCITY);           // G#
            case H -> notePlayerSynth.noteOff(octaveNum * 12 + 9, NOTE_OFF_VELOCITY);           // A
            case U -> notePlayerSynth.noteOff(octaveNum * 12 + 10, NOTE_OFF_VELOCITY);          // A#
            case J -> notePlayerSynth.noteOff(octaveNum * 12 + 11, NOTE_OFF_VELOCITY);          // B
            case K -> notePlayerSynth.noteOff(octaveNum * 12 + 12, NOTE_OFF_VELOCITY);          // C'
            case O -> notePlayerSynth.noteOff(octaveNum * 12 + 13, NOTE_OFF_VELOCITY);          // C#'
            case L -> notePlayerSynth.noteOff(octaveNum * 12 + 14, NOTE_OFF_VELOCITY);          // D'
            case P -> notePlayerSynth.noteOff(octaveNum * 12 + 15, NOTE_OFF_VELOCITY);          // D#'
            case SEMICOLON -> notePlayerSynth.noteOff(octaveNum * 12 + 16, NOTE_OFF_VELOCITY);  // E'
            case QUOTE -> notePlayerSynth.noteOff(octaveNum * 12 + 17, NOTE_OFF_VELOCITY);      // F'
        }
    }

    // Miscellaneous methods

    /**
     * Helper method that gets the save destination.
     *
     * @param forceChooseFile Whether the save destination must be forcibly chosen.
     * @return String representing the save destination. Returns <code>null</code> if no destination
     * was specified.
     */
    private String getSaveDestination(boolean forceChooseFile) {
        String saveDest, saveName;

        // Check if there already exist a place to save
        if (audtFilePath == null || forceChooseFile) {
            log(Level.FINE, "AUDT file destination not yet set; asking now");

            // Get current window
            Window window = rootPane.getScene().getWindow();

            // Ask user to choose a file
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                    "AudiTranscribe File (*.audt)", "*.audt"
            );
            File file = GUIUtils.saveFileDialog(window, projectName, extFilter);

            // If operation was cancelled return
            if (file == null) return null;

            // Set the actual destination to save the file
            saveDest = file.getAbsolutePath();
            saveName = file.getName();

            if (!saveDest.toLowerCase().endsWith(".audt")) saveDest += ".audt";
            if (!saveName.toLowerCase().endsWith(".audt")) saveName += ".audt";

            // Update the file path and file name
            if (audtFilePath == null) {
                audtFilePath = saveDest;
                audtFileName = saveName;
            }

            log(Level.FINE, "AUDT file destination set to: " + saveDest);
        } else {
            // Use the existing file path and file name
            saveDest = audtFilePath;
            saveName = audtFileName;

            log(Level.FINE, "Saving '" + saveName + "' to: " + saveDest);
        }

        // Return the needed data
        return saveDest;
    }

    /**
     * Helper method that generates the auxiliary WAV file for the audio processing/playing.
     *
     * @param mp3Bytes Read MP3 bytes from the AudiTranscribe file.
     * @return A <code>File</code> object pointing to a WAV file representing the audio data.
     * @throws IOException If the auxiliary MP3 file does not exist, or if the decompression process
     *                     fails.
     */
    private File generateWAVFileFromMP3(byte[] mp3Bytes) throws IOException {
        // Generate a UUID for unique file identification
        String uuid = MiscUtils.generateUUID(mp3Bytes.length);

        // Create an empty temporary MP3 file in the temporary directory
        File auxiliaryMP3File = new File(IOMethods.joinPaths(IOConstants.TEMP_FOLDER_PATH, uuid + ".mp3"));
        IOMethods.createFile(auxiliaryMP3File);

        // Write the raw MP3 bytes into the temporary files
        FileOutputStream fos = new FileOutputStream(auxiliaryMP3File);
        fos.write(mp3Bytes);
        fos.close();

        // Generate the output path to the MP3 file
        String auxiliaryWAVFilePath = IOMethods.joinPaths(IOConstants.TEMP_FOLDER_PATH, uuid + "-temp.wav");

        // Convert the auxiliary MP3 files to a WAV files
        auxiliaryWAVFilePath = FFmpeg.convertAudio(auxiliaryMP3File, auxiliaryWAVFilePath);

        // Read the newly created WAV files
        File auxiliaryWAVFile = new File(auxiliaryWAVFilePath);

        // Delete the original MP3 file
        IOMethods.delete(auxiliaryMP3File);

        // Return `File` pointer
        return auxiliaryWAVFile;
    }

    /**
     * Helper method that closes this view.
     */
    private void closeTranscriptionView() {
        ((Stage) rootPane.getScene().getWindow()).close();
    }

    /**
     * Helper method that returns the debug information needed for the debug view's lists.
     *
     * @return Debug information as a list of pairs.
     */
    private List<Pair<String, String>> getDebugInfo() {
        return List.of(
                new Pair<>("finalWidth", Double.toString(finalWidth)),
                new Pair<>("finalHeight", Double.toString(finalHeight)),
                new Pair<>("-----", "-----"),
                new Pair<>("paused", Boolean.toString(paused)),
                new Pair<>("currTime", Double.toString(currTime)),
                new Pair<>("playheadX", playheadX.toString()),
                new Pair<>("Playhead Line X-Coord", playheadLine.startXProperty().toString()),
                new Pair<>("Coloured Progress Pane Position", colouredProgressRectangle.widthProperty().toString()),
                new Pair<>("-----", "-----"),
                new Pair<>("hasUnsavedChanges", Boolean.toString(hasUnsavedChanges)),
                new Pair<>("audtFilePath", audtFilePath),
                new Pair<>("audtFileName", audtFileName),
                new Pair<>("-----", "-----"),
                new Pair<>("audioMuted", Boolean.toString(audioMuted)),
                new Pair<>("Audio Volume", Double.toString(audioVolumeSlider.getValue()))
        );
    }
}
