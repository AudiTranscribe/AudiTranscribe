/*
 * SpectrogramViewController.java
 *
 * Created on 2022-02-12
 * Updated on 2022-05-14
 *
 * Description: Contains the spectrogram view's controller class.
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.javatuples.Pair;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.AudioDataObject;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.GUIDataObject;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.ProjectDataObject;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.QTransformDataObject;
import site.overwrite.auditranscribe.io.db.ProjectsDB;
import site.overwrite.auditranscribe.plotting.PlottingStuffHandler;
import site.overwrite.auditranscribe.spectrogram.*;
import site.overwrite.auditranscribe.utils.*;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Map.entry;

public class SpectrogramViewController implements Initializable {
    // Constants
    final String[] MUSIC_KEYS = {"C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B"};
    final Map<String, Integer> TIME_SIGNATURE_TO_BEATS_PER_BAR = Map.ofEntries(
            // Simple time signatures
            entry("4/4", 4),
            entry("2/2", 2),
            entry("2/4", 4),
            entry("3/4", 4),
            entry("3/8", 8),

            // Compound time signatures
            entry("6/8", 8),
            entry("9/8", 8),
            entry("12/8", 8)
    );  // See https://en.wikipedia.org/wiki/Time_signature#Characteristics
    final String[] TIME_SIGNATURES = {"4/4", "2/2", "2/4", "3/4", "3/8", "6/8", "9/8", "12/8"};

    final Pair<Integer, Integer> BPM_RANGE = new Pair<>(1, 512);  // In the format [min, max]
    final Pair<Double, Double> OFFSET_RANGE = new Pair<>(-5., 5.);  // In the format [min, max]

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

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private ProjectsDB projectsDB;

    private String audtFilePath;
    private String audtFileName;
    private String audioFilePath;
    private String audioFileName;
    private Audio audio;
    private double[][] magnitudes;

    private String musicKey = "C";
    private int beatsPerBar = 4;
    private boolean isPaused = true;
    private boolean isMuted = false;
    private boolean scrollToPlayhead = false;

    private double finalWidth;
    private double finalHeight;

    private Label[] noteLabels;
    private Line[] beatLines;
    private StackPane[] barNumberEllipses;
    private Line playheadLine;

    // FXML Elements
    @FXML
    private AnchorPane mainPane;

    // Top HBox
    @FXML
    private Button newProjectButton, openProjectButton, saveProjectButton;

    @FXML
    private ChoiceBox<String> musicKeyChoice, timeSignatureChoice;

    @FXML
    private Spinner<Double> bpmSpinner, offsetSpinner;

    // Mid-view
    @FXML
    private ScrollPane leftPane, spectrogramPane, bottomPane;

    @FXML
    private AnchorPane leftPaneAnchor, spectrogramPaneAnchor, bottomPaneAnchor;

    @FXML
    private Pane notePane, barNumberPane, clickableProgressPane, colouredProgressPane;

    @FXML
    private ImageView spectrogramImage;

    // Bottom HBox
    @FXML
    private Label currTimeLabel, totalTimeLabel;

    @FXML
    private Button playButton, stopButton, playSkipBackButton, playSkipForwardButton, scrollButton, volumeButton;

    @FXML
    private ImageView playButtonImage, volumeButtonImage, scrollButtonImage;

    @FXML
    private Slider volumeSlider;

    // Initialization method
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Add CSS stylesheets to the scene
        mainPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/base.css"));
        mainPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/light-mode.css"));  // Todo: add theme support

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
        for (String musicKey : MUSIC_KEYS) musicKeyChoice.getItems().add(musicKey);
        for (String timeSignature : TIME_SIGNATURES) timeSignatureChoice.getItems().add(timeSignature);

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
            noteLabels = PlottingStuffHandler.addNoteLabels(
                    notePane, noteLabels, newValue, finalHeight, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER,
                    USE_FANCY_SHARPS_FOR_NOTE_LABELS
            );

            // Update the music key value and music key index
            musicKey = newValue;
            musicKeyIndex = ArrayUtils.findIndex(MUSIC_KEYS, newValue);
        });

        timeSignatureChoice.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    logger.log(Level.FINE, "Changed time signature from " + oldValue + " to " + newValue);

                    // Get the old and new beats per bar
                    int oldBeatsPerBar = 0;
                    if (oldValue != null) {
                        oldBeatsPerBar = TIME_SIGNATURE_TO_BEATS_PER_BAR.get(oldValue);
                    }
                    int newBeatsPerBar = TIME_SIGNATURE_TO_BEATS_PER_BAR.get(newValue);

                    // Update the beat lines and bar number ellipses
                    beatLines = PlottingStuffHandler.updateBeatLines(
                            spectrogramPaneAnchor, beatLines, audioDuration, bpm, bpm, offset, offset, finalHeight,
                            oldBeatsPerBar, newBeatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
                    );

                    barNumberEllipses = PlottingStuffHandler.updateBarNumberEllipses(
                            barNumberPane, barNumberEllipses, audioDuration, bpm, bpm, offset, offset,
                            barNumberPane.getPrefHeight(), oldBeatsPerBar, newBeatsPerBar, PX_PER_SECOND,
                            SPECTROGRAM_ZOOM_SCALE_X
                    );

                    // Update the time signature index
                    timeSignatureIndex = ArrayUtils.findIndex(TIME_SIGNATURES, newValue);

                    // Update the beats per bar
                    beatsPerBar = newBeatsPerBar;
                });

        // Add methods to buttons
        newProjectButton.setOnAction(actionEvent -> {
            // Get the current window
            Window window = ProjectIOHandlers.getWindow(actionEvent);

            // Get user to select a file
            File file = ProjectIOHandlers.getFileFromFileDialog(window);

            // Create the new project
            ProjectIOHandlers.newProject(mainStage, (Stage) window, file);
        });

        openProjectButton.setOnAction(actionEvent -> {
            // Get the current window
            Window window = ProjectIOHandlers.getWindow(actionEvent);

            // Get user to select a file
            File file = ProjectIOHandlers.getFileFromFileDialog(window);

            // Open the existing project
            ProjectIOHandlers.openProject(mainStage, (Stage) window, file);
        });

        saveProjectButton.setOnAction(this::handleSavingProject);

        playButton.setOnAction(event -> togglePlayButton());

        stopButton.setOnAction(event -> {
            logger.log(Level.FINE, "Pressed stop button");

            // First stop the audio
            try {
                audio.stopAudio();
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

        // Set method on the volume slider
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Update the volume value
            volume = newValue.doubleValue();

            // Change the icon of the volume button from mute to non-mute
            if (isMuted) {
                volumeButtonImage.setImage(
                        new Image(IOMethods.getFileURLAsString("images/icons/PNGs/volume-high.png"))
                );
                isMuted = false;
            }

            // Update audio volume
            try {
                audio.setPlaybackVolume(volume);
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }

            logger.log(Level.FINE, "Changed volume from " + oldValue + " to " + newValue);
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
                try {
                    seekToTime(seekTime);
                } catch (InvalidObjectException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Get the projects database
        try {
            projectsDB = new ProjectsDB();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Public methods

    /**
     * Method that finishes the setting up of the spectrogram view controller.<br>
     * Note that this method has to be called <b>last</b>, after all other spectrogram things have
     * been set up.
     * @param mainStage Main stage.
     */
    public void finishSetup(Stage mainStage) {
        // Update the main stage attribute
        this.mainStage = mainStage;

        // Set choices
        musicKeyChoice.setValue(MUSIC_KEYS[musicKeyIndex]);
        timeSignatureChoice.setValue(TIME_SIGNATURES[timeSignatureIndex]);

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

        // Update sliders
        volumeSlider.setValue(volume);

        // Update labels
        totalTimeLabel.setText(UnitConversion.secondsToTimeString(audioDuration));
        currTimeLabel.setText(UnitConversion.secondsToTimeString(currTime));

        // Update playhead
        try {
            seekToTime(currTime);
        } catch (InvalidObjectException e) {
            throw new RuntimeException(e);
        }

        // Set keyboard button press methods
        mainPane.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::keyboardPressEventHandler);

        // Ensure main pane is in focus
        mainPane.requestFocus();
    }

    /**
     * Method that makes the spectrogram view controller use the existing project data, that was
     * supposedly read from a file.
     *
     * @param audtFilePath <b>Absolute</b> path to the file that contained the data.
     * @param audtFileName The name of the AUDT file.
     * @param projectData  The project data.
     */
    public void useExistingData(String audtFilePath, String audtFileName, ProjectDataObject projectData) {
        // Set up GUI data
        musicKeyIndex = projectData.guiData.musicKeyIndex;
        timeSignatureIndex = projectData.guiData.timeSignatureIndex;
        bpm = projectData.guiData.bpm;
        offset = projectData.guiData.offsetSeconds;
        volume = projectData.guiData.playbackVolume;
        audioFileName = projectData.guiData.audioFileName;
        audioDuration = projectData.guiData.totalDurationInMS / 1000.;
        currTime = projectData.guiData.currTimeInMS / 1000.;

        // Set the AudiTranscribe file's file path and file name
        this.audtFilePath = audtFilePath;
        this.audtFileName = audtFileName;

        // Set up Q-Transform data and audio data
        try {
            setAudioAndSpectrogramData(projectData.qTransformData, projectData.audioData);
        } catch (IOException | UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        }

        // Update music key and beats per bar
        musicKey = MUSIC_KEYS[musicKeyIndex];
        beatsPerBar = TIME_SIGNATURE_TO_BEATS_PER_BAR.get(TIME_SIGNATURES[timeSignatureIndex]);

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
     * Method that sets the audio and spectrogram data for the spectrogram view controller.<br>
     * This method uses the actual audio file to do the setting of the data.
     *
     * @param audioObj An <code>Audio</code> object that contains audio data.
     */
    public void setAudioAndSpectrogramData(Audio audioObj) {
        // Set attributes
        audio = audioObj;
        audioFilePath = audioObj.getAudioFilePath();
        audioFileName = audioObj.getAudioFileName();
        audioDuration = audio.getDuration();
        sampleRate = audio.getSampleRate();

        // Generate spectrogram image based on newly generated magnitude data
        Spectrogram spectrogram = new Spectrogram(
                audio, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER, BINS_PER_OCTAVE, SPECTROGRAM_HOP_LENGTH, PX_PER_SECOND,
                NUM_PX_PER_OCTAVE
        );
        magnitudes = spectrogram.getSpectrogramMagnitudes(WindowFunction.HANN_WINDOW);
        WritableImage image = spectrogram.generateSpectrogram(magnitudes, ColourScale.VIRIDIS);

        // Finish setting up the spectrogram and its related attributes
        finishSettingUpSpectrogram(image);
    }

    /**
     * Method that sets the audio and spectrogram data for the spectrogram view controller.<br>
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
        audioFilePath = audioData.audioFilePath;
        audio = new Audio(new File(audioFilePath));
        sampleRate = audioData.sampleRate;

        magnitudes = qTransformData.qTransformMatrix;

        // Generate spectrogram image based on existing magnitude data
        Spectrogram spectrogram = new Spectrogram(
                MIN_NOTE_NUMBER, MAX_NOTE_NUMBER, BINS_PER_OCTAVE, SPECTROGRAM_HOP_LENGTH, PX_PER_SECOND,
                NUM_PX_PER_OCTAVE, sampleRate, audioDuration
        );
        WritableImage image = spectrogram.generateSpectrogram(magnitudes, ColourScale.VIRIDIS);

        // Finish setting up the spectrogram and its related attributes
        finishSettingUpSpectrogram(image);
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
            // If the `newPoxX` is within the last 'half width' of the entire spectrogram area, keep the
            // scrolling to the end
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
        currTimeLabel.setText(UnitConversion.secondsToTimeString(seekTime));

        // Update coloured progress pane and playhead line
        double newXPos = seekTime * PX_PER_SECOND * SPECTROGRAM_ZOOM_SCALE_X;

        colouredProgressPane.setPrefWidth(newXPos);
        PlottingStuffHandler.updatePlayheadLine(playheadLine, newXPos);

        logger.log(Level.FINE, "Seeked to " + seekTime + " seconds");
    }

    /**
     * Helper method that handles the saving of the project.
     *
     * @param event Event that triggered this function.
     */
    private void handleSavingProject(Event event) {
        // Allow user to select save location if `audtFilePath` is unset
        if (audtFilePath == null) {
            logger.log(Level.FINE, "AUDT file destination not yet set; asking now");

            // Get current window
            javafx.stage.Window window = ((Node) event.getSource()).getScene().getWindow();

            // Ask user to choose a file
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showSaveDialog(window);

            // If operation was cancelled return
            if (file == null) return;

            // Update the file path and file name
            audtFilePath = file.getAbsolutePath();
            audtFileName = file.getName();

            if (!audtFilePath.toLowerCase().endsWith(".audt")) audtFilePath += ".audt";
            if (!audtFileName.toLowerCase().endsWith(".audt")) audtFileName += ".audt";

            logger.log(Level.FINE, "AUDT file destination set to " + audtFilePath);
        }

        // Package all the current data into a `ProjectDataObject`
        logger.log(Level.INFO, "Packaging data for saving");
        QTransformDataObject qTransformData = new QTransformDataObject(
                magnitudes
        );
        AudioDataObject audioData = new AudioDataObject(
                audioFilePath, sampleRate
        );
        GUIDataObject guiData = new GUIDataObject(
                musicKeyIndex, timeSignatureIndex, bpm, offset, volume, audioFileName,
                (int) (audioDuration * 1000), (int) (currTime * 1000)
        );

        ProjectDataObject projectData = new ProjectDataObject(
                qTransformData, audioData, guiData
        );

        // Save the project
        try {
            ProjectIOHandlers.saveProject(audtFilePath, projectData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger.log(Level.INFO, "File saved");

        // Update the project file list
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
     * Helper method that toggles the paused state.
     *
     * @param isPaused Old paused state.
     * @return New paused state.
     */
    private boolean togglePaused(boolean isPaused) {
        if (isPaused) {
            // Change the icon of the play button from the play icon to the paused icon
            playButtonImage.setImage(new Image(IOMethods.getFileURLAsString("images/icons/PNGs/pause.png")));

            // Unpause the audio (i.e. play the audio)
            try {
                audio.playAudio();
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }

        } else {
            // Change the icon of the play button from the paused icon to the play icon
            playButtonImage.setImage(new Image(IOMethods.getFileURLAsString("images/icons/PNGs/play.png")));

            // Pause the audio
            try {
                audio.pauseAudio();
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
     * Helper method that finishes the setup for the spectrogram.
     */
    private void finishSettingUpSpectrogram(WritableImage image) {
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
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0, runnable -> {
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
                Platform.runLater(() -> currTimeLabel.setText(UnitConversion.secondsToTimeString(currTime)));

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
                        audio.stopAudio();
                        audio.pauseAudio();
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

        // Resize spectrogram image pane
        // (We do this at the end to ensure that the image is properly placed)
        spectrogramImage.setFitWidth(finalWidth);
        spectrogramImage.setFitHeight(finalHeight);

        // Show the spectrogram from the middle
        spectrogramPane.setVvalue(0.5);

        // Report that the spectrogram view is ready to be shown
        logger.log(Level.INFO, "Spectrogram view ready to be shown");
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
                    new Image(IOMethods.getFileURLAsString("images/icons/PNGs/footsteps-outline.png"))
            );

        } else {
            // Change the icon of the scroll button from non-filled to filled
            scrollButtonImage.setImage(
                    new Image(IOMethods.getFileURLAsString("images/icons/PNGs/footsteps-filled.png"))
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
                    new Image(IOMethods.getFileURLAsString("images/icons/PNGs/volume-high.png"))
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
                    new Image(IOMethods.getFileURLAsString("images/icons/PNGs/volume-mute.png"))
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
     * Helper method that handles a keyboard press event.
     *
     * @param keyEvent Key event.
     */
    private void keyboardPressEventHandler(KeyEvent keyEvent) {
        // Stop passing this event to the next node
        keyEvent.consume();

        // Handle key event
        KeyCode code = keyEvent.getCode();

        if (code == KeyCode.SPACE) {  // Space bar is to toggle the play button
            togglePlayButton();

        } else if (code == KeyCode.UP) {  // Up arrow is to increase volume
            volumeSlider.setValue(volumeSlider.getValue() + VOLUME_VALUE_DELTA_ON_KEY_PRESS);

        } else if (code == KeyCode.DOWN) {  // Down arrow is to decrease volume
            volumeSlider.setValue(volumeSlider.getValue() - VOLUME_VALUE_DELTA_ON_KEY_PRESS);

        } else if (code == KeyCode.M) {  // M key is to toggle mute
            toggleMuteButton();

        } else if (code == KeyCode.LEFT) {  // Left arrow is to seek 1 second before
            try {
                seekToTime(currTime - 1);
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }

        } else if (code == KeyCode.RIGHT) {  // Right arrow is to seek 1 second ahead
            try {
                seekToTime(currTime + 1);
            } catch (InvalidObjectException e) {
                throw new RuntimeException(e);
            }

        } else if (code == KeyCode.PERIOD) {  // Period key ('.') is to toggle seeking to playhead
            toggleScrollButton();

        } else if (NEW_PROJECT_COMBINATION.match(keyEvent)) {  // Create a new project
            // Get the current window
            Window window = ProjectIOHandlers.getWindow(keyEvent);

            // Get user to select a file
            File file = ProjectIOHandlers.getFileFromFileDialog(window);

            // Create the new project
            ProjectIOHandlers.newProject(mainStage, (Stage) window, file);

        } else if (OPEN_PROJECT_COMBINATION.match(keyEvent)) {  // Open a project
            // Get the current window
            Window window = ProjectIOHandlers.getWindow(keyEvent);

            // Get user to select a file
            File file = ProjectIOHandlers.getFileFromFileDialog(window);

            // Open the existing project
            ProjectIOHandlers.openProject(mainStage, (Stage) window, file);

        } else if (SAVE_PROJECT_COMBINATION.match(keyEvent)) {  // Save current project
            handleSavingProject(keyEvent);
        }
    }
}
