/*
 * TranscriptionViewController.java
 * Description: Contains the transcription view's controller class.
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

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import javafx.stage.Window;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.AudioProcessingMode;
import site.overwrite.auditranscribe.audio.FFmpegHandler;
import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.audio.exceptions.AudioTooLongException;
import site.overwrite.auditranscribe.audio.exceptions.FFmpegNotFoundException;
import site.overwrite.auditranscribe.generic.ClassWithLogging;
import site.overwrite.auditranscribe.generic.tuples.Pair;
import site.overwrite.auditranscribe.generic.tuples.Triple;
import site.overwrite.auditranscribe.io.CompressionHandlers;
import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.audt_file.AUDTFileConstants;
import site.overwrite.auditranscribe.io.audt_file.ProjectData;
import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.*;
import site.overwrite.auditranscribe.io.audt_file.v0x00090002.data_encapsulators.*;
import site.overwrite.auditranscribe.io.data_files.DataFiles;
import site.overwrite.auditranscribe.io.db.ProjectsDB;
import site.overwrite.auditranscribe.main_views.helpers.ProjectIOHandlers;
import site.overwrite.auditranscribe.main_views.icon.IconHelpers;
import site.overwrite.auditranscribe.main_views.scene_switching.SceneSwitchingData;
import site.overwrite.auditranscribe.main_views.scene_switching.SceneSwitchingState;
import site.overwrite.auditranscribe.misc.CustomTask;
import site.overwrite.auditranscribe.misc.Popups;
import site.overwrite.auditranscribe.misc.Theme;
import site.overwrite.auditranscribe.misc.spinners.CustomDoubleSpinnerValueFactory;
import site.overwrite.auditranscribe.music.MusicKey;
import site.overwrite.auditranscribe.music.MusicKeyEstimator;
import site.overwrite.auditranscribe.music.TimeSignature;
import site.overwrite.auditranscribe.music.bpm_estimation.BPMEstimator;
import site.overwrite.auditranscribe.music.exceptions.NoteRectangleCollisionException;
import site.overwrite.auditranscribe.music.notes.MIDIInstrument;
import site.overwrite.auditranscribe.music.notes.NotePlayerSequencer;
import site.overwrite.auditranscribe.music.notes.NotePlayerSynth;
import site.overwrite.auditranscribe.music.notes.NoteRectangle;
import site.overwrite.auditranscribe.plotting.PlottingHelpers;
import site.overwrite.auditranscribe.plotting.PlottingStuffHandler;
import site.overwrite.auditranscribe.spectrogram.ColourScale;
import site.overwrite.auditranscribe.spectrogram.Spectrogram;
import site.overwrite.auditranscribe.system.OSMethods;
import site.overwrite.auditranscribe.system.OSType;
import site.overwrite.auditranscribe.utils.*;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class TranscriptionViewController extends ClassWithLogging implements Initializable {
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

    private final long UPDATE_PLAYBACK_SCHEDULER_PERIOD = 50;  // In milliseconds

    private final boolean FANCY_NOTE_LABELS = true;  // Use fancy accidentals for note labels

    public final double VOLUME_VALUE_DELTA_ON_KEY_PRESS = 0.05;  // Amount to change the volume by

    public final int MIDI_CHANNEL_NUM = 0;
    private final MIDIInstrument NOTE_INSTRUMENT = MIDIInstrument.PIANO;
    private final int NOTE_ON_VELOCITY = 96;  // Within the range [0, 127]
    private final int NOTE_OFF_VELOCITY = 10;   // Within the range [0, 127]
    private final long NOTE_ON_DURATION = 75;  // In milliseconds
    private final long NOTE_OFF_DURATION = 925;  // In milliseconds

    private final KeyCodeCombination SAVE_PROJECT_COMBINATION =
            new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN);
    private final KeyCodeCombination UNDO_NOTE_EDIT_COMBINATION =
            new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN);
    private final KeyCodeCombination REDO_NOTE_EDIT_COMBINATION =
            new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);

    // File-Savable Attributes
    private int numSkippableBytes;

    private double sampleRate;  // Sample rate of the audio

    private String projectName;
    private int musicKeyIndex = 0;  // Index of the music key chosen, according to the `MUSIC_KEYS` array
    private TimeSignature timeSignature = TimeSignature.FOUR_FOUR;
    private double bpm = 120;
    private double offset = 0.;
    private double audioVolume = 0.5;
    private double audioDuration = 0;  // Will be updated upon scene initialization
    private double currTime = 0;

    // Other attributes
    private final DoubleProperty playheadX = new SimpleDoubleProperty(0);

    private boolean hasUnsavedChanges = true;
    private boolean changedProjectName = false;
    private int fileVersion;

    private Theme theme;

    private NotePlayerSynth notePlayerSynth;
    private NotePlayerSequencer notePlayerSequencer;
    private MusicNotesDataObject musicNotesData;

    private boolean isEverythingReady = false;

    private ProjectsDB projectsDB;

    private String audtFilePath;
    private String audtFileName;
    private Audio audio;
    private boolean isPaused = true;
    private boolean isAudioMuted = false;
    private boolean usingSlowedAudio = false;

    private byte[] qTransformBytes;  // These bytes are LZ4 compressed
    private double minQTransformMagnitude;
    private double maxQTransformMagnitude;

    private String musicKey = "C Major";
    private int beatsPerBar = 4;
    private boolean areNotesMuted = false;

    private boolean scrollToPlayhead = false;
    private boolean canEditNotes = false;

    private double finalWidth;
    private double finalHeight;

    private int octaveNum = 4;
    private int notesVolume = 80;

    private Label[] noteLabels;
    private Line[] beatLines;
    private StackPane[] barNumberEllipses;
    private Line playheadLine;

    public Queue<CustomTask<?>> ongoingTasks = new LinkedList<>();

    private SceneSwitchingState sceneSwitchingState = SceneSwitchingState.SHOW_MAIN_SCENE;
    private SceneSwitchingData sceneSwitchingData = new SceneSwitchingData();

    private ScheduledExecutorService scheduler, autosaveScheduler, memoryAvailableScheduler;

    // FXML Elements
    // Menu bar
    @FXML
    private MenuBar menuBar;

    // Todo: add undo/redo menu item
    @FXML
    private MenuItem newProjectMenuItem, openProjectMenuItem, renameProjectMenuItem, saveProjectMenuItem,
            saveAsMenuItem, exportMIDIMenuItem, preferencesMenuItem, quantizeNotesMenuItem, docsMenuItem, aboutMenuItem;

    // Main elements
    @FXML
    private VBox masterVBox;

    @FXML
    private AnchorPane rootPane, mainPane;

    // Top HBox
    @FXML
    private Button newProjectButton, openProjectButton, saveProjectButton;

    @FXML
    private ChoiceBox<String> musicKeyChoice;

    @FXML
    private ChoiceBox<TimeSignature> timeSignatureChoice;

    @FXML
    private Spinner<Double> bpmSpinner, offsetSpinner;

    @FXML
    private HBox progressBarHBox;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label freeMemoryLabel, maxMemoryLabel, progressLabel;

    // Mid-view
    @FXML
    private ScrollPane leftPane, spectrogramPane, bottomPane;

    @FXML
    private AnchorPane leftPaneAnchor, spectrogramPaneAnchor, bottomPaneAnchor;

    @FXML
    private Pane notePane, barNumberPane, clickableProgressPane, colouredProgressPane;

    @FXML
    private ImageView spectrogramImage;

    private Rectangle currentOctaveRectangle;

    // Bottom HBox
    @FXML
    private Label currTimeLabel, totalTimeLabel;

    @FXML
    private Button scrollButton, editNotesButton, playButton, stopButton, playStepBackwardButton, playStepForwardButton,
            toggleSlowedAudioButton, audioVolumeButton, notesVolumeButton;

    @FXML
    private Slider audioVolumeSlider, notesVolumeSlider;

    // Initialization method
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Make macOS systems use the system menu bar
        if (OSMethods.getOS() == OSType.MAC) {
            menuBar.useSystemMenuBarProperty().set(true);
        }

        // Clear the note rectangles
        NoteRectangle.allNoteRectangles.clear();
        NoteRectangle.noteRectanglesByNoteNumber.clear();

        // Set note rectangles' static attribute
        NoteRectangle.setSpectrogramPaneAnchor(spectrogramPaneAnchor);
        NoteRectangle.setIsPaused(isPaused);
        NoteRectangle.setCanEdit(canEditNotes);

        // Set the width and height of the root pane
        masterVBox.prefWidthProperty().bind(rootPane.widthProperty());
        masterVBox.prefHeightProperty().bind(rootPane.heightProperty());

        mainPane.prefWidthProperty().bind(rootPane.widthProperty());
        mainPane.prefHeightProperty().bind(rootPane.heightProperty().subtract(menuBar.heightProperty()));

        // Update attributes
        try {
            notePlayerSynth = new NotePlayerSynth(NOTE_INSTRUMENT, MIDI_CHANNEL_NUM);
        } catch (MidiUnavailableException ignored) {  // We will notify the user that MIDI unavailable later
        }

        notePlayerSequencer = new NotePlayerSequencer(DataFiles.SETTINGS_DATA_FILE.data.notePlayingDelayOffset);

        // Update spinners' ranges
        bpmSpinner.setValueFactory(new CustomDoubleSpinnerValueFactory(
                BPM_RANGE.value0(), BPM_RANGE.value1(), 120, 0.1, 2
        ));
        offsetSpinner.setValueFactory(new CustomDoubleSpinnerValueFactory(
                OFFSET_RANGE.value0(), OFFSET_RANGE.value1(), 0, 0.01, 2
        ));

        // Set the choice boxes' choices
        for (String musicKey : MusicUtils.MUSIC_KEYS) musicKeyChoice.getItems().add(musicKey);
        for (TimeSignature signature : TimeSignature.values()) timeSignatureChoice.getItems().add(signature);

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
                        beatLines = PlottingStuffHandler.updateBeatLines(
                                spectrogramPaneAnchor, beatLines, audioDuration, bpm, bpm, offset, offset,
                                finalHeight, oldBeatsPerBar, newBeatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
                        );

                        barNumberEllipses = PlottingStuffHandler.updateBarNumberEllipses(
                                barNumberPane, barNumberEllipses, audioDuration, bpm, bpm, offset, offset,
                                barNumberPane.getPrefHeight(), oldBeatsPerBar, newBeatsPerBar, PX_PER_SECOND,
                                SPECTROGRAM_ZOOM_SCALE_X, spectrogramPane.getWidth()
                        );
                    }

                    // Update the time signature index
                    timeSignature = newValue;

                    // Update the beats per bar
                    beatsPerBar = newBeatsPerBar;
                });

        // Add methods to buttons
        newProjectButton.setOnAction(this::handleNewProject);

        openProjectButton.setOnAction(this::handleOpenProject);

        saveProjectButton.setOnAction(event -> handleSavingProject(false, false));

        playButton.setOnAction(event -> togglePlayButton());

        stopButton.setOnAction(event -> {
            log(Level.FINE, "Pressed stop button");

            // Seek to beginning of the audio
            seekToTime(0);

            // Then trigger pause
            isPaused = togglePaused(false);
        });

        playStepBackwardButton.setOnAction(event -> {
            log(Level.FINE, "Pressed skip back button");

            notePlayerSequencer.stop();
            isPaused = togglePaused(false);  // Pause the audio
            seekToTime(0);
            updateScrollPosition(0, finalWidth);
        });

        playStepForwardButton.setOnAction(event -> {
            log(Level.FINE, "Pressed skip forward button");

            notePlayerSequencer.stop();
            isPaused = togglePaused(true);  // Seeking to end later will toggle paused again
            seekToTime(audioDuration);
            updateScrollPosition(finalWidth, finalWidth);
        });

        toggleSlowedAudioButton.setOnAction(event -> {
            // Update the text that is shown
            if (usingSlowedAudio) {
                // Now no longer using it
                toggleSlowedAudioButton.setText("1.0x");
            } else {
                // Starting to use it
                toggleSlowedAudioButton.setText("0.5x");
            }

            // Update the flag
            usingSlowedAudio = !usingSlowedAudio;

            log(
                    Level.FINE,
                    "Toggled audio slowdown; audio playback speed is now " + (usingSlowedAudio ? "0.5x" : "1.0x")
            );
        });

        scrollButton.setOnAction(event -> toggleScrollButton());

        editNotesButton.setOnAction(event -> toggleEditNotesButton());

        audioVolumeButton.setOnAction(event -> toggleAudioMuteButton());

        notesVolumeButton.setOnAction(event -> toggleNoteMuteButton());

        // Set spectrogram pane mouse event handler
        spectrogramPaneAnchor.addEventHandler(MouseEvent.ANY, new EventHandler<>() {
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

                    if (clickX >= spectrogramPaneAnchor.getBoundsInParent().getMinX() &&
                            clickX <= spectrogramPaneAnchor.getBoundsInParent().getMaxX() &&
                            clickY >= spectrogramPaneAnchor.getBoundsInParent().getMinY() &&
                            clickY <= spectrogramPaneAnchor.getBoundsInParent().getMaxY()
                    ) {
                        // Compute the frequency that the mouse click would correspond to
                        double estimatedFreq = PlottingHelpers.heightToFreq(
                                clickY,
                                UnitConversionUtils.noteNumberToFreq(MIN_NOTE_NUMBER),
                                UnitConversionUtils.noteNumberToFreq(MAX_NOTE_NUMBER),
                                spectrogramPaneAnchor.getHeight()
                        );

                        // Now estimate the note number
                        int estimatedNoteNum = (int) Math.round(UnitConversionUtils.freqToNoteNumber(estimatedFreq));

                        if (canEditNotes) {
                            if (isPaused) {  // Permit note placement only when paused
                                // Compute the time that the mouse click would correspond to
                                double estimatedTime = clickX / finalWidth * audioDuration;

                                // Determine if it is a left click or a right click
                                if (event.getButton() == MouseButton.PRIMARY) {
                                    // Compute the duration of one beat
                                    double beatDuration = 60 / bpm;

                                    // Ignore any clicks that are too close to the boundary
                                    if (estimatedTime > audioDuration - beatDuration ||
                                            estimatedNoteNum < MIN_NOTE_NUMBER + 1 ||
                                            estimatedNoteNum > MAX_NOTE_NUMBER - 1
                                    ) return;

                                    // Attempt to create a new note rectangle
                                    try {
                                        // Create note rectangle
                                        NoteRectangle noteRect = new NoteRectangle(
                                                estimatedTime, beatDuration, estimatedNoteNum
                                        );

                                        // Add the note rectangle to the spectrogram pane
                                        spectrogramPaneAnchor.getChildren().add(noteRect);
                                        log(
                                                Level.FINE,
                                                "Placed note " + estimatedNoteNum + " at " + estimatedTime +
                                                        " seconds"
                                        );

                                        // Update the `hasUnsavedChanges` flag
                                        hasUnsavedChanges = true;
                                    } catch (NoteRectangleCollisionException ignored) {
                                    }
                                }
                            }

                        } else {
                            // Play the note
                            log(
                                    Level.FINE,
                                    "Playing " + UnitConversionUtils.noteNumberToNote(
                                            estimatedNoteNum, musicKey, false
                                    )
                            );
                            notePlayerSynth.playNoteForDuration(
                                    estimatedNoteNum, NOTE_ON_VELOCITY, NOTE_OFF_VELOCITY,
                                    NOTE_ON_DURATION, NOTE_OFF_DURATION
                            );
                        }
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

        // Add methods to menu items
        newProjectMenuItem.setOnAction(this::handleNewProject);
        openProjectMenuItem.setOnAction(this::handleOpenProject);
        renameProjectMenuItem.setOnAction(this::handleRenameProject);
        saveProjectMenuItem.setOnAction(event -> handleSavingProject(false, false));
        saveAsMenuItem.setOnAction(event -> handleSavingProject(false, true));
        exportMIDIMenuItem.setOnAction(event -> handleExportMIDI());
        preferencesMenuItem.setOnAction(event -> PreferencesViewController.showPreferencesWindow());
        quantizeNotesMenuItem.setOnAction(event -> handleQuantizeNotes());
        docsMenuItem.setOnAction(event -> GUIUtils.openURLInBrowser("https://docs.auditranscribe.app/"));
        aboutMenuItem.setOnAction(event -> AboutViewController.showAboutWindow());

        // Create scheduler to update memory available
        memoryAvailableScheduler = Executors.newScheduledThreadPool(0, runnable -> {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setDaemon(true);
            return thread;
        });
        memoryAvailableScheduler.scheduleAtFixedRate(() -> Platform.runLater(() -> {
            // Get the presumed free memory available
            // (See https://stackoverflow.com/a/12807848)
            long allocatedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;

            // Update the free memory label
            freeMemoryLabel.setText(MathUtils.round(presumableFreeMemory / 1e6, 2) + " MB");
        }), 1, 1, TimeUnit.SECONDS);

        // Get the projects database
        try {
            projectsDB = new ProjectsDB();
        } catch (SQLException e) {
            logException(e);
            throw new RuntimeException(e);
        }

        // Set the maximum memory available
        long maxMemory = Runtime.getRuntime().maxMemory();
        maxMemoryLabel.setText(
                (maxMemory == Long.MAX_VALUE ? "∞" : MathUtils.round(maxMemory / 1e6, 2)) + " MB"
        );
    }

    // Getter/Setter methods
    public SceneSwitchingState getSceneSwitchingState() {
        if (sceneSwitchingState == null) return SceneSwitchingState.SHOW_MAIN_SCENE;
        return sceneSwitchingState;
    }

    public SceneSwitchingData getSceneSwitchingData() {
        return sceneSwitchingData;
    }

    public void setFileVersion(int fileVersion) {
        this.fileVersion = fileVersion;
    }

    // Public methods

    /**
     * Method that sets the theme for the scene.
     */
    public void setThemeOnScene() {
        // Get the theme
        theme = Theme.values()[DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal];

        // Set stylesheets
        rootPane.getStylesheets().clear();  // Reset the stylesheets first before adding new ones

        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/base.css"));
        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/" + theme.cssFile));

        // Set graphics
        IconHelpers.setSVGOnButton(
                newProjectButton, 20, IMAGE_BUTTON_LENGTH, "plus-circle-solid", theme.shortName
        );
        IconHelpers.setSVGOnButton(
                openProjectButton, 20, IMAGE_BUTTON_LENGTH, "folder-open-solid", theme.shortName
        );
        IconHelpers.setSVGOnButton(
                saveProjectButton, 20, IMAGE_BUTTON_LENGTH, "floppy-solid", theme.shortName
        );

        IconHelpers.setSVGOnButton(
                scrollButton, 15, 22.5, IMAGE_BUTTON_LENGTH, IMAGE_BUTTON_LENGTH,
                "map-marker-line", theme.shortName
        );
        IconHelpers.setSVGOnButton(
                editNotesButton, 20, IMAGE_BUTTON_LENGTH, "pencil-line", theme.shortName
        );

        IconHelpers.setSVGOnButton(
                playButton, 20, IMAGE_BUTTON_LENGTH, "play-solid", theme.shortName
        );
        IconHelpers.setSVGOnButton(
                stopButton, 20, IMAGE_BUTTON_LENGTH, "stop-solid", theme.shortName
        );
        IconHelpers.setSVGOnButton(
                playStepBackwardButton, 20, IMAGE_BUTTON_LENGTH, "step-backward-solid",
                theme.shortName
        );
        IconHelpers.setSVGOnButton(
                playStepForwardButton, 20, IMAGE_BUTTON_LENGTH, "step-forward-solid",
                theme.shortName
        );

        IconHelpers.setSVGOnButton(
                audioVolumeButton, 20, IMAGE_BUTTON_LENGTH, "volume-up-solid", theme.shortName
        );
        IconHelpers.setSVGOnButton(
                notesVolumeButton, 15, 20, IMAGE_BUTTON_LENGTH, IMAGE_BUTTON_LENGTH,
                "music-note-solid", theme.shortName
        );
    }

    /**
     * Method that finishes the setting up of the transcription view controller.<br>
     * Note that this method has to be called <b>last</b>, after all other spectrogram things have
     * been set up.
     */
    public void finishSetup() {
        // Set choices
        musicKeyChoice.setValue(MusicUtils.MUSIC_KEYS[musicKeyIndex]);
        timeSignatureChoice.setValue(timeSignature);

        // Update spinners' initial values
        updateBPMValue(bpm, true);
        updateOffsetValue(offset, true);

        bpmSpinner.setValueFactory(new CustomDoubleSpinnerValueFactory(
                BPM_RANGE.value0(), BPM_RANGE.value1(), bpm, 0.1, 2
        ));
        offsetSpinner.setValueFactory(new CustomDoubleSpinnerValueFactory(
                OFFSET_RANGE.value0(), OFFSET_RANGE.value1(), offset, 0.01, 2
        ));

        // Set methods on the volume sliders
        audioVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Update the `hasUnsavedChanges` flag
            hasUnsavedChanges = true;

            // Update the audio volume value
            audioVolume = newValue.doubleValue();

            // Change the icon of the audio volume button from mute to non-mute
            if (isAudioMuted) {
                IconHelpers.setSVGOnButton(
                        audioVolumeButton, 20, IMAGE_BUTTON_LENGTH, "volume-up-solid",
                        theme.shortName
                );
                isAudioMuted = false;
            }

            // Update audio volume
            audio.setPlaybackVolume(audioVolume);

            // Update CSS
            updateVolumeSliderCSS(audioVolumeSlider, audioVolume);

            log(Level.FINE, "Changed audio volume from " + oldValue + " to " + newValue);
        });

        notesVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Update the notes volume value
            notesVolume = newValue.intValue();

            // Change the icon of the notes' volume button from off to on
            if (areNotesMuted) {
                IconHelpers.setSVGOnButton(
                        notesVolumeButton, 15, 20, IMAGE_BUTTON_LENGTH, IMAGE_BUTTON_LENGTH,
                        "music-note-solid", theme.shortName
                );
                isAudioMuted = false;
            }

            // Update CSS
            updateVolumeSliderCSS(notesVolumeSlider, (double) (notesVolume - 33) / 94);

            log(Level.FINE, "Changed notes volume from " + oldValue + " to " + newValue);
        });

        // Update labels
        totalTimeLabel.setText(UnitConversionUtils.secondsToTimeString(audioDuration));
        currTimeLabel.setText(UnitConversionUtils.secondsToTimeString(currTime));

        // Set keyboard button press/release methods
        mainPane.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::keyPressEventHandler);
        mainPane.getScene().addEventFilter(KeyEvent.KEY_RELEASED, this::keyReleasedEventHandler);

        // Define the lists note rectangles by note number
        NoteRectangle.defineNoteRectanglesByNoteNumberLists(MAX_NOTE_NUMBER - MIN_NOTE_NUMBER + 1);

        // Report that the transcription view is ready to be shown
        log(Level.INFO, "Transcription view ready to be shown");
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
        musicKeyIndex = projectData.projectInfoData.musicKeyIndex;
        timeSignature = projectData.projectInfoData.timeSignature;
        bpm = projectData.projectInfoData.bpm;
        offset = projectData.projectInfoData.offsetSeconds;
        audioVolume = projectData.projectInfoData.playbackVolume;
        currTime = projectData.projectInfoData.currTimeInMS / 1000.;

        // Set the music notes data attribute
        this.musicNotesData = projectData.musicNotesData;

        // Set the AudiTranscribe file's file path and file name
        this.audtFilePath = audtFilePath;
        this.audtFileName = audtFileName;

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

        // Update music key and beats per bar
        musicKey = MusicUtils.MUSIC_KEYS[musicKeyIndex];
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
     * Method that sets the audio and spectrogram data for the transcription view controller.<br>
     * This method uses the actual audio file to do the setting of the data.
     *
     * @param audioObj           An <code>Audio</code> object that contains audio data.
     * @param sceneSwitchingData Scene switching data that controls whether certain tasks will be
     *                           executed (e.g. BPM estimation task).
     */
    public void setAudioAndSpectrogramData(Audio audioObj, SceneSwitchingData sceneSwitchingData) {
        // Set attributes
        audio = audioObj;
        audioDuration = audio.getDuration();
        sampleRate = audio.getSampleRate();

        projectName = sceneSwitchingData.projectName;

        // Generate spectrogram image based on newly generated magnitude data
        CustomTask<WritableImage> spectrogramTask = new CustomTask<>("Generate Spectrogram") {
            @Override
            protected WritableImage call() throws IOException {
                // Define a spectrogram object
                Spectrogram spectrogram = new Spectrogram(
                        audio, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER, BINS_PER_OCTAVE, SPECTROGRAM_HOP_LENGTH,
                        PX_PER_SECOND, NUM_PX_PER_OCTAVE, this
                );

                // Obtain the raw spectrogram magnitudes
                double[][] magnitudes = spectrogram.getSpectrogramMagnitudes(
                        WindowFunction.values()[DataFiles.SETTINGS_DATA_FILE.data.windowFunctionEnumOrdinal]
                );

                // Update attributes
                this.setMessage("Compressing spectrogram data...");
                Triple<Byte[], Double, Double> conversionTuple =
                        QTransformDataObject.qTransformMagnitudesToByteData(magnitudes, this);

                qTransformBytes = TypeConversionUtils.toByteArray(conversionTuple.value0());
                minQTransformMagnitude = conversionTuple.value1();
                maxQTransformMagnitude = conversionTuple.value2();

                // Generate spectrogram
                return spectrogram.generateSpectrogram(
                        magnitudes,
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
                    // Create a music key estimator
                    MusicKeyEstimator musicKeyEstimator = new MusicKeyEstimator(audio.getMonoSamples(), sampleRate);

                    // Get the top 4 most likely keys
                    List<Pair<MusicKey, Double>> mostLikelyKeys =
                            musicKeyEstimator.getMostLikelyKeysWithCorrelation(4, this);

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
     * @param audioData      The audio data that will be used in both the spectrogram data and
     *                       the audio data.
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

        // Define a FFmpeg handler
        FFmpegHandler ffmpegHandler = new FFmpegHandler(DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath);

        // Handle the compressed original MP3 bytes
        Pair<Byte[], File> returnedData = handleCompressedMP3Bytes(ffmpegHandler, audioData.compressedOriginalMP3Bytes);
        byte[] rawOriginalMP3Bytes = TypeConversionUtils.toByteArray(returnedData.value0());
        File auxOriginalWAVFile = returnedData.value1();

        // Attempt to process the slowed MP3 bytes
        byte[] rawSlowedMP3Bytes;
        File auxSlowedWAVFile;
        if (audioData.compressedSlowedMP3Bytes != null) {
            returnedData = handleCompressedMP3Bytes(ffmpegHandler, audioData.compressedSlowedMP3Bytes);
            rawSlowedMP3Bytes = TypeConversionUtils.toByteArray(returnedData.value0());
            auxSlowedWAVFile = returnedData.value1();
        } else {
            log(Level.INFO, "Slowed audio not yet generated; generating now");

            // Slow down the original WAV file
            String auxSlowedMP3Path = ffmpegHandler.generateAltTempoAudio(
                    auxOriginalWAVFile,
                    IOMethods.joinPaths(IOConstants.TEMP_FOLDER_PATH, "slowed-temp-1.mp3"),
                    0.5
            );
            File auxSlowedMP3File = new File(auxSlowedMP3Path);
            rawSlowedMP3Bytes = Files.readAllBytes(Path.of(auxSlowedMP3Path));

            // Convert the returned MP3 file into a WAV file
            String auxSlowedWAVPath = ffmpegHandler.convertAudio(
                    auxSlowedMP3File, IOMethods.joinPaths(IOConstants.TEMP_FOLDER_PATH, "slowed-temp-2.wav")
            );
            auxSlowedWAVFile = new File(auxSlowedWAVPath);

            // Delete unneeded files
            IOMethods.delete(auxSlowedMP3File);

            log(Level.INFO, "Slowed audio generated");
        }

        // Create the `Audio` object
        audio = new Audio(
                auxOriginalWAVFile, auxSlowedWAVFile, AudioProcessingMode.WITH_PLAYBACK,
                AudioProcessingMode.WITH_SLOWDOWN
        );

        // Update the raw MP3 bytes of the audio object
        // (This is to reduce the time needed to save the file later)
        audio.setRawOriginalMP3Bytes(rawOriginalMP3Bytes);
        audio.setRawSlowedMP3Bytes(rawSlowedMP3Bytes);

        // Delete the auxiliary files
        IOMethods.delete(auxOriginalWAVFile);
        IOMethods.delete(auxSlowedWAVFile);

        // Update the audio object's duration
        // (The `MediaPlayer` duration cannot be trusted)
        audio.setDuration(audioDuration);

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
     * Method that updates the scrolling of the page to center the playhead.
     *
     * @param newPosX New X position.
     * @param width   Width of the spectrogram pane.
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

    /**
     * Method that handles the things to do when the scene is to be closed.
     */
    public void handleSceneClosing() {
        // Stop the audio playing
        audio.stop();

        // Close the note player sequencer
        notePlayerSequencer.close();

        // Clear the note rectangles
        NoteRectangle.allNoteRectangles.clear();

        // Shut down the schedulers
        if (scheduler != null) scheduler.shutdown();
        if (autosaveScheduler != null) autosaveScheduler.shutdown();
        if (memoryAvailableScheduler != null) memoryAvailableScheduler.shutdown();
    }

    /**
     * Helper method that handles the unsaved changes.
     *
     * @return A boolean, <code>true</code> if the window should be closed, and <code>false</code>
     * if not.
     */
    public boolean handleUnsavedChanges() {
        // Do not do anything if the save button is disabled
        if (saveProjectButton.isDisabled()) return false;

        // Now check if there are unsaved changes
        if (hasUnsavedChanges || NoteRectangle.getHasEditedNoteRectangles()) {
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

            // Handle different cases
            if (selectedButton.isPresent()) {
                // Don't save and don't exit
                if (selectedButton.get() == saveAndExit) {
                    // Determine the save location
                    String saveDest = getSaveDestination(false);

                    // Try to save the project
                    if (saveDest != null) {
                        try {
                            saveData(false, saveDest, null);
                            return true;  // Can exit silently
                        } catch (IOException | FFmpegNotFoundException e) {
                            // Show exception that was thrown
                            Popups.showExceptionAlert(
                                    rootPane.getScene().getWindow(),
                                    "File Saving Failure",
                                    "AudiTranscribe failed to save the file.",
                                    e
                            );
                            logException(e);
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
                    // We just want to exit
                    return true;
                } else if (selectedButton.get() == dontSaveDontExit) {
                    // Don't want to exit
                    return false;
                } else {
                    // Assume default is don't want to leave
                    return false;
                }
            } else {
                // Assume don't want to save and don't want to exit
                return false;
            }
        } else {
            // If there are no unsaved changes, then closing window is permitted
            return true;
        }
    }

    // Main methods

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

        // Round `seekTime` to 3 decimal places
        seekTime = MathUtils.round(seekTime, 3);

        // Update the start time of the audio
        // (Do this so that when the player resumes out of a stop state it will start here)
        audio.setAudioStartTime(seekTime);

        // Set the playback time
        // (We do this after updating start time to avoid pesky seeking issues)
        audio.setAudioPlaybackTime(seekTime);

        // Update note sequencer current time
        if (!areNotesMuted && notePlayerSequencer.isSequencerAvailable()) {
            if (!notePlayerSequencer.getSequencer().isRunning() && !isPaused) {  // Not running but unpaused
                notePlayerSequencer.play(seekTime, usingSlowedAudio);
            } else {
                notePlayerSequencer.setCurrTime(seekTime);
            }
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
            audio.play(usingSlowedAudio);
        } else {  // Is currently playing; want to make audio pause
            // Change the icon of the play button from the paused icon to the play icon
            // (So that the user knows that the next interaction with button will play audio)
            iconToUse = "play-solid";

            // Pause the audio
            audio.pause();

            // Stop note sequencer playback
            notePlayerSequencer.stop();
        }

        Platform.runLater(() -> IconHelpers.setSVGOnButton(
                playButton, 20, IMAGE_BUTTON_LENGTH, iconToUse, theme.shortName
        ));

        // Toggle paused state for note rectangles
        NoteRectangle.setIsPaused(!isPaused);

        // Toggle disabled state of the toggle slowdown button
        toggleSlowedAudioButton.setDisable(isPaused);  // If currently paused, will block

        // Return the toggled version of the `isPaused` flag
        log(Level.FINE, "Toggled pause state; now is " + (!isPaused ? "paused" : "playing"));
        return !isPaused;
    }

    /**
     * Helper method that sets up the note player sequencer by setting the notes on it.
     */
    private void setupNotePlayerSequencer() {
        // Get note rectangles' data
        int numNoteRects = NoteRectangle.allNoteRectangles.size();
        Object[] noteRectsKeys = NoteRectangle.allNoteRectangles.keySet().toArray();

        // Get the note onset times, note durations, and note numbers from the note rectangles
        double[] noteOnsetTimes = new double[numNoteRects];
        double[] noteDurations = new double[numNoteRects];
        int[] noteNums = new int[numNoteRects];

        for (int i = 0; i < numNoteRects; i++) {
            String key = (String) noteRectsKeys[i];

            noteOnsetTimes[i] = NoteRectangle.allNoteRectangles.get(key).getNoteOnsetTime();
            noteDurations[i] = NoteRectangle.allNoteRectangles.get(key).getNoteDuration();
            noteNums[i] = NoteRectangle.allNoteRectangles.get(key).noteNum;
        }

        // Setup note player sequencer
        notePlayerSequencer.setOnVelocity(notesVolume);
        notePlayerSequencer.setOffVelocity(NOTE_OFF_VELOCITY);
        notePlayerSequencer.setBPM(bpm);
        notePlayerSequencer.setInstrument(NOTE_INSTRUMENT);

        // Clear existing notes, and set new notes
        notePlayerSequencer.setNotesOnTrack(noteOnsetTimes, noteDurations, noteNums, usingSlowedAudio);
    }

    // IO methods

    /**
     * Helper method that helps open a new project.
     *
     * @param event Event that triggered this function.
     */
    private void handleNewProject(Event event) {
        // Do not do anything if the button is disabled
        if (newProjectButton.isDisabled()) return;

        // Pause the current audio
        isPaused = togglePaused(false);

        // Stop note sequencer playback
        notePlayerSequencer.stop();

        // Deal with possible unsaved changes
        boolean canCloseWindow = handleUnsavedChanges();
        if (canCloseWindow) {
            // Get the scene switching data
            Pair<Boolean, SceneSwitchingData> pair = ProjectSetupViewController.showProjectSetupView();
            boolean shouldProceed = pair.value0();
            sceneSwitchingData = pair.value1();

            // Specify the scene switching state
            if (shouldProceed) {
                // Signal the creation of a new project
                sceneSwitchingState = SceneSwitchingState.NEW_PROJECT;

                // Close this stage
                ((Stage) rootPane.getScene().getWindow()).close();
            }
        }
    }

    /**
     * Helper method that helps open an existing project.
     *
     * @param event Event that triggered this function.
     */
    private void handleOpenProject(Event event) {
        // Do not do anything if the button is disabled
        if (openProjectButton.isDisabled()) return;

        // Pause the current audio
        isPaused = togglePaused(false);

        // Stop note sequencer playback
        notePlayerSequencer.stop();

        // Deal with possible unsaved changes
        boolean canCloseWindow = handleUnsavedChanges();
        if (canCloseWindow) {
            // Get the current window
            Window window = rootPane.getScene().getWindow();

            // Get user to select an AUDT file
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                    "AudiTranscribe files (*.audt)", "*.audt"
            );
            File file = ProjectIOHandlers.getFileFromFileDialog(window, extFilter);

            // If a file was selected, stop the audio completely
            if (file != null) {
                audio.stop();
            }

            // Verify that the user actually chose a file
            if (file == null) {
                Popups.showInformationAlert(rootPane.getScene().getWindow(), "Info", "No file selected.");
            } else {
                // Set the scene switching status and the selected file
                sceneSwitchingState = SceneSwitchingState.OPEN_PROJECT;
                sceneSwitchingData.file = file;

                // Close this stage
                ((Stage) rootPane.getScene().getWindow()).close();
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
        // Do not do anything if the button is disabled
        if (saveProjectButton.isDisabled()) return;

        // Get the save destination
        String saveDest = getSaveDestination(forceChooseFile);

        // Set up task to run in alternate thread
        CustomTask<Void> task = new CustomTask<>("Save Project") {
            @Override
            protected Void call() throws Exception {
                saveData(forceChooseFile, saveDest, this);
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
     * Helper method that handles the exporting of the note rectangles' data to MIDI.
     */
    private void handleExportMIDI() {
        // Get current window
        Window window = rootPane.getScene().getWindow();

        // Ask user to choose a file
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "MIDI Files (*.mid, *.midi)",
                "*.mid", "*.midi"
        ));
        File file = fileChooser.showSaveDialog(window);

        // If operation was cancelled, show error
        if (file == null) {
            Popups.showInformationAlert(
                    rootPane.getScene().getWindow(),
                    "No destination specified",
                    "No destination was specified. The MIDI file will not be created."
            );
            return;
        }

        // Set up the note player sequencer by setting the notes on it
        setupNotePlayerSequencer();

        // Now write the sequence to the MIDI file
        try {
            notePlayerSequencer.exportToMIDI(
                    timeSignature, musicKey, file.getAbsolutePath()
            );
            log(Level.FINE, "Exported notes to '" + file.getAbsolutePath() + "'.");
            Popups.showInformationAlert(
                    rootPane.getScene().getWindow(),
                    "Successfully exported to MIDI",
                    "Successfully exported to MIDI."
            );
        } catch (IOException e) {
            logException(e);
            Popups.showExceptionAlert(
                    rootPane.getScene().getWindow(),
                    "Failed to export to MIDI file",
                    "An exception occurred when exporting the notes to MIDI file.",
                    e
            );
        }
    }

    /**
     * Helper method that helps quantize the notes.
     */
    private void handleQuantizeNotes() {
        NoteRectangle.quantizeNotes(bpm, offset, timeSignature);
        log(Level.FINE, "Quantized notes");
    }

    /**
     * Helper method that helps save the data into an AUDT file.
     *
     * @param forceChooseFile Whether the file was forcibly chosen.
     * @param saveDest        The destination to save the file to.
     * @param task            The <code>CustomTask</code> object that will handle the saving of the
     *                        file.
     * @throws FFmpegNotFoundException If the FFmpeg binary could not be found.
     * @throws IOException             If the saving to the AUDT file failed.
     */
    private void saveData(
            boolean forceChooseFile, String saveDest, CustomTask<?> task
    ) throws FFmpegNotFoundException, IOException {
        // Get note rectangles' data
        int numRectangles = NoteRectangle.allNoteRectangles.size();
        Object[] noteRectsKeys = NoteRectangle.allNoteRectangles.keySet().toArray();

        double[] timesToPlaceRectangles = new double[numRectangles];
        double[] noteDurations = new double[numRectangles];
        int[] noteNums = new int[numRectangles];

        for (int i = 0; i < numRectangles; i++) {
            String key = (String) noteRectsKeys[i];
            NoteRectangle noteRectangle = NoteRectangle.allNoteRectangles.get(key);

            timesToPlaceRectangles[i] = noteRectangle.getNoteOnsetTime();
            noteDurations[i] = noteRectangle.getNoteDuration();
            noteNums[i] = noteRectangle.noteNum;
        }

        // Package project info data and music notes data for saving
        // (Note: current file version is 0x00090002, so all data objects used will be for that version)
        log(Level.INFO, "Packaging data for saving");

        ProjectInfoDataObject projectInfoData = new ProjectInfoDataObject0x00090002(
                projectName, musicKeyIndex, timeSignature, bpm, offset, audioVolume,
                (int) (currTime * 1000)
        );
        MusicNotesDataObject musicNotesData = new MusicNotesDataObject0x00090002(
                timesToPlaceRectangles, noteDurations, noteNums
        );

        // Determine what mode of the writer should be used
        if (numSkippableBytes == 0 || forceChooseFile || fileVersion != AUDTFileConstants.FILE_VERSION_NUMBER) {
            // Compress the audio data
            byte[] compressedOriginalMP3Bytes;
            try {
                compressedOriginalMP3Bytes = CompressionHandlers.lz4Compress(
                        audio.originalWAVBytesToMP3Bytes(DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath),
                        task
                );
            } catch (IOException e) {
                logException(e);
                throw new RuntimeException(e);
            }

            byte[] compressedSlowedMP3Bytes;
            try {
                compressedSlowedMP3Bytes = CompressionHandlers.lz4Compress(
                        audio.slowedWAVBytesToMP3Bytes(DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath),
                        task
                );
            } catch (IOException e) {
                logException(e);
                throw new RuntimeException(e);
            }

            // Package Q-transform data and audio data for saving
            QTransformDataObject qTransformData = new QTransformDataObject0x00090002(
                    qTransformBytes, minQTransformMagnitude, maxQTransformMagnitude
            );
            AudioDataObject audioData = new AudioDataObject0x00090002(
                    compressedOriginalMP3Bytes, compressedSlowedMP3Bytes, sampleRate, (int) (audioDuration * 1000)
            );

            // Calculate the number of skippable bytes
            numSkippableBytes = 32 +  // Header section
                    UnchangingDataPropertiesObject.NUM_BYTES_NEEDED +
                    qTransformData.numBytesNeeded() +
                    audioData.numBytesNeeded();

            // Update the unchanging data properties
            UnchangingDataPropertiesObject unchangingDataProperties = new UnchangingDataPropertiesObject0x00090002(
                    numSkippableBytes
            );

            // Package all the current data into a `ProjectData`
            ProjectData projectData = new ProjectData(
                    unchangingDataProperties, qTransformData, audioData, projectInfoData, musicNotesData
            );

            // Save the project
            ProjectIOHandlers.saveProject(saveDest, projectData);

        } else {
            ProjectIOHandlers.saveProject(saveDest, numSkippableBytes, projectInfoData, musicNotesData);
        }

        // Set flags
        hasUnsavedChanges = false;
        NoteRectangle.setHasEditedNoteRectangles(false);

        log(Level.INFO, "File saved successfully");
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
                    spectrogramPaneAnchor, beatLines, audioDuration, oldBPM, newBPM, offset, offset, finalHeight,
                    beatsPerBar, beatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
            );

            // Update the bar number ellipses
            barNumberEllipses = PlottingStuffHandler.updateBarNumberEllipses(
                    barNumberPane, barNumberEllipses, audioDuration, oldBPM, newBPM, offset, offset,
                    barNumberPane.getPrefHeight(), beatsPerBar, beatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X,
                    spectrogramPane.getWidth()
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
                    spectrogramPaneAnchor, beatLines, audioDuration, bpm, bpm, oldOffset, newOffset, finalHeight,
                    beatsPerBar, beatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
            );

            // Update the bar number ellipses
            barNumberEllipses = PlottingStuffHandler.updateBarNumberEllipses(
                    barNumberPane, barNumberEllipses, audioDuration, bpm, bpm, oldOffset, newOffset,
                    barNumberPane.getPrefHeight(), beatsPerBar, beatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X,
                    spectrogramPane.getWidth()
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
    private void updateMusicKeyValue(String newMusicKey, boolean forceUpdate) {
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
        musicKeyIndex = ArrayUtils.findIndex(MusicUtils.MUSIC_KEYS, newMusicKey);
    }

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
                fillAmount * 100, fillAmount * 100);

        // Apply the style to the volume slider's track (if available)
        StackPane track = (StackPane) volumeSlider.lookup(".track");
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
            leftPane.setFitToWidth(true);
            leftPaneAnchor.setPrefHeight(finalHeight);

            spectrogramPaneAnchor.setPrefWidth(finalWidth);
            spectrogramPaneAnchor.setPrefHeight(finalHeight);

            bottomPane.setFitToHeight(true);
            bottomPaneAnchor.setPrefWidth(finalWidth);

            clickableProgressPane.setPrefWidth(finalWidth);

            // Set scrolling for panes
            leftPane.vvalueProperty().bindBidirectional(spectrogramPane.vvalueProperty());
            bottomPane.hvalueProperty().bindBidirectional(spectrogramPane.hvalueProperty());

            // Add the playhead line
            playheadLine = PlottingStuffHandler.createPlayheadLine(finalHeight);
            spectrogramPaneAnchor.getChildren().add(playheadLine);

            // Bind properties
            colouredProgressPane.prefWidthProperty().bind(playheadX);
            playheadLine.startXProperty().bind(playheadX);
            playheadLine.endXProperty().bind(playheadX);

            // Create a constantly-executing service for playback functionality
            scheduler = Executors.newScheduledThreadPool(0, runnable -> {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setDaemon(true);  // Make it so that it can shut down gracefully by placing it in background
                return thread;
            });
            scheduler.scheduleAtFixedRate(() -> {
                // Nothing really changes if the audio is paused
                if (!isPaused) {
                    // Get the current audio time
                    currTime = audio.getCurrAudioTime(usingSlowedAudio);

                    // Update the current time label
                    Platform.runLater(() -> currTimeLabel.setText(UnitConversionUtils.secondsToTimeString(currTime)));

                    // Update the playhead X position
                    playheadX.set(currTime * PX_PER_SECOND * SPECTROGRAM_ZOOM_SCALE_X);

                    // Check if the current time has exceeded and is not paused
                    if (currTime >= audioDuration) {
                        log(Level.FINE, "Playback reached end of audio, will start from beginning upon play");

                        // Pause the audio
                        isPaused = togglePaused(false);

                        // Specially update the start time to 0
                        // (Because the `seekToTime` method would have set it to the end, which is not what we want)
                        audio.setAudioStartTime(0);

                        // We need to do this so that the status is set to paused
                        audio.stop();
                        audio.pause();
                    }

                    // Update scrolling
                    if (scrollToPlayhead) {
                        updateScrollPosition(playheadX.doubleValue(), spectrogramPane.getWidth());
                    }
                }
            }, 0, UPDATE_PLAYBACK_SCHEDULER_PERIOD, TimeUnit.MILLISECONDS);

            // Create another constantly-executing service for autosaving
            autosaveScheduler = Executors.newScheduledThreadPool(0, runnable -> {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setDaemon(true);
                return thread;
            });
            autosaveScheduler.scheduleAtFixedRate(() -> Platform.runLater(
                            () -> {
                                if (audtFilePath != null) {
                                    handleSavingProject(true, false);
                                    log(Level.INFO, "Autosave project successful");
                                } else {
                                    log(Level.INFO, "Autosave skipped, since project was not loaded from file");
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
            currentOctaveRectangle = PlottingStuffHandler.addCurrentOctaveRectangle(
                    notePane, finalHeight, octaveNum, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER
            );

            // Add note labels and note lines
            noteLabels = PlottingStuffHandler.addNoteLabels(
                    notePane, noteLabels, musicKey, finalHeight, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER,
                    FANCY_NOTE_LABELS
            );
            PlottingStuffHandler.addNoteLines(spectrogramPaneAnchor, finalHeight, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER);

            // Add the beat lines and bar number ellipses
            beatLines = PlottingStuffHandler.getBeatLines(
                    bpm, beatsPerBar, PX_PER_SECOND, finalHeight, audioDuration, offset, SPECTROGRAM_ZOOM_SCALE_X
            );
            PlottingStuffHandler.addBeatLines(spectrogramPaneAnchor, beatLines);

            barNumberEllipses = PlottingStuffHandler.getBarNumberEllipses(
                    bpm, beatsPerBar, PX_PER_SECOND, barNumberPane.getPrefHeight(), audioDuration, offset,
                    SPECTROGRAM_ZOOM_SCALE_X
            );
            PlottingStuffHandler.addBarNumberEllipses(barNumberPane, barNumberEllipses);

            // Resize spectrogram image pane
            // (We do this at the end to ensure that the image is properly placed)
            spectrogramImage.setFitWidth(finalWidth);
            spectrogramImage.setFitHeight(finalHeight);

            // Resize spectrogram pane
            spectrogramPane.setPrefWidth(finalWidth);
            spectrogramPane.setPrefHeight(finalHeight);

            // Set `NoteRectangle` static attributes
            NoteRectangle.setSpectrogramWidth(finalWidth);
            NoteRectangle.setSpectrogramHeight(finalHeight);
            NoteRectangle.setMinNoteNum(MIN_NOTE_NUMBER);
            NoteRectangle.setMaxNoteNum(MAX_NOTE_NUMBER);
            NoteRectangle.setTotalDuration(audioDuration);

            // Settle layout of the main pane
            mainPane.layout();

            // Show the spectrogram from the middle
            spectrogramPane.setVvalue(0.5);

            // Update volume sliders
            audioVolumeSlider.setValue(audioVolume);
            notesVolumeSlider.setValue(notesVolume);

            updateVolumeSliderCSS(audioVolumeSlider, audioVolume);
            updateVolumeSliderCSS(notesVolumeSlider, (double) (notesVolume - 33) / 94);

            // Ensure main pane is in focus
            rootPane.requestFocus();

            // Mark the task as completed and report that the transcription view is ready to be shown
            markTaskAsCompleted(task);
            log(Level.INFO, "Spectrogram for '" + projectName + "' ready to be shown");
        });
    }

    /**
     * Helper method that sets up the estimation task.
     *
     * @param task The estimation task.
     */
    private void setupEstimationTask(CustomTask<Pair<Double, String>> task) {
        // Set task completion listener
        task.setOnSucceeded(event -> {
            // Get the BPM and key values
            Pair<Double, String> returnedPair = task.getValue();
            double newBPM = returnedPair.value0();
            String key = returnedPair.value1();

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
            log(Level.INFO, "Estimation task complete");
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

                // Update playhead position
                seekToTime(currTime);
                updateScrollPosition(
                        currTime * PX_PER_SECOND * SPECTROGRAM_ZOOM_SCALE_X,
                        spectrogramPane.getWidth()
                );

                // Set up note rectangles
                if (musicNotesData != null) {
                    int numNoteRectangles = musicNotesData.noteNums.length;
                    for (int i = 0; i < numNoteRectangles; i++) {
                        // Get the note rectangle data
                        double timeToPlaceRectangle = musicNotesData.timesToPlaceRectangles[i];
                        double noteDuration = musicNotesData.noteDurations[i];
                        int noteNum = musicNotesData.noteNums[i];

                        // Attempt to create a new note rectangle
                        try {
                            // Create the note rectangle
                            NoteRectangle noteRect = new NoteRectangle(timeToPlaceRectangle, noteDuration, noteNum);

                            // Add the note rectangle to the spectrogram pane
                            spectrogramPaneAnchor.getChildren().add(noteRect);

                            log(
                                    Level.FINE,
                                    "Loaded note " + noteNum + " with " + noteDuration + " seconds duration at " +
                                            timeToPlaceRectangle + " seconds"
                            );
                        } catch (NoteRectangleCollisionException ignored) {
                        }
                    }
                }

                // Check if the sequencer is available
                if (!notePlayerSequencer.isSequencerAvailable()) {
                    // Show a warning message to the user
                    Popups.showWarningAlert(
                            rootPane.getScene().getWindow(),
                            "MIDI Playback Unavailable",
                            "The MIDI playback is not available on your system. Playback of created " +
                                    "notes will not work."
                    );
                }

                // Reset the sequencer
                notePlayerSequencer.stop();

                // Enable all disabled nodes
                Node[] disabledNodes = new Node[]{
                        // Top Hbox
                        newProjectButton, openProjectButton, saveProjectButton,
                        musicKeyChoice, bpmSpinner, timeSignatureChoice, offsetSpinner,

                        // Bottom Hbox
                        audioVolumeButton, audioVolumeSlider, notesVolumeButton, notesVolumeSlider,
                        playButton, stopButton, playStepBackwardButton, playStepForwardButton,
                        toggleSlowedAudioButton, scrollButton, editNotesButton
                };

                for (Node node : disabledNodes) {
                    node.setDisable(false);
                }

                // Clear note rectangles' stacks
                NoteRectangle.clearStacks();

                // Handle attempt to close the window
                rootPane.getScene().getWindow().setOnCloseRequest((windowEvent) -> {
                    // Deal with possible unsaved changes
                    boolean canCloseWindow = handleUnsavedChanges();
                    if (!canCloseWindow) windowEvent.consume();
                });

                // If we are using existing data (i.e., AUDT file path was already set), then initially there are no
                // unsaved changes
                if (audtFilePath != null) {
                    hasUnsavedChanges = false;
                    NoteRectangle.setHasEditedNoteRectangles(false);
                }
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
        if (isPaused && !areNotesMuted) {  // We use `isPaused` here because we will toggle it later
            // Set up the note player sequencer by setting the notes on it
            setupNotePlayerSequencer();
        }

        // Toggle audio paused state
        if (currTime == audioDuration) {
            audio.setAudioPlaybackTime(0);
        }
        isPaused = togglePaused(isPaused);

        // Play notes on note player sequencer
        // (We separate this method from above to ensure a more accurate note playing delay)
        if (!isPaused && !areNotesMuted) {  // We use `!isPaused` here because it was toggled already
            notePlayerSequencer.play(currTime, usingSlowedAudio);
        }

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
        IconHelpers.setSVGOnButton(
                scrollButton, 15, 22.5, IMAGE_BUTTON_LENGTH, IMAGE_BUTTON_LENGTH, iconToUse,
                theme.shortName
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
        IconHelpers.setSVGOnButton(editNotesButton, 20, IMAGE_BUTTON_LENGTH, iconToUse, theme.shortName);

        // Toggle the `canEditNotes` flag
        canEditNotes = !canEditNotes;
        NoteRectangle.setCanEdit(canEditNotes);

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
            audio.setPlaybackVolume(audioVolume);
        } else {
            // Want to change from non-mute to mute
            iconToUse = "volume-mute-solid";

            // Mute the audio by setting the volume to zero
            audio.setPlaybackVolume(0);
        }

        // Change the icon
        IconHelpers.setSVGOnButton(
                audioVolumeButton, 20, IMAGE_BUTTON_LENGTH, iconToUse, theme.shortName
        );

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
        IconHelpers.setSVGOnButton(
                notesVolumeButton, 15, 20, IMAGE_BUTTON_LENGTH, IMAGE_BUTTON_LENGTH, iconToUse,
                theme.shortName
        );

        // Toggle the `areNotesMuted` flag
        areNotesMuted = !areNotesMuted;

        log(Level.FINE, "Toggled notes mute button (notes muted is now " + areNotesMuted + ")");
    }

    // Keyboard event handlers

    /**
     * Helper method that handles a keyboard key press event.
     *
     * @param keyEvent Key press event.
     */
    private void keyPressEventHandler(KeyEvent keyEvent) {
        // If the spectrogram is not ready or if in the middle of editing do not do anything
        if (!isEverythingReady || NoteRectangle.isEditing) {
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
            handleSavingProject(false, false);
            return;
        } else if (UNDO_NOTE_EDIT_COMBINATION.match(keyEvent)) {  // Undo note edit
            NoteRectangle.editAction(NoteRectangle.EditAction.UNDO);
        } else if (REDO_NOTE_EDIT_COMBINATION.match(keyEvent)) {  // Redo note edit
            NoteRectangle.editAction(NoteRectangle.EditAction.REDO);
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
            case N -> toggleEditNotesButton();
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
                if (octaveNum < 9) {
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
        if (!isEverythingReady || NoteRectangle.isEditing) {
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
     * @return String representing the save destination.
     */
    private String getSaveDestination(boolean forceChooseFile) {
        String saveDest, saveName;

        // Check if there already exist a place to save
        if (audtFilePath == null || forceChooseFile) {
            log(Level.FINE, "AUDT file destination not yet set; asking now");

            // Get current window
            Window window = rootPane.getScene().getWindow();

            // Ask user to choose a file
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showSaveDialog(window);

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
     * Helper method that handles the compressed MP3 bytes.
     *
     * @param ffmpegHandler      FFmpeg handler.
     * @param compressedMP3Bytes Compressed MP3 bytes.
     * @return A pair. The first value is a byte array, representing the raw MP3 bytes. The second
     * is a <code>File</code> object pointing to a WAV file representing the audio data.
     * @throws IOException If the auxiliary MP3 file does not exist, or if the decompression process
     *                     fails.
     */
    private Pair<Byte[], File> handleCompressedMP3Bytes(
            FFmpegHandler ffmpegHandler, byte[] compressedMP3Bytes
    ) throws IOException {
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
        auxiliaryWAVFilePath = ffmpegHandler.convertAudio(auxiliaryMP3File, auxiliaryWAVFilePath);

        // Read the newly created WAV files
        File auxiliaryWAVFile = new File(auxiliaryWAVFilePath);

        // Delete the original MP3 file
        IOMethods.delete(auxiliaryMP3File);

        // Return needed information
        return new Pair<>(TypeConversionUtils.toByteArray(rawMP3Bytes), auxiliaryWAVFile);
    }
}
