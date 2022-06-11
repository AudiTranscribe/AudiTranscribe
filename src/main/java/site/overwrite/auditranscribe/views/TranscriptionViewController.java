/*
 * TranscriptionViewController.java
 *
 * Created on 2022-02-12
 * Updated on 2022-06-11
 *
 * Description: Contains the transcription view's controller class.
 */

package site.overwrite.auditranscribe.views;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import site.overwrite.auditranscribe.audio.AudioProcessingMode;
import site.overwrite.auditranscribe.audio.ffmpeg.AudioConverter;
import site.overwrite.auditranscribe.bpm_estimation.BPMEstimator;
import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.LZ4;
import site.overwrite.auditranscribe.misc.CustomTask;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.io.json_files.file_classes.SettingsFile;
import site.overwrite.auditranscribe.misc.Theme;
import site.overwrite.auditranscribe.notes.NotePlayer;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.AudioDataObject;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.GUIDataObject;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.ProjectDataObject;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.QTransformDataObject;
import site.overwrite.auditranscribe.io.db.ProjectsDB;
import site.overwrite.auditranscribe.plotting.PlottingHelpers;
import site.overwrite.auditranscribe.plotting.PlottingStuffHandler;
import site.overwrite.auditranscribe.spectrogram.*;
import site.overwrite.auditranscribe.utils.*;
import site.overwrite.auditranscribe.views.helpers.MouseHandler;
import site.overwrite.auditranscribe.views.helpers.Popups;
import site.overwrite.auditranscribe.views.helpers.ProjectIOHandlers;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TranscriptionViewController implements Initializable {
    // Constants
    final Pair<Integer, Integer> BPM_RANGE = new Pair<>(1, 512);  // In the format [min, max]
    final Pair<Double, Double> OFFSET_RANGE = new Pair<>(-15., 15.);  // In the format [min, max]

    public final double SPECTROGRAM_ZOOM_SCALE_X = 2;
    public final double SPECTROGRAM_ZOOM_SCALE_Y = 5;

    public final int PX_PER_SECOND = 120;
    final int BINS_PER_OCTAVE = 60;
    final int SPECTROGRAM_HOP_LENGTH = 1024;  // Needs to be a power of 2
    final double NUM_PX_PER_OCTAVE = 72;

    final int MIN_NOTE_NUMBER = 0;  // C0
    final int MAX_NOTE_NUMBER = 119;  // B9

    final long UPDATE_PLAYBACK_SCHEDULER_PERIOD = 50;  // In milliseconds

    final boolean USE_FANCY_SHARPS_FOR_NOTE_LABELS = true;

    final double VOLUME_VALUE_DELTA_ON_KEY_PRESS = 0.05;

    final String NOTE_PLAYING_INSTRUMENT = "PIANO";
    final int MIDI_CHANNEL_NUM = 0;
    final int NOTE_PLAYING_ON_VELOCITY = 96;  // Within the range [0, 127]
    final int NOTE_PLAYING_OFF_VELOCITY = 10;   // Within the range [0, 127]
    final long NOTE_PLAYING_ON_DURATION = 75;  // In milliseconds
    final long NOTE_PLAYING_OFF_DURATION = 925;  // In milliseconds

    final KeyCodeCombination NEW_PROJECT_COMBINATION = new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN);
    final KeyCodeCombination OPEN_PROJECT_COMBINATION = new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN);
    final KeyCodeCombination SAVE_PROJECT_COMBINATION = new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN);

    // File-Savable Attributes
    private double sampleRate;  // Sample rate of the audio

    private int musicKeyIndex = 0;  // Index of the music key chosen, according to the `MUSIC_KEYS` array
    private int timeSignatureIndex = 0;
    private double bpm = 120;
    private double offset = 0.;
    private double volume = 0.5;
    private double audioDuration = 0;  // Will be updated upon scene initialization
    private double currTime = 0;

    // Other attributes
    Stage mainStage;
    MainViewController mainViewController;
    SettingsFile settingsFile;
    Theme theme;

    NotePlayer notePlayer;

    private boolean isEverythingReady = false;

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private ProjectsDB projectsDB;
    private List<Audio> allAudio;

    private String audtFilePath;
    private String audtFileName;
    private String audioFileName;
    private Audio audio;

    private byte[] compressedMP3Bytes;

    private byte[] qTransformBytes;  // LZ4 compressed version
    private double minQTransformMagnitude;
    private double maxQTransformMagnitude;

    private String musicKey = "C Major";
    private int beatsPerBar = 4;
    private boolean isPaused = true;
    private boolean isMuted = false;
    private boolean scrollToPlayhead = false;

    private double finalWidth;
    private double finalHeight;

    private int octaveNum = 4;

    private Label[] noteLabels;
    private Line[] beatLines;
    private StackPane[] barNumberEllipses;
    private Line playheadLine;

    Queue<CustomTask<?>> ongoingTasks = new LinkedList<>();
    ScheduledExecutorService scheduler;

    // FXML Elements
    // Menu bar
    @FXML
    private MenuBar menuBar;

    @FXML
    private MenuItem newProjectMenuItem, openProjectMenuItem, saveProjectMenuItem, saveAsMenuItem, preferencesMenuItem,
            aboutMenuItem;

    // Main elements
    @FXML
    private VBox masterVBox;

    @FXML
    private AnchorPane rootPane, mainPane;

    // Top HBox
    @FXML
    private Button newProjectButton, openProjectButton, saveProjectButton;

    @FXML
    private ImageView newProjectButtonImage, openProjectButtonImage, saveProjectButtonImage;

    @FXML
    private ChoiceBox<String> musicKeyChoice, timeSignatureChoice;

    @FXML
    private Spinner<Double> bpmSpinner, offsetSpinner;

    @FXML
    private HBox progressBarHBox;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label progressLabel;

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
    private Button playButton, stopButton, playSkipBackButton, playSkipForwardButton, scrollButton, volumeButton;

    @FXML
    private ImageView playButtonImage, stopButtonImage, playSkipBackButtonImage, playSkipForwardButtonImage,
            scrollButtonImage, volumeButtonImage;

    @FXML
    private Slider volumeSlider;

    // Initialization method
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Make macOS systems use the system menu bar
        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac")) {
            menuBar.useSystemMenuBarProperty().set(true);
        }

        // Set the width and height of the root pane
        masterVBox.prefWidthProperty().bind(rootPane.widthProperty());
        masterVBox.prefHeightProperty().bind(rootPane.heightProperty());

        mainPane.prefWidthProperty().bind(rootPane.widthProperty());
        mainPane.prefHeightProperty().bind(rootPane.heightProperty().subtract(menuBar.heightProperty()));

        // Update any attributes
        try {
            notePlayer = new NotePlayer(NOTE_PLAYING_INSTRUMENT, MIDI_CHANNEL_NUM);
        } catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        }

        // Update spinners' ranges
        SpinnerValueFactory.DoubleSpinnerValueFactory bpmSpinnerFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(
                        BPM_RANGE.getValue0(), BPM_RANGE.getValue1(), 120, 0.1
                );
        SpinnerValueFactory.DoubleSpinnerValueFactory offsetSpinnerFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(
                        OFFSET_RANGE.getValue0(), OFFSET_RANGE.getValue1(), 0, 0.01
                );

        bpmSpinner.setValueFactory(bpmSpinnerFactory);
        offsetSpinner.setValueFactory(offsetSpinnerFactory);

        // Set the choice boxes' choices
        for (String musicKey : MusicUtils.MUSIC_KEYS) musicKeyChoice.getItems().add(musicKey);
        for (String timeSignature : MusicUtils.TIME_SIGNATURES) timeSignatureChoice.getItems().add(timeSignature);

        // Set methods on spinners
        bpmSpinner.valueProperty().addListener((observable, oldValue, newValue) -> updateBPMValue(newValue));
        offsetSpinner.valueProperty().addListener(((observable, oldValue, newValue) -> updateOffsetValue(newValue)));

        bpmSpinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {  // Lost focus
                updateBPMValue(bpmSpinner.getValue());
            }
        });

        offsetSpinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {  // Lost focus
                updateOffsetValue(offsetSpinner.getValue());
            }
        });

        // Set methods on choice box fields
        musicKeyChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            logger.log(Level.FINE, "Changed music key from " + oldValue + " to " + newValue);

            // Update note pane and note labels
            if (isEverythingReady) {
                noteLabels = PlottingStuffHandler.addNoteLabels(
                        notePane, noteLabels, newValue, finalHeight, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER,
                        USE_FANCY_SHARPS_FOR_NOTE_LABELS
                );
            }

            // Update the music key value and music key index
            musicKey = newValue;
            musicKeyIndex = ArrayUtils.findIndex(MusicUtils.MUSIC_KEYS, newValue);
        });

        timeSignatureChoice.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    logger.log(Level.FINE, "Changed time signature from " + oldValue + " to " + newValue);

                    // Get the old and new beats per bar
                    int oldBeatsPerBar = 0;
                    if (oldValue != null) {
                        oldBeatsPerBar = MusicUtils.TIME_SIGNATURE_TO_BEATS_PER_BAR.get(oldValue);
                    }
                    int newBeatsPerBar = MusicUtils.TIME_SIGNATURE_TO_BEATS_PER_BAR.get(newValue);

                    // Update the beat lines and bar number ellipses, if the spectrogram is ready
                    if (isEverythingReady) {
                        beatLines = PlottingStuffHandler.updateBeatLines(
                                spectrogramPaneAnchor, beatLines, audioDuration, bpm, bpm, offset, offset, finalHeight,
                                oldBeatsPerBar, newBeatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
                        );

                        barNumberEllipses = PlottingStuffHandler.updateBarNumberEllipses(
                                barNumberPane, barNumberEllipses, audioDuration, bpm, bpm, offset, offset,
                                barNumberPane.getPrefHeight(), oldBeatsPerBar, newBeatsPerBar, PX_PER_SECOND,
                                SPECTROGRAM_ZOOM_SCALE_X
                        );
                    }

                    // Update the time signature index
                    timeSignatureIndex = ArrayUtils.findIndex(MusicUtils.TIME_SIGNATURES, newValue);

                    // Update the beats per bar
                    beatsPerBar = newBeatsPerBar;
                });

        // Add methods to buttons
        newProjectButton.setOnAction(this::handleNewProject);

        openProjectButton.setOnAction(this::handleOpenProject);

        saveProjectButton.setOnAction(event -> handleSavingProject(false));

        playButton.setOnAction(event -> togglePlayButton());

        stopButton.setOnAction(event -> {
            logger.log(Level.FINE, "Pressed stop button");

            // First stop the audio
            try {
                audio.stop();
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }

            // Then update the timings shown on the GUI
            try {
                seekToTime(0);
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }

            // Finally, toggle the paused flag
            isPaused = togglePaused(false);
        });

        playSkipBackButton.setOnAction(event -> {
            logger.log(Level.FINE, "Pressed skip back button");

            // Seek to the start of the audio
            try {
                seekToTime(0);
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }

            // Pause the audio
            isPaused = togglePaused(false);
        });

        playSkipForwardButton.setOnAction(event -> {
            logger.log(Level.FINE, "Pressed skip forward button");

            // Seek to the end of the audio
            try {
                seekToTime(audioDuration);
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }

            // Force the audio to play at the end
            // (This is to avoid a nasty seek to end issue where user needs to click on play button twice)
            isPaused = togglePaused(true);
        });

        volumeButton.setOnAction(event -> toggleMuteButton());

        scrollButton.setOnAction(event -> toggleScrollButton());

        // Set spectrogram pane mouse event handler
        spectrogramPaneAnchor.addEventHandler(MouseEvent.ANY, new MouseHandler(event -> {
        }, event -> {
            if (isEverythingReady) {
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
                            clickY, UnitConversionUtils.noteNumberToFreq(MIN_NOTE_NUMBER),
                            UnitConversionUtils.noteNumberToFreq(MAX_NOTE_NUMBER), spectrogramPaneAnchor.getHeight()
                    );

                    // Now estimate the note number
                    int estimatedNoteNum = (int) Math.round(UnitConversionUtils.freqToNoteNumber(estimatedFreq));

                    // Play the note
                    try {
                        logger.log(Level.FINE, "Playing " + UnitConversionUtils.noteNumberToNote(
                                estimatedNoteNum, false, false
                        ));
                        notePlayer.playNoteForDuration(
                                estimatedNoteNum, NOTE_PLAYING_ON_VELOCITY, NOTE_PLAYING_OFF_VELOCITY,
                                NOTE_PLAYING_ON_DURATION, NOTE_PLAYING_OFF_DURATION
                        );
                    } catch (InvalidParameterException ignored) {
                    }
                }
            }
        }));

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
                try {
                    seekToTime(seekTime);
                } catch (InvalidObjectException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Add methods to menu items
        newProjectMenuItem.setOnAction(this::handleNewProject);

        openProjectMenuItem.setOnAction(this::handleOpenProject);

        saveProjectMenuItem.setOnAction(event -> handleSavingProject(false));

        saveAsMenuItem.setOnAction(event -> handleSavingProject(true));

        preferencesMenuItem.setOnAction(actionEvent -> PreferencesViewController.showPreferencesWindow(settingsFile));

        aboutMenuItem.setOnAction(actionEvent -> AboutViewController.showAboutWindow(settingsFile));

        // Get the projects database
        try {
            projectsDB = new ProjectsDB();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Setter methods
    public void setSettingsFile(SettingsFile settingsFile) {
        this.settingsFile = settingsFile;
    }

    // Public methods

    /**
     * Method that sets the theme for the scene.
     */
    public void setThemeOnScene() {
        // Get the theme
        theme = Theme.values()[settingsFile.data.themeEnumOrdinal];

        // Set stylesheets
        rootPane.getStylesheets().clear();  // Reset the stylesheets first before adding new ones

        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/base.css"));
        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/" + theme.cssFile));

        // Set graphics
        newProjectButtonImage.setImage(new Image(IOMethods.getFileURLAsString(
                "images/icons/PNGs/" + theme.shortName + "/create.png"
        )));
        openProjectButtonImage.setImage(new Image(IOMethods.getFileURLAsString(
                "images/icons/PNGs/" + theme.shortName + "/folder-open.png"
        )));
        saveProjectButtonImage.setImage(new Image(IOMethods.getFileURLAsString(
                "images/icons/PNGs/" + theme.shortName + "/save.png"
        )));

        playButtonImage.setImage(new Image(IOMethods.getFileURLAsString(
                "images/icons/PNGs/" + theme.shortName + "/play.png"
        )));
        stopButtonImage.setImage(new Image(IOMethods.getFileURLAsString(
                "images/icons/PNGs/" + theme.shortName + "/stop.png"
        )));
        playSkipBackButtonImage.setImage(new Image(IOMethods.getFileURLAsString(
                "images/icons/PNGs/" + theme.shortName + "/play-skip-back.png"
        )));
        playSkipForwardButtonImage.setImage(new Image(IOMethods.getFileURLAsString(
                "images/icons/PNGs/" + theme.shortName + "/play-skip-forward.png"
        )));
        scrollButtonImage.setImage(new Image(IOMethods.getFileURLAsString(
                "images/icons/PNGs/" + theme.shortName + "/footsteps-outline.png"
        )));
        volumeButtonImage.setImage(new Image(IOMethods.getFileURLAsString(
                "images/icons/PNGs/" + theme.shortName + "/volume-high.png"
        )));
    }

    /**
     * Method that sets the volume slider's CSS.
     */
    public void updateVolumeSliderCSS() {
        // Generate the style of the volume slider for the current volume value
        String style = String.format(
                "-fx-background-color: linear-gradient(" +
                        "to right, -slider-filled-colour %f%%, -slider-unfilled-colour %f%%" +
                        ");",
                volume * 100, volume * 100);

        // Apply the style to the volume slider's track (if available)
        StackPane track = (StackPane) volumeSlider.lookup(".track");
        if (track != null) track.setStyle(style);
    }

    /**
     * Method that finishes the setting up of the transcription view controller.<br>
     * Note that this method has to be called <b>last</b>, after all other spectrogram things have
     * been set up.
     *
     * @param mainStage          Main stage.
     * @param allAudio           List of all opened <code>Audio</code> objects.
     * @param mainViewController Controller object of the main class.
     */
    public void finishSetup(Stage mainStage, List<Audio> allAudio, MainViewController mainViewController) {
        // Update attributes
        this.mainStage = mainStage;
        this.mainViewController = mainViewController;
        this.allAudio = allAudio;

        // Append the current audio to the list of all audio
        allAudio.add(audio);

        // Set choices
        musicKeyChoice.setValue(MusicUtils.MUSIC_KEYS[musicKeyIndex]);
        timeSignatureChoice.setValue(MusicUtils.TIME_SIGNATURES[timeSignatureIndex]);

        // Update spinners' initial values
        updateBPMValue(bpm, true);
        updateOffsetValue(offset, true);

        SpinnerValueFactory.DoubleSpinnerValueFactory bpmSpinnerFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(
                        BPM_RANGE.getValue0(), BPM_RANGE.getValue1(), bpm, 0.1
                );
        SpinnerValueFactory.DoubleSpinnerValueFactory offsetSpinnerFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(
                        OFFSET_RANGE.getValue0(), OFFSET_RANGE.getValue1(), offset, 0.01
                );

        bpmSpinner.setValueFactory(bpmSpinnerFactory);
        offsetSpinner.setValueFactory(offsetSpinnerFactory);

        // Set method on the volume slider
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Update the volume value
            volume = newValue.doubleValue();

            // Change the icon of the volume button from mute to non-mute
            if (isMuted) {
                volumeButtonImage.setImage(
                        new Image(IOMethods.getFileURLAsString(
                                "images/icons/PNGs/" + theme.shortName + "/volume-high.png"
                        ))
                );
                isMuted = false;
            }

            // Update audio volume
            try {
                audio.setPlaybackVolume(volume);
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }

            // Update CSS
            updateVolumeSliderCSS();

            logger.log(Level.FINE, "Changed volume from " + oldValue + " to " + newValue);
        });

        // Update labels
        totalTimeLabel.setText(UnitConversionUtils.secondsToTimeString(audioDuration));
        currTimeLabel.setText(UnitConversionUtils.secondsToTimeString(currTime));

        // Set keyboard button press/release methods
        mainPane.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::keyPressEventHandler);
        mainPane.getScene().addEventFilter(KeyEvent.KEY_RELEASED, this::keyReleasedEventHandler);

        // Report that the transcription view is ready to be shown
        logger.log(Level.INFO, "Transcription view ready to be shown");
    }

    /**
     * Method that makes the transcription view controller use the existing project data, that was
     * supposedly read from a file.
     *
     * @param audtFilePath <b>Absolute</b> path to the file that contained the data.
     * @param audtFileName The name of the AUDT file.
     * @param projectData  The project data.
     */
    public void useExistingData(
            String audtFilePath, String audtFileName, ProjectDataObject projectData
    ) {
        // Set up GUI data
        musicKeyIndex = projectData.guiData.musicKeyIndex;
        timeSignatureIndex = projectData.guiData.timeSignatureIndex;
        bpm = projectData.guiData.bpm;
        offset = projectData.guiData.offsetSeconds;
        volume = projectData.guiData.playbackVolume;
        currTime = projectData.guiData.currTimeInMS / 1000.;

        // Set the AudiTranscribe file's file path and file name
        this.audtFilePath = audtFilePath;
        this.audtFileName = audtFileName;

        // Set up Q-Transform data and audio data
        try {
            setAudioAndSpectrogramData(projectData.qTransformData, projectData.audioData);
        } catch (IOException | UnsupportedAudioFileException e) {
            Popups.showExceptionAlert(
                    "Error loading audio data.",
                    "An error occurred when loading the audio data. Does the audio file " +
                            "still exist at the original location?",
                    e
            );
            throw new RuntimeException(e);
        }

        // Update music key and beats per bar
        musicKey = MusicUtils.MUSIC_KEYS[musicKeyIndex];
        beatsPerBar = MusicUtils.TIME_SIGNATURE_TO_BEATS_PER_BAR.get(MusicUtils.TIME_SIGNATURES[timeSignatureIndex]);

        // Attempt to add this project to the projects' database
        try {
            if (!projectsDB.checkIfProjectExists(audtFilePath)) {
                // Insert the record into the database
                projectsDB.insertProjectRecord(audtFilePath, audtFileName);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method that sets the audio and spectrogram data for the transcription view controller.<br>
     * This method uses the actual audio file to do the setting of the data.
     *
     * @param audioObj An <code>Audio</code> object that contains audio data.
     */
    public void setAudioAndSpectrogramData(Audio audioObj) {
        // Set attributes
        audio = audioObj;
        audioFileName = audioObj.getAudioFileName();
        audioDuration = audio.getDuration();
        sampleRate = audio.getSampleRate();

        // Generate spectrogram image based on newly generated magnitude data
        CustomTask<WritableImage> spectrogramTask = new CustomTask<>("Generate Spectrogram") {
            @Override
            protected WritableImage call() throws IOException {
                // Define a spectrogram object
                Spectrogram spectrogram = new Spectrogram(
                        audio, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER, BINS_PER_OCTAVE, SPECTROGRAM_HOP_LENGTH, PX_PER_SECOND,
                        NUM_PX_PER_OCTAVE, this
                );

                // Obtain the raw spectrogram magnitudes
                double[][] magnitudes = spectrogram.getSpectrogramMagnitudes(
                        WindowFunction.values()[settingsFile.data.windowFunctionEnumOrdinal]
                );

                // Update attributes
                this.setMessage("Compressing spectrogram data...");
                Triplet<Byte[], Double, Double> conversionTuple =
                        QTransformDataObject.qTransformMagnitudesToByteData(magnitudes, this);

                qTransformBytes = TypeConversionUtils.toByteArray(conversionTuple.getValue0());
                minQTransformMagnitude = conversionTuple.getValue1();
                maxQTransformMagnitude = conversionTuple.getValue2();

                // Generate spectrogram
                return spectrogram.generateSpectrogram(
                        magnitudes,
                        ColourScale.values()[settingsFile.data.colourScaleEnumOrdinal]
                );
            }
        };

        // Estimate BPM based on the audio samples
        CustomTask<Double> bpmTask = new CustomTask<>("Estimate BPM") {
            @Override
            protected Double call() {
                return BPMEstimator.estimate(audio.getMonoSamples(), sampleRate).get(0);  // Assume we take fist element
            }
        };

        // Set up tasks
        setupSpectrogramTask(spectrogramTask, "Generating spectrogram...");
        setupBPMEstimationTask(bpmTask);

        // Start the tasks
        startTasks(spectrogramTask, bpmTask);
    }

    /**
     * Method that sets the audio and spectrogram data for the transcription view controller.<br>
     * This method uses existing data (provided in <code>qTransformData</code> and
     * <code>audioData</code>) to do the setting of the data.
     *
     * @param qTransformData The Q-Transform data that will be used to set the spectrogram data.
     * @param audioData      The audio data that will be used in both the spectrogram data and
     *                       the audio data.
     * @throws UnsupportedAudioFileException If the audio file path that was provided in
     *                                       <code>audioData</code> points to an invalid audio file.
     * @throws IOException                   If the audio file path that was provided in
     *                                       <code>audioData</code> points to a file that is invalid
     *                                       (or does not exist).
     */
    public void setAudioAndSpectrogramData(
            QTransformDataObject qTransformData, AudioDataObject audioData
    ) throws UnsupportedAudioFileException, IOException {
        // Set attributes
        compressedMP3Bytes = audioData.compressedMP3Bytes;
        sampleRate = audioData.sampleRate;
        audioDuration = audioData.totalDurationInMS / 1000.;
        audioFileName = audioData.audioFileName;

        qTransformBytes = qTransformData.qTransformBytes;
        minQTransformMagnitude = qTransformData.minMagnitude;
        maxQTransformMagnitude = qTransformData.maxMagnitude;

        // Decompress the MP3 bytes
        byte[] rawMP3Bytes = LZ4.lz4Decompress(compressedMP3Bytes);

        // Ensure that the temporary directory exists
        IOMethods.createFolder(IOConstants.TEMP_FOLDER);
        logger.log(Level.FINE, "Temporary folder created: " + IOConstants.TEMP_FOLDER);

        // Create an empty MP3 file in the temporary directory
        File auxiliaryMP3File = new File(IOConstants.TEMP_FOLDER + "temp-1.mp3");
        IOMethods.createFile(auxiliaryMP3File.getAbsolutePath());

        // Write the raw MP3 bytes into a temporary file
        FileOutputStream fos = new FileOutputStream(auxiliaryMP3File);
        fos.write(rawMP3Bytes);
        fos.close();

        // Define a new audio converter
        AudioConverter audioConverter = new AudioConverter(settingsFile.data.ffmpegInstallationPath);

        // Generate the output path to the MP3 file
        String outputPath = IOConstants.TEMP_FOLDER + "temp-2.wav";

        // Convert the auxiliary MP3 file to a WAV file
        outputPath = audioConverter.convertAudio(auxiliaryMP3File, outputPath);

        // Read the newly created WAV file
        File auxiliaryWAVFile = new File(outputPath);

        // Create the `Audio` object
        audio = new Audio(
                auxiliaryWAVFile, audioFileName, AudioProcessingMode.PLAYBACK_ONLY
        );

        // Update the raw MP3 bytes of the audio object
        audio.setRawMP3Bytes(rawMP3Bytes);  // This is to reduce the time needed to save the file later

        // Delete the temporary files
        IOMethods.deleteFile(auxiliaryMP3File.getAbsolutePath());
        IOMethods.deleteFile(auxiliaryWAVFile.getAbsolutePath());

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
                        ColourScale.values()[settingsFile.data.colourScaleEnumOrdinal]
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
            // If the `newPoxX` is within the last 'half width' of the entire spectrogram area, keep the scrolling to
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
        try {
            audio.stop();
        } catch (InvalidObjectException e) {
            throw new RuntimeException(e);
        }

        // Shutdown the scheduler
        scheduler.shutdown();
    }

    // Private methods

    /**
     * Helper method that seeks to the specified time.
     *
     * @param seekTime Time to seek to.
     * @throws InvalidObjectException If the <code>MediaPlayer</code> object was not defined in the
     *                                audio object.
     */
    private void seekToTime(double seekTime) throws InvalidObjectException {
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

        // Update the start time of the audio
        // (Do this so that when the player resumes out of a stop state it will start here)
        audio.setAudioStartTime(seekTime);

        // Set the playback time
        // (We do this after updating start time to avoid pesky seeking issues)
        audio.setAudioPlaybackTime(seekTime);

        // Update the current time and current time label
        currTime = seekTime;
        currTimeLabel.setText(UnitConversionUtils.secondsToTimeString(seekTime));

        // Update coloured progress pane and playhead line
        double newXPos = seekTime * PX_PER_SECOND * SPECTROGRAM_ZOOM_SCALE_X;

        colouredProgressPane.setPrefWidth(newXPos);
        if (isEverythingReady) PlottingStuffHandler.updatePlayheadLine(playheadLine, newXPos);

        logger.log(Level.FINE, "Seeked to " + seekTime + " seconds");
    }

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

        // Get the current window
        Window window = rootPane.getScene().getWindow();

        // Get user to select an audio file
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Audio files (*.wav, *.mp3, *.flac, *.aif, *.aiff)",
                "*.wav", "*.mp3", "*.flac", "*.aif", "*.aiff"
        );
        File file = ProjectIOHandlers.getFileFromFileDialog(window, extFilter);

        // If a file was selected, stop the audio completely
        if (file != null) {
            try {
                audio.stop();
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }
        }

        // Create the new project
        ProjectIOHandlers.newProject(
                mainStage, (Stage) window, file, settingsFile, allAudio, mainViewController
        );
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

        // Get the current window
        Window window = rootPane.getScene().getWindow();

        // Get user to select an AUDT file
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "AudiTranscribe files (*.audt)", "*.audt"
        );
        File file = ProjectIOHandlers.getFileFromFileDialog(window, extFilter);

        // If a file was selected, stop the audio completely
        if (file != null) {
            try {
                audio.stop();
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }
        }

        // Open the existing project
        ProjectIOHandlers.openProject(
                mainStage, (Stage) window, file, settingsFile, allAudio, mainViewController
        );
    }

    /**
     * Helper method that handles the saving of the project.
     *
     * @param forceChooseFile Boolean whether to force the user to choose a file.
     */
    private void handleSavingProject(boolean forceChooseFile) {
        // Do not do anything if the button is disabled
        if (saveProjectButton.isDisabled()) return;

        // Allow user to select save location
        String saveDest, saveName;

        if (audtFilePath == null || forceChooseFile) {
            logger.log(Level.FINE, "AUDT file destination not yet set; asking now");

            // Get current window
            Window window = rootPane.getScene().getWindow();

            // Ask user to choose a file
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showSaveDialog(window);

            // If operation was cancelled return
            if (file == null) return;

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

            logger.log(Level.FINE, "AUDT file destination set to " + saveDest);
        } else {
            // Use the existing file path and file name
            saveDest = audtFilePath;
            saveName = audtFileName;

            logger.log(Level.FINE, "Saving " + saveName + " to " + saveDest);
        }

        // Set up task to run in alternate thread
        String finalSaveDest = saveDest;
        CustomTask<Void> task = new CustomTask<>("Save Project") {
            @Override
            protected Void call() throws Exception {
                // Compress the raw MP3 bytes
                try {
                    compressedMP3Bytes = LZ4.lz4Compress(
                            audio.wavBytesToMP3Bytes(settingsFile.data.ffmpegInstallationPath), this
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // Package all the current data into a `ProjectDataObject`
                logger.log(Level.INFO, "Packaging data for saving");
                QTransformDataObject qTransformData = new QTransformDataObject(
                        qTransformBytes, minQTransformMagnitude, maxQTransformMagnitude
                );
                AudioDataObject audioData = new AudioDataObject(
                        compressedMP3Bytes, sampleRate, (int) audioDuration * 1000,
                        audioFileName);
                GUIDataObject guiData = new GUIDataObject(
                        musicKeyIndex, timeSignatureIndex, bpm, offset, volume,
                        (int) currTime * 1000
                );

                ProjectDataObject projectData = new ProjectDataObject(
                        qTransformData, audioData, guiData
                );

                // Save the project
                ProjectIOHandlers.saveProject(finalSaveDest, projectData);
                logger.log(Level.INFO, "File saved");
                return null;
            }
        };

        // Link the progress of the task with the progress bar
        progressBarHBox.setVisible(true);
        progressBar.progressProperty().bind(task.progressProperty());
        progressLabel.setText("Saving file...");

        // Methods to run after task succeeded
        task.setOnSucceeded(event -> {
            // Update the project file list
            try {
                if (!projectsDB.checkIfProjectExists(audtFilePath)) {
                    // Insert the record into the database
                    projectsDB.insertProjectRecord(audtFilePath, audtFileName);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            // Hide the progress box
            progressBarHBox.setVisible(false);
        });

        // Start new thread to save the file
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Helper method that toggles the paused state.
     *
     * @param isPaused Old paused state.
     * @return New paused state.
     */
    private boolean togglePaused(boolean isPaused) {
        if (isPaused) {
            // Change the icon of the play button from the play icon to the paused icon
            playButtonImage.setImage(new Image(IOMethods.getFileURLAsString(
                    "images/icons/PNGs/" + theme.shortName + "/pause.png"
            )));

            // Unpause the audio (i.e. play the audio)
            try {
                audio.play();
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }

        } else {
            // Change the icon of the play button from the paused icon to the play icon
            playButtonImage.setImage(new Image(IOMethods.getFileURLAsString(
                    "images/icons/PNGs/" + theme.shortName + "/play.png"
            )));

            // Pause the audio
            try {
                audio.pause();
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }
        }

        // Return the toggled version of the `isPaused` flag
        logger.log(Level.FINE, "Toggled pause state from " + isPaused + " to " + !isPaused);
        return !isPaused;
    }

    /**
     * Helper method that helps update the needed things when the BPM value is to be updated.
     *
     * @param newBPM      New BPM value.
     * @param forceUpdate Whether to force an update to the BPM value.
     */
    private void updateBPMValue(double newBPM, boolean forceUpdate) {
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
                    barNumberPane.getPrefHeight(), beatsPerBar, beatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
            );
        }

        // Update the BPM value
        if (!forceUpdate) {
            logger.log(Level.FINE, "Updated BPM value from " + bpm + " to " + newBPM);
        } else {
            logger.log(Level.FINE, "Force update BPM value to " + newBPM);
        }
        bpm = newBPM;
    }

    /**
     * Helper method that helps update the needed things when the BPM value is to be updated.
     *
     * @param newBPM New BPM value.
     */
    private void updateBPMValue(double newBPM) {
        updateBPMValue(newBPM, false);
    }

    /**
     * Helper method that helps update the needed things when the offset value is to be updated.
     *
     * @param newOffset   New offset value.
     * @param forceUpdate Whether to force an update to the offset value.
     */
    private void updateOffsetValue(double newOffset, boolean forceUpdate) {
        // Get the previous offset value
        double oldOffset = forceUpdate ? OFFSET_RANGE.getValue0() - 1 : offset;  // Make it 1 less than permitted

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
                    barNumberPane.getPrefHeight(), beatsPerBar, beatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
            );
        }

        // Update the offset value
        if (!forceUpdate) {
            logger.log(Level.FINE, "Updated offset value from " + offset + " to " + newOffset);
        } else {
            logger.log(Level.FINE, "Force update offset value to " + newOffset);
        }
        offset = newOffset;
    }

    /**
     * Helper method that helps update the needed things when the offset value is to be updated.
     *
     * @param newOffset New offset value.
     */
    private void updateOffsetValue(double newOffset) {
        updateOffsetValue(newOffset, false);
    }

    /**
     * Helper method that starts the spectrogram generation task.
     *
     * @param task    The task to start.
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
            colouredProgressPane.setPrefWidth(0);

            // Set scrolling for panes
            leftPane.vvalueProperty().bindBidirectional(spectrogramPane.vvalueProperty());
            bottomPane.hvalueProperty().bindBidirectional(spectrogramPane.hvalueProperty());

            // Add the playhead line
            playheadLine = PlottingStuffHandler.createPlayheadLine(finalHeight);
            spectrogramPaneAnchor.getChildren().add(playheadLine);

            // Create a constantly-executing service
            scheduler = Executors.newScheduledThreadPool(0, runnable -> {
                Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                thread.setDaemon(true);  // Make it so that it can shut down gracefully by placing it in background
                return thread;
            });
            scheduler.scheduleAtFixedRate(() -> {
                // Nothing really changes if the audio is paused
                if (!isPaused) {
                    // Get the current audio time
                    try {
                        currTime = audio.getCurrAudioTime();
                    } catch (InvalidObjectException e) {
                        throw new RuntimeException(e);
                    }

                    // Update the current time label
                    Platform.runLater(() -> currTimeLabel.setText(UnitConversionUtils.secondsToTimeString(currTime)));

                    // Update coloured progress pane and playhead line
                    double newPosX = currTime * PX_PER_SECOND * SPECTROGRAM_ZOOM_SCALE_X;
                    colouredProgressPane.setPrefWidth(newPosX);
                    PlottingStuffHandler.updatePlayheadLine(playheadLine, newPosX);

                    // Check if the current time has exceeded and is not paused
                    if (currTime >= audioDuration) {
                        logger.log(Level.FINE, "Playback reached end of audio, will start from beginning upon play");
                        // Pause the audio
                        isPaused = togglePaused(false);

                        // Specially update the start time to 0
                        // (Because the `seekToTime` method would have set it to the end, which is not what we want)
                        try {
                            audio.setAudioStartTime(0);
                        } catch (InvalidObjectException e) {
                            throw new RuntimeException(e);
                        }

                        // We need to do this so that the status is set to paused
                        try {
                            audio.stop();
                            audio.pause();
                        } catch (InvalidObjectException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    // Update scrolling
                    if (scrollToPlayhead) {
                        updateScrollPosition(newPosX, spectrogramPane.getWidth());
                    }
                }
            }, 0, UPDATE_PLAYBACK_SCHEDULER_PERIOD, TimeUnit.MILLISECONDS);

            // Set image on the spectrogram area
            spectrogramImage.setFitHeight(finalWidth);
            spectrogramImage.setFitWidth(finalHeight);
            spectrogramImage.setImage(image);

            // Add note labels and note lines
            noteLabels = PlottingStuffHandler.addNoteLabels(
                    notePane, noteLabels, musicKey, finalHeight, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER,
                    USE_FANCY_SHARPS_FOR_NOTE_LABELS
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

            // Set the current octave rectangle
            currentOctaveRectangle = PlottingStuffHandler.addCurrentOctaveRectangle(
                    notePane, finalHeight, octaveNum, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER
            );

            // Resize spectrogram image pane
            // (We do this at the end to ensure that the image is properly placed)
            spectrogramImage.setFitWidth(finalWidth);
            spectrogramImage.setFitHeight(finalHeight);

            // Resize spectrogram pane
            spectrogramPane.setPrefWidth(finalWidth);
            spectrogramPane.setPrefHeight(finalHeight);

            // Settle layout of the main pane
            mainPane.layout();

            // Show the spectrogram from the middle
            spectrogramPane.setVvalue(0.5);

            // Update playhead position
            try {
                seekToTime(currTime);
                updateScrollPosition(
                        currTime * PX_PER_SECOND * SPECTROGRAM_ZOOM_SCALE_X,
                        spectrogramPane.getWidth()
                );
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }

            // Update volume slider
            volumeSlider.setValue(volume);
            updateVolumeSliderCSS();

            // Ensure main pane is in focus
            rootPane.requestFocus();

            // Mark the task as completed and report that the transcription view is ready to be shown
            markTaskAsCompleted(task);
            logger.log(Level.INFO, "Spectrogram for " + audioFileName + " ready to be shown");
        });
    }

    /**
     * Helper method that starts the BPM estimation task.
     *
     * @param task The task to start.
     */
    private void setupBPMEstimationTask(CustomTask<Double> task) {
        // Set the task's message
        task.setMessage("Estimating tempo...");

        // Set task completion listener
        task.setOnSucceeded(event -> {
            // Update the BPM value
            updateBPMValue(MathUtils.round(task.getValue(), 1));

            // Update BPM spinner initial value
            bpmSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(
                    BPM_RANGE.getValue0(), BPM_RANGE.getValue1(), bpm, 0.1
            ));

            // Mark the task as completed
            markTaskAsCompleted(task);
            logger.log(Level.INFO, "BPM estimation task complete");
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
                // Define a worker thread pool
                ExecutorService executor = Executors.newFixedThreadPool(
                        tasks.length, runnable -> {
                            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                            thread.setDaemon(true);  // Make it so that it can shut down gracefully by placing it in background
                            return thread;
                        }
                );

                // Convert the array of tasks into a list of tasks
                Collection<CustomTask<?>> taskList = List.of(tasks);

                taskList.forEach(task -> task.setOnFailed(event -> {
                    // Log the error
                    logger.log(Level.SEVERE, "Task \"" + task.name + "\" failed: " + task.getException().getMessage());
                    task.getException().printStackTrace();

                    // Show error dialog
                    Popups.showExceptionAlert(
                            "An Error Occurred",
                            "Task \"" + task.name + "\" failed.",
                            (Exception) task.getException()
                    );
                }));

                // Add all tasks to the ongoing tasks queue
                ongoingTasks.addAll(taskList);

                // Update the progress bar section
                markTaskAsCompleted(null);

                // Execute all tasks
                taskList.forEach(executor::execute);
                logger.log(Level.INFO, "Started all transcription view tasks");

                // Await for all tasks' completion
                executor.shutdown();  // Prevent new tasks from being submitted

                boolean hasTerminated;
                try {
                    hasTerminated = executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                logger.log(Level.INFO, "All tasks have finished");
                return hasTerminated;
            }
        };
        masterTask.setOnSucceeded(event -> {
            // Check if all tasks are completed
            if (masterTask.getValue()) {
                // Enable all disabled nodes
                Node[] disabledNodes = new Node[]{
                        // Top Hbox
                        newProjectButton, openProjectButton, saveProjectButton,
                        musicKeyChoice, bpmSpinner, timeSignatureChoice, offsetSpinner,

                        // Bottom Hbox
                        playButton, stopButton, playSkipBackButton, playSkipForwardButton,
                        scrollButton,
                        volumeButton, volumeSlider
                };

                for (Node node : disabledNodes) {
                    node.setDisable(false);
                }

                // Update the `isTranscriptionViewReady` flag
                isEverythingReady = true;
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
                // Hide the progress bar section
                progressBarHBox.setVisible(false);

                // Unbind the progress label text property
                progressLabel.textProperty().unbind();
            }
        }
    }

    /**
     * Helper method that toggles the play button.
     */
    private void togglePlayButton() {
        if (currTime == audioDuration) {
            try {
                audio.setAudioPlaybackTime(0);
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }
        }
        isPaused = togglePaused(isPaused);

        logger.log(Level.FINE, "Toggled pause state (paused is now " + isPaused + ")");
    }

    /**
     * Helper method that toggles the scroll button.
     */
    private void toggleScrollButton() {
        if (scrollToPlayhead) {
            // Change the icon of the scroll button from filled to non-filled
            scrollButtonImage.setImage(
                    new Image(IOMethods.getFileURLAsString(
                            "images/icons/PNGs/" + theme.shortName + "/footsteps-outline.png"
                    ))
            );

        } else {
            // Change the icon of the scroll button from non-filled to filled
            scrollButtonImage.setImage(
                    new Image(IOMethods.getFileURLAsString(
                            "images/icons/PNGs/" + theme.shortName + "/footsteps-filled.png"
                    ))
            );
        }

        // Toggle the `scrollToPlayhead` flag
        scrollToPlayhead = !scrollToPlayhead;

        logger.log(Level.FINE, "Toggled scroll (scroll is now " + scrollToPlayhead + ")");
    }

    /**
     * Helper method that toggles the mute button.
     */
    private void toggleMuteButton() {
        if (isMuted) {
            // Change the icon of the volume button from mute to non-mute
            volumeButtonImage.setImage(
                    new Image(IOMethods.getFileURLAsString(
                            "images/icons/PNGs/" + theme.shortName + "/volume-high.png"
                    ))
            );

            // Unmute the audio by setting the volume back to the value before the mute
            try {
                audio.setPlaybackVolume(volume);
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }
        } else {
            // Change the icon of the volume button from non-mute to mute
            volumeButtonImage.setImage(
                    new Image(IOMethods.getFileURLAsString(
                            "images/icons/PNGs/" + theme.shortName + "/volume-mute.png"
                    ))
            );

            // Mute the audio by setting the volume to zero
            try {
                audio.setPlaybackVolume(0);
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }
        }

        // Toggle the `isMuted` flag
        isMuted = !isMuted;

        logger.log(Level.FINE, "Toggled mute button (muted is now " + isMuted + ")");
    }

    /**
     * Helper method that handles a keyboard key press event.
     *
     * @param keyEvent Key press event.
     */
    private void keyPressEventHandler(KeyEvent keyEvent) {
        // If the spectrogram is not ready do not do anything
        if (!isEverythingReady) return;

        // Get the key event's target
        Node target = (Node) keyEvent.getTarget();

        // Check if the target is a text field or a spinner
        if (target instanceof TextField || target instanceof Spinner) {
            // If it is, do nothing
            return;
        }

        // Get the key event's key code
        KeyCode code = keyEvent.getCode();

        // Non-note playing key press inputs
        if (code == KeyCode.SPACE) {  // Space bar is to toggle the play button
            keyEvent.consume();
            togglePlayButton();

        } else if (code == KeyCode.UP) {  // Up arrow is to increase volume
            keyEvent.consume();
            volumeSlider.setValue(volumeSlider.getValue() + VOLUME_VALUE_DELTA_ON_KEY_PRESS);

        } else if (code == KeyCode.DOWN) {  // Down arrow is to decrease volume
            keyEvent.consume();
            volumeSlider.setValue(volumeSlider.getValue() - VOLUME_VALUE_DELTA_ON_KEY_PRESS);

        } else if (code == KeyCode.M) {  // M key is to toggle mute
            keyEvent.consume();
            toggleMuteButton();

        } else if (code == KeyCode.LEFT) {  // Left arrow is to seek 1 second before
            keyEvent.consume();
            try {
                seekToTime(currTime - 1);
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }

        } else if (code == KeyCode.RIGHT) {  // Right arrow is to seek 1 second ahead
            keyEvent.consume();
            try {
                seekToTime(currTime + 1);
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }

        } else if (code == KeyCode.PERIOD) {  // Period key ('.') is to toggle seeking to playhead
            keyEvent.consume();
            toggleScrollButton();

        } else if (NEW_PROJECT_COMBINATION.match(keyEvent)) {  // Create a new project
            // Consume the key event
            keyEvent.consume();

            // Get the current window
            Window window = rootPane.getScene().getWindow();

            // Get user to select a WAV file
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                    "WAV files (*.wav)", "*.wav"
            );
            File file = ProjectIOHandlers.getFileFromFileDialog(window, extFilter);

            // Create the new project
            ProjectIOHandlers.newProject(
                    mainStage, (Stage) window, file, settingsFile, allAudio, mainViewController
            );

        } else if (OPEN_PROJECT_COMBINATION.match(keyEvent)) {  // Open a project
            // Consume the key event
            keyEvent.consume();

            // Get the current window
            Window window = rootPane.getScene().getWindow();

            // Get user to select an AUDT file
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                    "AudiTranscribe files (*.audt)", "*.audt"
            );
            File file = ProjectIOHandlers.getFileFromFileDialog(window, extFilter);

            // Open the existing project
            ProjectIOHandlers.openProject(
                    mainStage, (Stage) window, file, settingsFile, allAudio, mainViewController
            );

        } else if (SAVE_PROJECT_COMBINATION.match(keyEvent)) {  // Save current project
            handleSavingProject(false);

        } else if (code == KeyCode.MINUS) {  // Increase playback octave number by 1
            keyEvent.consume();
            notePlayer.silenceChannel();  // Stop any notes from playing
            if (octaveNum > 0) {
                logger.log(Level.FINE, "Playback octave raised to " + octaveNum);
                PlottingStuffHandler.updateCurrentOctaveRectangle(
                        currentOctaveRectangle, finalHeight, --octaveNum, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER
                );
            }

        } else if (code == KeyCode.EQUALS) {  // Decrease playback octave number by 1
            keyEvent.consume();
            notePlayer.silenceChannel();  // Stop any notes from playing
            if (octaveNum < 9) {
                logger.log(Level.FINE, "Playback octave lowered to " + octaveNum);
                PlottingStuffHandler.updateCurrentOctaveRectangle(
                        currentOctaveRectangle, finalHeight, ++octaveNum, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER
                );
            }
        }

        // Note playing keyboard inputs
        else {
            switch (code) {
                case A -> notePlayer.noteOn(octaveNum * 12, NOTE_PLAYING_ON_VELOCITY);  // C
                case W -> notePlayer.noteOn(octaveNum * 12 + 1, NOTE_PLAYING_ON_VELOCITY);  // C#
                case S -> notePlayer.noteOn(octaveNum * 12 + 2, NOTE_PLAYING_ON_VELOCITY);  // D
                case E -> notePlayer.noteOn(octaveNum * 12 + 3, NOTE_PLAYING_ON_VELOCITY);  // D#
                case D -> notePlayer.noteOn(octaveNum * 12 + 4, NOTE_PLAYING_ON_VELOCITY);  // E
                case F -> notePlayer.noteOn(octaveNum * 12 + 5, NOTE_PLAYING_ON_VELOCITY);  // F
                case T -> notePlayer.noteOn(octaveNum * 12 + 6, NOTE_PLAYING_ON_VELOCITY);  // F#
                case G -> notePlayer.noteOn(octaveNum * 12 + 7, NOTE_PLAYING_ON_VELOCITY);  // G
                case Y -> notePlayer.noteOn(octaveNum * 12 + 8, NOTE_PLAYING_ON_VELOCITY);  // G#
                case H -> notePlayer.noteOn(octaveNum * 12 + 9, NOTE_PLAYING_ON_VELOCITY);  // A
                case U -> notePlayer.noteOn(octaveNum * 12 + 10, NOTE_PLAYING_ON_VELOCITY);  // A#
                case J -> notePlayer.noteOn(octaveNum * 12 + 11, NOTE_PLAYING_ON_VELOCITY);  // B
                case K -> notePlayer.noteOn(octaveNum * 12 + 12, NOTE_PLAYING_ON_VELOCITY);  // C'
                case O -> notePlayer.noteOn(octaveNum * 12 + 13, NOTE_PLAYING_ON_VELOCITY);  // C#'
                case L -> notePlayer.noteOn(octaveNum * 12 + 14, NOTE_PLAYING_ON_VELOCITY);  // D'
                case P -> notePlayer.noteOn(octaveNum * 12 + 15, NOTE_PLAYING_ON_VELOCITY);  // D#'
                case SEMICOLON -> notePlayer.noteOn(octaveNum * 12 + 16, NOTE_PLAYING_ON_VELOCITY);  // E'
                case QUOTE -> notePlayer.noteOn(octaveNum * 12 + 17, NOTE_PLAYING_ON_VELOCITY);  // F'
            }
        }
    }

    /**
     * Helper method that handles a keyboard key released event.
     *
     * @param keyEvent Key released event.
     */
    private void keyReleasedEventHandler(KeyEvent keyEvent) {
        // If the spectrogram is not ready do not do anything
        if (!isEverythingReady) return;

        // Handle key event
        KeyCode code = keyEvent.getCode();

        switch (code) {
            case A -> notePlayer.noteOff(octaveNum * 12, NOTE_PLAYING_OFF_VELOCITY);  // C
            case W -> notePlayer.noteOff(octaveNum * 12 + 1, NOTE_PLAYING_OFF_VELOCITY);  // C#
            case S -> notePlayer.noteOff(octaveNum * 12 + 2, NOTE_PLAYING_OFF_VELOCITY);  // D
            case E -> notePlayer.noteOff(octaveNum * 12 + 3, NOTE_PLAYING_OFF_VELOCITY);  // D#
            case D -> notePlayer.noteOff(octaveNum * 12 + 4, NOTE_PLAYING_OFF_VELOCITY);  // E
            case F -> notePlayer.noteOff(octaveNum * 12 + 5, NOTE_PLAYING_OFF_VELOCITY);  // F
            case T -> notePlayer.noteOff(octaveNum * 12 + 6, NOTE_PLAYING_OFF_VELOCITY);  // F#
            case G -> notePlayer.noteOff(octaveNum * 12 + 7, NOTE_PLAYING_OFF_VELOCITY);  // G
            case Y -> notePlayer.noteOff(octaveNum * 12 + 8, NOTE_PLAYING_OFF_VELOCITY);  // G#
            case H -> notePlayer.noteOff(octaveNum * 12 + 9, NOTE_PLAYING_OFF_VELOCITY);  // A
            case U -> notePlayer.noteOff(octaveNum * 12 + 10, NOTE_PLAYING_OFF_VELOCITY);  // A#
            case J -> notePlayer.noteOff(octaveNum * 12 + 11, NOTE_PLAYING_OFF_VELOCITY);  // B
            case K -> notePlayer.noteOff(octaveNum * 12 + 12, NOTE_PLAYING_OFF_VELOCITY);  // C'
            case O -> notePlayer.noteOff(octaveNum * 12 + 13, NOTE_PLAYING_OFF_VELOCITY);  // C#'
            case L -> notePlayer.noteOff(octaveNum * 12 + 14, NOTE_PLAYING_OFF_VELOCITY);  // D'
            case P -> notePlayer.noteOff(octaveNum * 12 + 15, NOTE_PLAYING_OFF_VELOCITY);  // D#'
            case SEMICOLON -> notePlayer.noteOff(octaveNum * 12 + 16, NOTE_PLAYING_OFF_VELOCITY);  // E'
            case QUOTE -> notePlayer.noteOff(octaveNum * 12 + 17, NOTE_PLAYING_OFF_VELOCITY);  // F'
        }
    }
}
