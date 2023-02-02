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
import app.auditranscribe.audio.AudioProcessingMode;
import app.auditranscribe.audio.FFmpegHandler;
import app.auditranscribe.audio.exceptions.AudioTooLongException;
import app.auditranscribe.audio.exceptions.FFmpegNotFoundException;
import app.auditranscribe.fxml.IconHelper;
import app.auditranscribe.fxml.Popups;
import app.auditranscribe.fxml.Theme;
import app.auditranscribe.fxml.plotting.ColourScale;
import app.auditranscribe.fxml.plotting.PlottingHelpers;
import app.auditranscribe.fxml.plotting.PlottingStuffHandler;
import app.auditranscribe.fxml.plotting.Spectrogram;
import app.auditranscribe.fxml.views.main.scene_switching.SceneSwitchingData;
import app.auditranscribe.fxml.views.main.scene_switching.SceneSwitchingState;
import app.auditranscribe.generic.tuples.Pair;
import app.auditranscribe.io.CompressionHandlers;
import app.auditranscribe.io.IOConstants;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.audt_file.ProjectData;
import app.auditranscribe.io.audt_file.base.data_encapsulators.AudioDataObject;
import app.auditranscribe.io.audt_file.base.data_encapsulators.MusicNotesDataObject;
import app.auditranscribe.io.audt_file.base.data_encapsulators.QTransformDataObject;
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

    private final long UPDATE_PLAYBACK_SCHEDULER_PERIOD = 50;  // In milliseconds

    private final boolean FANCY_NOTE_LABELS = true;  // Use fancy accidentals for note labels

    private final double IMAGE_BUTTON_LENGTH = 50;  // In pixels

    private final int MIDI_CHANNEL_NUM = 0;
    private final MIDIInstrument NOTE_INSTRUMENT = MIDIInstrument.PIANO;
    private final int NOTE_ON_VELOCITY = 96;  // Within the range [0, 127]
    private final int NOTE_OFF_VELOCITY = 10;   // Within the range [0, 127]
    private final long NOTE_ON_DURATION = 75;  // In milliseconds
    private final long NOTE_OFF_DURATION = 925;  // In milliseconds

    private final double VOLUME_VALUE_DELTA_ON_KEY_PRESS = 0.05;  // Amount to change the volume by
    private final KeyCodeCombination SAVE_PROJECT_COMBINATION =
            new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN);
    private final KeyCodeCombination UNDO_NOTE_EDIT_COMBINATION =
            new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN);
    private final KeyCodeCombination REDO_NOTE_EDIT_COMBINATION =
            new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);
    private final KeyCodeCombination DEBUG_COMBINATION =
            new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);

    // Attributes
    private int numSkippableBytes;

    private ProjectsDB projectsDB;

    private Audio audio;
    private final DoubleProperty playheadX = new SimpleDoubleProperty(0);
    private double currTime = 0;
    private double audioDuration = 0;  // Will be updated upon scene initialization
    private double sampleRate;

    private byte[] qTransformBytes;
    private double minQTransformMagnitude;
    private double maxQTransformMagnitude;

    private double finalWidth;
    private double finalHeight;

    private double audioVolume = 1;  // Percentage from 0 to 200%
    private int notesVolume = 80;  // MIDI velocity
    private int octaveNum = 4;  // Currently highlighted octave number
    private boolean isAudioMuted = false;
    private boolean areNotesMuted = false;

    private boolean scrollToPlayhead = false;
    private boolean canEditNotes = false;

    private MusicKey musicKey = MusicKey.C_MAJOR;
    private double bpm = 120;
    private TimeSignature timeSignature = TimeSignature.FOUR_FOUR;
    private int beatsPerBar = timeSignature.beatsPerBar;
    private double offset = 0;

    private boolean hasUnsavedChanges = true;
    private boolean changedProjectName = false;

    private boolean isPaused = true;

    private NotePlayerSynth notePlayerSynth;
    //    private NotePlayerSequencer notePlayerSequencer;
    private MusicNotesDataObject musicNotesData;

    private boolean debugMode = false;

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
            saveAsMenuItem, exportMIDIMenuItem, settingsMenuItem, undoMenuItem, redoMenuItem, quantizeNotesMenuItem,
            docsMenuItem, aboutMenuItem;

    @FXML
    private VBox mainVBox;

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
    private HBox progressBarHBox, memoryUseHBox;

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
    private Pane notePane, barNumberPane, clickableProgressPane, colouredProgressPane;

    @FXML
    private ImageView spectrogramImage;

    private Rectangle currentOctaveRectangle;

    // Bottom
    @FXML
    private Button scrollButton, editNotesButton, playButton, rewindToBeginningButton, toggleSlowedAudioButton;

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
            notePlayerSynth = new NotePlayerSynth(NOTE_INSTRUMENT, MIDI_CHANNEL_NUM);
        } catch (MidiUnavailableException ignored) {  // We will notify the user that MIDI unavailable later
        }

        // Set spinners' factories
        bpmSpinnerFactory = new CustomDoubleSpinnerValueFactory(
                BPM_RANGE.value0(), BPM_RANGE.value1(), 120, 0.1, 2, "",
                " bpm"
        );
        bpmSpinner.setValueFactory(bpmSpinnerFactory);

        offsetSpinnerFactory = new CustomDoubleSpinnerValueFactory(
                OFFSET_RANGE.value0(), OFFSET_RANGE.value1(), 0, 0.01, 2, "",
                " s"
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
                isAudioMuted = false;
                toggleAudioMuteButton();
            } else if (isAudioMuted) {
                IconHelper.setSVGOnButton(
                        audioVolumeButton, 20, IMAGE_BUTTON_LENGTH, "volume-up-solid"
                );
                isAudioMuted = false;
            }

            // Update audio volume
            audio.setVolume(audioVolume);

            // Update CSS
            updateSliderCSS(audioVolumeSlider, audioVolume);

            log(Level.FINE, "Changed audio volume from " + oldValue + " to " + newValue);
        });

        notesVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Update the notes volume value
            notesVolume = newValue.intValue();

            // Change the icon of the notes' volume button if needed
            if (notesVolume == notesVolumeSlider.getMin()) {
                // Hacky way to set the mute icon
                areNotesMuted = false;
                toggleNoteMuteButton();
            } else if (areNotesMuted) {
                IconHelper.setSVGOnButton(
                        notesVolumeButton, 15, 20, IMAGE_BUTTON_LENGTH, IMAGE_BUTTON_LENGTH,
                        "music-note-solid"
                );
                areNotesMuted = false;
            }

            // Update CSS
            updateSliderCSS(notesVolumeSlider, notesVolume);

            log(Level.FINE, "Changed notes volume from " + oldValue + " to " + newValue);
        });

        // Add methods to buttons
        // Todo add the rest
        audioVolumeButton.setOnAction(event -> toggleAudioMuteButton());
        notesVolumeButton.setOnAction(event -> toggleNoteMuteButton());

        scrollButton.setOnAction(event -> toggleScrollButton());
        editNotesButton.setOnAction(event -> toggleEditNotesButton());

        playButton.setOnAction(event -> togglePlayButton());
        rewindToBeginningButton.setOnAction(event -> {
            log(Level.FINE, "Pressed rewind to beginning button");

            isPaused = togglePaused(false);  // Pause the audio
            seekToTime(0);
            updateScrollPosition(0, finalWidth);
        });
//        toggleSlowedAudioButton.setOnAction(event -> {
//            // Update the text that is shown
//            if (usingSlowedAudio) {
//                // Now no longer using it
//                toggleSlowedAudioButton.setText("1.0x");
//            } else {
//                // Starting to use it
//                toggleSlowedAudioButton.setText("0.5x");
//            }
//
//            // Update the flag
//            usingSlowedAudio = !usingSlowedAudio;
//
//            log(
//                    Level.FINE,
//                    "Toggled audio slowdown; audio playback speed is now " + (usingSlowedAudio ? "0.5x" : "1.0x")
//            );
//        });

        // Add methods to menu items
        // Todo: add the rest
//        newProjectMenuItem.setOnAction(this::handleNewProject);
//        openProjectMenuItem.setOnAction(this::handleOpenProject);
//        renameProjectMenuItem.setOnAction(this::handleRenameProject);
//        saveProjectMenuItem.setOnAction(event -> handleSavingProject(false, false));
//        saveAsMenuItem.setOnAction(event -> handleSavingProject(false, true));
//        exportMIDIMenuItem.setOnAction(event -> handleExportMIDI());
        settingsMenuItem.setOnAction(event -> SettingsViewController.showSettingsWindow());
//        undoMenuItem.setOnAction(event -> NoteRectangle.editAction(NoteRectangle.EditAction.UNDO));
//        redoMenuItem.setOnAction(event -> NoteRectangle.editAction(NoteRectangle.EditAction.REDO));
//        quantizeNotesMenuItem.setOnAction(event -> handleQuantizeNotes());
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

//                        if (canEditNotes) {
//                            if (isPaused) {  // Permit note placement only when paused
//                                // Compute the time that the mouse click would correspond to
//                                double estimatedTime = clickX / finalWidth * audioDuration;
//
//                                // Determine if it is a left click or a right click
//                                if (event.getButton() == MouseButton.PRIMARY) {
//                                    // Compute the duration of one beat
//                                    double beatDuration = 60 / bpm;
//
//                                    // Ignore any clicks that are too close to the boundary
//                                    if (estimatedTime > audioDuration - beatDuration ||
//                                            estimatedNoteNum < MIN_NOTE_NUMBER + 1 ||
//                                            estimatedNoteNum > MAX_NOTE_NUMBER - 1
//                                    ) return;
//
//                                    // Attempt to create a new note rectangle
//                                    try {
//                                        // Create note rectangle
//                                        NoteRectangle noteRect = new NoteRectangle(
//                                                estimatedTime, beatDuration, estimatedNoteNum
//                                        );
//
//                                        // Add the note rectangle to the spectrogram pane
//                                        spectrogramAnchorPane.getChildren().add(noteRect);
//                                        log(
//                                                Level.FINE,
//                                                "Placed note " + estimatedNoteNum + " at " + estimatedTime +
//                                                        " seconds"
//                                        );
//
//                                        // Update the `hasUnsavedChanges` flag
//                                        hasUnsavedChanges = true;
//                                    } catch (NoteRectangleCollisionException ignored) {
//                                    }
//                                }
//                            }
//
//                        } else {
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
//                        }
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
//        currTimeLabel.setText(UnitConversionUtils.secondsToTimeString(currTime));

        // Set keyboard button press/release methods
        mainVBox.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::keyPressEventHandler);
        mainVBox.getScene().addEventFilter(KeyEvent.KEY_RELEASED, this::keyReleasedEventHandler);

        // Define the lists note rectangles by note number
//        NoteRectangle.defineNoteRectanglesByNoteNumberLists(MAX_NOTE_NUMBER - MIN_NOTE_NUMBER + 1);

        // Report that the transcription view is ready to be shown
        log(Level.INFO, "Transcription view ready to be shown");
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
    public void setAudioAndSpectrogramData(Audio anAudio, SceneSwitchingData data) {
        // Set attributes
        audio = anAudio;
        audioDuration = anAudio.getDuration();
        sampleRate = anAudio.getSampleRate();

        projectName = data.projectName;

        // Generate spectrogram image based on audio
        CustomTask<WritableImage> spectrogramTask = new CustomTask<>() {
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
        CustomTask<Pair<Double, MusicKey>> estimationTask = new CustomTask<>("Estimation Task") {
            @Override
            protected Pair<Double, MusicKey> call() {
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
                MusicKey key;
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
                    key = mostLikelyKey;
                } else {
                    key = sceneSwitchingData.musicKey;
                }

                // Now return them both as a pair
                return new Pair<>(bpm, key);
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
        // Set attributes
        sampleRate = audioData.sampleRate;
        audioDuration = audioData.totalDurationInMS / 1000.;

        qTransformBytes = qTransformData.qTransformBytes;
        minQTransformMagnitude = qTransformData.minMagnitude;
        maxQTransformMagnitude = qTransformData.maxMagnitude;

        // Ensure that the temporary directory exists
        IOMethods.createFolder(IOConstants.TEMP_FOLDER_PATH);
        log(Level.FINE, "Temporary folder created: " + IOConstants.TEMP_FOLDER_PATH);

        // Initialize the FFmpeg handler (if not done already)
        FFmpegHandler.initFFmpegHandler(DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath);

        // Handle the compressed original MP3 bytes
        Pair<Byte[], File> returnedData = handleCompressedMP3Bytes(audioData.compressedOriginalMP3Bytes);
//        byte[] rawOriginalMP3Bytes = TypeConversionUtils.toByteArray(returnedData.value0());
        File auxOriginalWAVFile = returnedData.value1();

//        // Attempt to process the slowed MP3 bytes
//        byte[] rawSlowedMP3Bytes;
//        File auxSlowedWAVFile;
//        if (audioData.compressedSlowedMP3Bytes != null) {
//            returnedData = handleCompressedMP3Bytes(ffmpegHandler, audioData.compressedSlowedMP3Bytes);
//            rawSlowedMP3Bytes = TypeConversionUtils.toByteArray(returnedData.value0());
//            auxSlowedWAVFile = returnedData.value1();
//        } else {
//            log(Level.INFO, "Slowed audio not yet generated; generating now");
//
//            // Slow down the original WAV file
//            String auxSlowedMP3Path = FFmpegHandler.generateAltTempoAudio(
//                    auxOriginalWAVFile,
//                    IOMethods.joinPaths(IOConstants.TEMP_FOLDER_PATH, "slowed-temp-1.mp3"),
//                    0.5
//            );
//            File auxSlowedMP3File = new File(auxSlowedMP3Path);
//            rawSlowedMP3Bytes = Files.readAllBytes(Path.of(auxSlowedMP3Path));
//
//            // Convert the returned MP3 file into a WAV file
//            String auxSlowedWAVPath = ffmpegHandler.convertAudio(
//                    auxSlowedMP3File, IOMethods.joinPaths(IOConstants.TEMP_FOLDER_PATH, "slowed-temp-2.wav")
//            );
//            auxSlowedWAVFile = new File(auxSlowedWAVPath);
//
//            // Delete unneeded files
//            IOMethods.delete(auxSlowedMP3File);
//
//            log(Level.INFO, "Slowed audio generated");
//        }

        // Create the `Audio` object
//        audio = new Audio(
//                auxOriginalWAVFile, auxSlowedWAVFile, AudioProcessingMode.WITH_PLAYBACK,
//                AudioProcessingMode.WITH_SLOWDOWN
//        );
        audio = new Audio(auxOriginalWAVFile, AudioProcessingMode.WITH_PLAYBACK);

        // Update the raw MP3 bytes of the audio object
        // (This is to reduce the time needed to save the file later)
//        audio.setRawOriginalMP3Bytes(rawOriginalMP3Bytes);
//        audio.setRawSlowedMP3Bytes(rawSlowedMP3Bytes);

        // Update the audio object's duration
        // (The `MediaPlayer` duration cannot be trusted)
//        audio.setDuration(audioDuration);

        // Convert the bytes back into magnitude data
        double[][] magnitudes = QTransformDataObject.byteDataToQTransformMagnitudes(
                qTransformBytes, minQTransformMagnitude, maxQTransformMagnitude
        );

        // Generate spectrogram image based on existing magnitude data
        CustomTask<WritableImage> spectrogramTask = new CustomTask<>("Load Spectrogram") {
            @Override
            protected WritableImage call() {
                Spectrogram spectrogram = new Spectrogram(
                        MIN_NOTE_NUMBER, MAX_NOTE_NUMBER, BINS_PER_OCTAVE, SPECTROGRAM_HOP_LENGTH, PX_PER_SECOND,
                        NUM_PX_PER_OCTAVE, sampleRate, audioDuration, this
                );
                return spectrogram.generateSpectrogram(
                        magnitudes,
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
        // Todo implement
        // Get number of skippable bytes
        numSkippableBytes = projectData.unchangingDataProperties.numSkippableBytes;

        // Set up project data
        projectName = projectData.projectInfoData.projectName;
        musicKey = projectData.projectInfoData.musicKey;
        timeSignature = projectData.projectInfoData.timeSignature;
        bpm = projectData.projectInfoData.bpm;
        offset = projectData.projectInfoData.offsetSeconds;
        audioVolume = projectData.projectInfoData.playbackVolume;
//        currTime = projectData.projectInfoData.currTimeInMS / 1000.;

//        // Set the music notes data attribute
//        this.musicNotesData = projectData.musicNotesData;
//
//        // Set the AudiTranscribe file's file path and file name
//        this.audtFilePath = audtFilePath;
//        this.audtFileName = audtFileName;

        // Set up Q-Transform data and audio data
        try {
            setAudioAndSpectrogramData(projectData.qTransformData, projectData.audioData);
        } catch (IOException | UnsupportedAudioFileException e) {
            Popups.showExceptionAlert(
                    rootPane.getScene().getWindow(),
                    "Error loading audio data.",
                    "An error occurred when loading the audio data. Does the audio file " +
                            "still exist at the original location?",
                    e
            );
            logException(e);
            e.printStackTrace();
        } catch (FFmpegNotFoundException e) {
            Popups.showExceptionAlert(
                    rootPane.getScene().getWindow(),
                    "Error loading audio data.",
                    "FFmpeg was not found. Please install it and try again.",
                    e
            );
            logException(e);
            e.printStackTrace();
        } catch (AudioTooLongException e) {
            Popups.showExceptionAlert(
                    rootPane.getScene().getWindow(),
                    "Error loading audio data.",
                    "The audio file is too long. Please select a shorter audio file.",
                    e
            );
            logException(e);
            e.printStackTrace();
        }

        // Update beats per bar
        beatsPerBar = timeSignature.beatsPerBar;

        // Attempt to add this project to the projects' database
        try {
            if (projectsDB.checkIfProjectDoesNotExist(audtFilePath)) {
                // Insert the record into the database
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
        audio.deleteWAVFile();
        // Todo add
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
            if (isPaused) isPaused = togglePaused(true);
        }

        // Make audio seek to that time
        seekTime = MathUtils.round(seekTime, 3);
        audio.seekToTime(seekTime);

        // Todo remove?
//        // Update the start time of the audio
//        // (Do this so that when the player resumes out of a stop state it will start here)
//        audio.setAudioStartTime(seekTime);
//
//        // Set the playback time
//        // (We do this after updating start time to avoid pesky seeking issues)
//        audio.setAudioPlaybackTime(seekTime);

        // Update note sequencer current time
        // Todo implement
//        if (!areNotesMuted && notePlayerSequencer.isSequencerAvailable()) {
//            if (!notePlayerSequencer.getSequencer().isRunning() && !isPaused) {  // Not running but unpaused
//                notePlayerSequencer.play(seekTime, usingSlowedAudio);
//            } else {
//                notePlayerSequencer.setCurrTime(seekTime);
//            }
//        }

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

            // Stop note sequencer playback
//            notePlayerSequencer.stop();
        }

        Platform.runLater(() -> IconHelper.setSVGOnButton(playButton, 20, IMAGE_BUTTON_LENGTH, iconToUse));

        // Toggle paused state for note rectangles
//        NoteRectangle.setIsPaused(!isPaused);

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
            beatLines = PlottingStuffHandler.updateBeatLines(
                    spectrogramAnchorPane, beatLines, audioDuration, oldBPM, newBPM, offset, offset, finalHeight,
                    beatsPerBar, beatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
            );

            // Update the bar number ellipses
            barNumberEllipses = PlottingStuffHandler.updateBarNumberEllipses(
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
            beatLines = PlottingStuffHandler.updateBeatLines(
                    spectrogramAnchorPane, beatLines, audioDuration, bpm, bpm, oldOffset, newOffset, finalHeight,
                    beatsPerBar, beatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
            );

            // Update the bar number ellipses
            barNumberEllipses = PlottingStuffHandler.updateBarNumberEllipses(
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
            noteLabels = PlottingStuffHandler.addNoteLabels(
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

    // Task handlers

    /**
     * Helper method that sets up the spectrogram generation task.
     *
     * @param task    The spectrogram task.
     * @param message Message to display at the side of the progress bar.
     */
    private void setupSpectrogramTask(CustomTask<WritableImage> task, String message) {
        // Set the task's message
        task.setMessage(message);

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
            playheadLine = PlottingStuffHandler.createPlayheadLine(finalHeight);
            spectrogramAnchorPane.getChildren().add(playheadLine);

            // Bind properties
            colouredProgressPane.prefWidthProperty().bind(playheadX);
            playheadLine.startXProperty().bind(playheadX);
            playheadLine.endXProperty().bind(playheadX);
//
            // Schedule playback functionality
            scheduler.scheduleAtFixedRate(() -> {
                // Nothing really changes if the audio is paused
                if (!isPaused) {
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
                        isPaused = togglePaused(false);

                        // Todo: check if this works?
//                        // Specially update the start time to 0
//                        // (Because the `seekToTime` method would have set it to the end, which is not what we want)
//                        audio.setAudioStartTime(0);

                        // We need to do this so that the status is set to paused
//                        audio.stop();
                        audio.pause();
                    }

                    // Update scrolling
                    if (scrollToPlayhead) {
                        updateScrollPosition(playheadX.doubleValue(), spectrogramScrollPane.getWidth());
                    }
                }
            }, 0, UPDATE_PLAYBACK_SCHEDULER_PERIOD, TimeUnit.MILLISECONDS);
//
//            // Schedule debug view updating
//            if (debugMode) {
//                scheduler.scheduleAtFixedRate(() -> {
//                    if (debugViewController != null) {
//                        debugViewController.setListContent(getDebugInfo());
//                    }
//                }, 0, UPDATE_PLAYBACK_SCHEDULER_PERIOD, TimeUnit.MILLISECONDS);
//            }
//
//            // Schedule autosave functionality
//            scheduler.scheduleAtFixedRate(() -> Platform.runLater(
//                            () -> {
//                                if (audtFilePath != null) {
//                                    handleSavingProject(true, false);
//                                    log(Level.INFO, "Autosave project successful");
//                                } else {
//                                    log(Level.INFO, "Autosave skipped, since project was not loaded from file");
//                                }
//                            }),
//                    DataFiles.SETTINGS_DATA_FILE.data.autosaveInterval,
//                    DataFiles.SETTINGS_DATA_FILE.data.autosaveInterval,
//                    TimeUnit.MINUTES
//            );

            // Set image on the spectrogram area
            spectrogramImage.setFitHeight(finalWidth);
            spectrogramImage.setFitWidth(finalHeight);
            spectrogramImage.setImage(image);

            // Set the current octave rectangle
            currentOctaveRectangle = PlottingStuffHandler.addCurrentOctaveRectangle(
                    notePane, finalHeight, octaveNum, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER
            );

            // Add note labels and note lines
            noteLabels = PlottingStuffHandler.addNoteLabels(
                    notePane, noteLabels, musicKey, finalHeight, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER,
                    FANCY_NOTE_LABELS
            );
            PlottingStuffHandler.addNoteLines(spectrogramAnchorPane, finalHeight, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER);

            // Add the beat lines and bar number ellipses
            beatLines = PlottingStuffHandler.getBeatLines(
                    bpm, beatsPerBar, PX_PER_SECOND, finalHeight, audioDuration, offset, SPECTROGRAM_ZOOM_SCALE_X
            );
            PlottingStuffHandler.addBeatLines(spectrogramAnchorPane, beatLines);

            barNumberEllipses = PlottingStuffHandler.getBarNumberEllipses(
                    bpm, beatsPerBar, PX_PER_SECOND, bottomAnchorPane.getPrefHeight(), audioDuration, offset,
                    SPECTROGRAM_ZOOM_SCALE_X
            );
            PlottingStuffHandler.addBarNumberEllipses(barNumberPane, barNumberEllipses);

            // Resize spectrogram image pane
            // (We do this at the end to ensure that the image is properly placed)
            spectrogramImage.setFitWidth(finalWidth);
            spectrogramImage.setFitHeight(finalHeight);

            // Resize spectrogram pane
            spectrogramScrollPane.setPrefWidth(finalWidth);
            spectrogramScrollPane.setPrefHeight(finalHeight);

//            // Set `NoteRectangle` static attributes
//            NoteRectangle.setSpectrogramWidth(finalWidth);
//            NoteRectangle.setSpectrogramHeight(finalHeight);
//            NoteRectangle.setMinNoteNum(MIN_NOTE_NUMBER);
//            NoteRectangle.setMaxNoteNum(MAX_NOTE_NUMBER);
//            NoteRectangle.setTotalDuration(audioDuration);

            // Settle layout of the main pane
            mainVBox.layout();

            // Show the spectrogram from the middle
            spectrogramScrollPane.setVvalue(0.5);

            // Update volume sliders
            audioVolumeSlider.setValue(audioVolume);
            notesVolumeSlider.setValue(notesVolume);

            updateSliderCSS(audioVolumeSlider, audioVolume);
            updateSliderCSS(notesVolumeSlider, notesVolume);

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
    private void setupEstimationTask(CustomTask<Pair<Double, MusicKey>> task) {
        // Set task completion listener
        task.setOnSucceeded(event -> {
            // Get the BPM and key values
            Pair<Double, MusicKey> returnedPair = task.getValue();
            double newBPM = returnedPair.value0();
            MusicKey key = returnedPair.value1();

            // Update the BPM value
            updateBPMValue(MathUtils.round(newBPM, 1), false);

            // Update BPM spinner initial value
            bpmSpinner.setValueFactory(new CustomDoubleSpinnerValueFactory(
                    BPM_RANGE.value0(), BPM_RANGE.value1(), bpm, 0.1, 2
            ));

            // Update the music key choice
            updateMusicKeyValue(key, sceneSwitchingData.estimateMusicKey);  // Will force update if estimating key
            musicKeyChoice.setValue(key);

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
                        task.getException().printStackTrace();
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

                    log(Level.INFO, "Started task: '" + task.name + "'");

                    // Await for completion
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        logException(e);
                        throw new RuntimeException(e);
                    }
                }

                log(Level.INFO, "All tasks complete");
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

//                // Update playhead position
//                seekToTime(currTime);
//                updateScrollPosition(
//                        currTime * PX_PER_SECOND * SPECTROGRAM_ZOOM_SCALE_X,
//                        spectrogramScrollPane.getWidth()
//                );
//
//                // Set up note rectangles
//                if (musicNotesData != null) {
//                    int numNoteRectangles = musicNotesData.noteNums.length;
//                    for (int i = 0; i < numNoteRectangles; i++) {
//                        // Get the note rectangle data
//                        double timeToPlaceRectangle = musicNotesData.timesToPlaceRectangles[i];
//                        double noteDuration = musicNotesData.noteDurations[i];
//                        int noteNum = musicNotesData.noteNums[i];
//
//                        // Attempt to create a new note rectangle
//                        try {
//                            // Create the note rectangle
//                            NoteRectangle noteRect = new NoteRectangle(timeToPlaceRectangle, noteDuration, noteNum);
//
//                            // Add the note rectangle to the spectrogram pane
//                            spectrogramAnchorPane.getChildren().add(noteRect);
//
//                            log(
//                                    Level.FINE,
//                                    "Loaded note " + noteNum + " with " + noteDuration + " seconds duration at " +
//                                            timeToPlaceRectangle + " seconds"
//                            );
//                        } catch (NoteRectangleCollisionException ignored) {
//                        }
//                    }
//                }
//
//                // Check if the sequencer is available
//                if (!notePlayerSequencer.isSequencerAvailable()) {
//                    // Show a warning message to the user
//                    Popups.showWarningAlert(
//                            rootPane.getScene().getWindow(),
//                            "MIDI Playback Unavailable",
//                            "The MIDI playback is not available on your system. Playback of created " +
//                                    "notes will not work."
//                    );
//                }
//
//                // Reset the sequencer
//                notePlayerSequencer.stop();

                // Enable all disabled nodes
                Node[] disabledNodes = new Node[]{
                        // Top Hbox
                        audioVolumeButton, audioVolumeSlider, notesVolumeButton, notesVolumeSlider, musicKeyChoice,
                        bpmSpinner, timeSignatureChoice, offsetSpinner,

                        // Bottom Hbox
                        scrollButton, editNotesButton, playButton, rewindToBeginningButton, toggleSlowedAudioButton
                };

                for (Node node : disabledNodes) {
                    node.setDisable(false);
                }

                // Initialize the source data line for the audio
                audio.play();
                audio.pause();

//                // Clear note rectangles' stacks
//                NoteRectangle.clearStacks();
//
//                // Handle attempt to close the window
//                rootPane.getScene().getWindow().setOnCloseRequest((windowEvent) -> {
//                    // Deal with possible unsaved changes
//                    boolean canCloseWindow = handleUnsavedChanges();
//                    if (!canCloseWindow) windowEvent.consume();
//                });
//
//                // If we are using existing data (i.e., AUDT file path was already set), then initially there are no
//                // unsaved changes
//                if (audtFilePath != null) {
//                    hasUnsavedChanges = false;
//                    NoteRectangle.setHasEditedNoteRectangles(false);
//                }
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
            if (ongoingTasks.size() != 0) {
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

        // Handle note rectangle operations when toggle paused
        // Todo implement
//        if (isPaused && !areNotesMuted) {  // We use `isPaused` here because we will toggle it later
//            // Set up the note player sequencer by setting the notes on it
//            setupNotePlayerSequencer();
//        }

        // Toggle audio paused state
        if (currTime == audioDuration) {
            audio.seekToTime(0);
        }
        isPaused = togglePaused(isPaused);

        // Play notes on note player sequencer
        // (We separate this method from above to ensure a more accurate note playing delay)
        // Todo implement
//        if (!isPaused && !areNotesMuted) {  // We use `!isPaused` here because it was toggled already
//            notePlayerSequencer.play(currTime, usingSlowedAudio);
//        }

        // Disable note volume slider and note muting button if playing
        notesVolumeButton.setDisable(!isPaused);
        notesVolumeSlider.setDisable(!isPaused);

        log(Level.FINE, "Toggled play button; audio is now " + (!isPaused ? "paused" : "playing"));
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
     * Helper method that toggles the edit notes button.
     */
    private void toggleEditNotesButton() {
        // Change the icon
        String iconToUse = "pencil-solid";
        if (canEditNotes) iconToUse = "pencil-line";  // Want to change from filled to non-filled
        IconHelper.setSVGOnButton(editNotesButton, 20, IMAGE_BUTTON_LENGTH, iconToUse);

        // Toggle the `canEditNotes` flag
        canEditNotes = !canEditNotes;
//        NoteRectangle.setCanEdit(canEditNotes);  // Todo implement

        log(Level.FINE, "Toggled editing notes (editing notes is now " + canEditNotes + ")");
    }

    /**
     * Helper method that toggles the audio mute button.
     */
    private void toggleAudioMuteButton() {
        // Determine icon to use
        String iconToUse;

        if (isAudioMuted) {
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
        isAudioMuted = !isAudioMuted;

        log(Level.FINE, "Toggled audio mute button (audio muted is now " + isAudioMuted + ")");
    }

    /**
     * Helper method that toggles the note mute button.
     */
    private void toggleNoteMuteButton() {
        // Change the icon
        String iconToUse = "music-note-line";
        if (areNotesMuted) iconToUse = "music-note-solid";  // Want to change icon from off to on
        IconHelper.setSVGOnButton(
                notesVolumeButton, 15, 20, IMAGE_BUTTON_LENGTH, IMAGE_BUTTON_LENGTH, iconToUse
        );

        // Toggle the `areNotesMuted` flag
        areNotesMuted = !areNotesMuted;

        log(Level.FINE, "Toggled notes mute button (notes muted is now " + areNotesMuted + ")");
    }

    // Miscellaneous handlers

    /**
     * Helper method that handles the compressed MP3 bytes.
     *
     * @param compressedMP3Bytes Compressed MP3 bytes.
     * @return A pair. The first value is a byte array, representing the raw MP3 bytes. The second
     * is a <code>File</code> object pointing to a WAV file representing the audio data.
     * @throws IOException If the auxiliary MP3 file does not exist, or if the decompression process
     *                     fails.
     */
    private Pair<Byte[], File> handleCompressedMP3Bytes(byte[] compressedMP3Bytes) throws IOException {
        // Obtain the raw MP3 bytes
        byte[] rawMP3Bytes = CompressionHandlers.lz4Decompress(compressedMP3Bytes);

        // Generate a UUID for unique file identification
        String uuid = MiscUtils.generateUUID(rawMP3Bytes.length);

        // Create an empty temporary MP3 file in the temporary directory
        File auxiliaryMP3File = new File(IOMethods.joinPaths(IOConstants.TEMP_FOLDER_PATH, uuid + ".mp3"));
        IOMethods.createFile(auxiliaryMP3File);

        // Write the raw MP3 bytes into the temporary files
        FileOutputStream fos = new FileOutputStream(auxiliaryMP3File);
        fos.write(rawMP3Bytes);
        fos.close();

        // Generate the output path to the MP3 file
        String auxiliaryWAVFilePath = IOMethods.joinPaths(IOConstants.TEMP_FOLDER_PATH, uuid + "-temp.wav");

        // Convert the auxiliary MP3 files to a WAV files
        auxiliaryWAVFilePath = FFmpegHandler.convertAudio(auxiliaryMP3File, auxiliaryWAVFilePath);

        // Read the newly created WAV files
        File auxiliaryWAVFile = new File(auxiliaryWAVFilePath);

        // Delete the original MP3 file
        IOMethods.delete(auxiliaryMP3File);

        // Return needed information
        return new Pair<>(TypeConversionUtils.toByteArray(rawMP3Bytes), auxiliaryWAVFile);
    }

    // Keyboard event handlers

    /**
     * Helper method that handles a keyboard key press event.
     *
     * @param keyEvent Key press event.
     */
    private void keyPressEventHandler(KeyEvent keyEvent) {
        // If the spectrogram is not ready or if in the middle of editing do not do anything
//        if (!isEverythingReady || NoteRectangle.isEditing) {
        if (!isEverythingReady) {
            keyEvent.consume();
            return;
        }

        // Get the key event's target
        Node target = (Node) keyEvent.getTarget();

        // Check if the target is a text field or a spinner
        if (target instanceof TextField || target instanceof Spinner) {
            // If it is, do nothing
            return;
        }

        // Check if user is using any shortcuts
        if (SAVE_PROJECT_COMBINATION.match(keyEvent)) {  // Save current project
//            handleSavingProject(false, false);
            return;
        } else if (UNDO_NOTE_EDIT_COMBINATION.match(keyEvent)) {  // Undo note edit
//            NoteRectangle.editAction(NoteRectangle.EditAction.UNDO);
            return;
        } else if (REDO_NOTE_EDIT_COMBINATION.match(keyEvent)) {  // Redo note edit
//            NoteRectangle.editAction(NoteRectangle.EditAction.REDO);
            return;
        } else if (DEBUG_COMBINATION.match(keyEvent)) {  // Show debug view
//            debugViewController = DebugViewController.showDebugView(rootPane.getScene().getWindow());
            return;
        }

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
//            case N -> toggleEditNotesButton();
            case MINUS -> {
                notePlayerSynth.silenceChannel();  // Stop any notes from playing
                if (octaveNum > 0) {
                    log(Level.FINE, "Playback octave raised to " + octaveNum);
                    PlottingStuffHandler.updateCurrentOctaveRectangle(
                            currentOctaveRectangle, finalHeight, --octaveNum, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER
                    );
                }
            }
            case EQUALS -> {
                notePlayerSynth.silenceChannel();  // Stop any notes from playing
                if (octaveNum < 8) {
                    log(Level.FINE, "Playback octave lowered to " + octaveNum);
                    PlottingStuffHandler.updateCurrentOctaveRectangle(
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
        // If the spectrogram is not ready or if in the middle of editing do not do anything
//        if (!isEverythingReady || NoteRectangle.isEditing) {
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
}
