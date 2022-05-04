/*
 * SpectrogramViewController.java
 *
 * Created on 2022-02-12
 * Updated on 2022-05-04
 *
 * Description: Contains the spectrogram view's controller class.
 */

package site.overwrite.auditranscribe.views;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.util.Pair;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.Window;
import site.overwrite.auditranscribe.io.ProjectIOHandlers;
import site.overwrite.auditranscribe.plotting.PlottingStuffHandler;
import site.overwrite.auditranscribe.spectrogram.ColourScale;
import site.overwrite.auditranscribe.spectrogram.Spectrogram;
import site.overwrite.auditranscribe.utils.ArrayUtils;
import site.overwrite.auditranscribe.utils.FileUtils;
import site.overwrite.auditranscribe.utils.UnitConversion;

import java.io.IOException;
import java.net.URL;
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

    final double SPECTROGRAM_ZOOM_SCALE_X = 2;
    final double SPECTROGRAM_ZOOM_SCALE_Y = 5;

    final int PX_PER_SECOND = 120;
    final int BINS_PER_OCTAVE = 60;
    final int SPECTROGRAM_HOP_LENGTH = 1024;  // Needs to be a power of 2
    final double NUM_PX_PER_OCTAVE = 72;

    final int MIN_NOTE_NUMBER = 0;  // C0
    final int MAX_NOTE_NUMBER = 119;  // B9

    final long UPDATE_PLAYBACK_SCHEDULER_PERIOD = 50;  // In milliseconds

    final boolean USE_FANCY_SHARPS_FOR_NOTE_LABELS = true;

    // File-Savable Attributes
    private int keyIndex = 0;  // Index of the key chosen, according to the `MUSIC_KEYS` array
    private int timeSignatureIndex = 0;
    private double bpm = 120;
    private double offset = 0.;
    private double volume = 0.5;
    private double audioDuration = 0;  // Will be updated upon scene initialization
    private double currTime = 0;

    // Other attributes
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private Audio audio;

    private String key = "C";
    private int beatsPerBar = 4;
    private boolean isPaused = true;
    private boolean isMuted = false;
    private boolean scrollToPlayhead = false;

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

    // Helper methods
    protected boolean togglePaused(boolean isPaused) {
        if (isPaused) {
            // Change the icon of the play button from the play icon to the paused icon
            playButtonImage.setImage(new Image(FileUtils.getFileURLAsString("images/icons/PNGs/pause.png")));

            // Unpause the audio (i.e. play the audio)
            audio.playAudio();
        } else {
            // Change the icon of the play button from the paused icon to the play icon
            playButtonImage.setImage(new Image(FileUtils.getFileURLAsString("images/icons/PNGs/play.png")));

            // Pause the audio
            audio.pauseAudio();
        }

        // Return the toggled version of the `isPaused` flag
        logger.log(Level.FINE, "Toggled pause state from " + isPaused + " to " + !isPaused);
        return !isPaused;
    }

    protected void seekToTime(double seekTime) {
        // Update the start time of the audio
        // (Do this so that when the player resumes out of a stop state it will start here)
        audio.setAudioStartTime(seekTime);

        // Set the playback time
        // (We do this after updating start time to avoid pesky seeking issues)
        audio.setAudioPlaybackTime(seekTime);

        // Update the current time label
        currTimeLabel.setText(UnitConversion.secondsToTimeString(seekTime));

        // Update coloured progress pane and playhead line
        double newXPos = seekTime * PX_PER_SECOND * SPECTROGRAM_ZOOM_SCALE_X;

        colouredProgressPane.setPrefWidth(newXPos);
        PlottingStuffHandler.updatePlayheadLine(playheadLine, newXPos);

        logger.log(Level.FINE, "Seeked to " + seekTime + " seconds");
    }

    // Value updating methods
    protected void updateBPMValue(double newBPM) {
        // Update the beat lines
        beatLines = PlottingStuffHandler.updateBeatLines(
                spectrogramPaneAnchor, beatLines, audioDuration, bpm, newBPM, offset, offset, finalHeight, beatsPerBar,
                beatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
        );

        // Update the bar number ellipses
        barNumberEllipses = PlottingStuffHandler.updateBarNumberEllipses(
                barNumberPane, barNumberEllipses, audioDuration, bpm, newBPM, offset, offset,
                barNumberPane.getPrefHeight(), beatsPerBar, beatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
        );

        // Update the BPM value
        logger.log(Level.FINE, "Updated BPM value from " + bpm + " to " + newBPM);
        bpm = newBPM;
    }

    protected void updateOffsetValue(double newOffset) {
        // Update the beat lines
        beatLines = PlottingStuffHandler.updateBeatLines(
                spectrogramPaneAnchor, beatLines, audioDuration, bpm, bpm, offset, newOffset, finalHeight, beatsPerBar,
                beatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
        );

        // Update the bar number ellipses
        barNumberEllipses = PlottingStuffHandler.updateBarNumberEllipses(
                barNumberPane, barNumberEllipses, audioDuration, bpm, bpm, offset, newOffset,
                barNumberPane.getPrefHeight(), beatsPerBar, beatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
        );

        // Update the offset value
        logger.log(Level.FINE, "Updated offset value from " + offset + " to " + newOffset);
        offset = newOffset;
    }

    // Initialization method
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Add CSS stylesheets to the scene
        mainPane.getStylesheets().add(FileUtils.getFileURLAsString("views/css/base.css"));
        mainPane.getStylesheets().add(FileUtils.getFileURLAsString("views/css/light-mode.css"));  // Todo: add theme support

        // Update spinners' ranges
        SpinnerValueFactory.DoubleSpinnerValueFactory bpmSpinnerFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(
                        BPM_RANGE.getKey(), BPM_RANGE.getValue(), 120, 0.1
                );
        SpinnerValueFactory.DoubleSpinnerValueFactory offsetSpinnerFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(
                        OFFSET_RANGE.getKey(), OFFSET_RANGE.getValue(), 0, 0.01
                );

        bpmSpinner.setValueFactory(bpmSpinnerFactory);
        offsetSpinner.setValueFactory(offsetSpinnerFactory);

        // Set the choice boxes' choices
        for (String musicKey : MUSIC_KEYS) musicKeyChoice.getItems().add(musicKey);
        for (String timeSignature : TIME_SIGNATURES) timeSignatureChoice.getItems().add(timeSignature);

        musicKeyChoice.setValue("C");
        timeSignatureChoice.setValue("4/4");

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
            key = newValue;
            keyIndex = ArrayUtils.findIndex(MUSIC_KEYS, newValue);
        });

        timeSignatureChoice.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    logger.log(Level.FINE, "Changed time signature from " + oldValue + " to " + newValue);

                    // Get the old and new beats per bar
                    int oldBeatsPerBar = TIME_SIGNATURE_TO_BEATS_PER_BAR.get(oldValue);
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
        newProjectButton.setOnAction(ProjectIOHandlers::newProject);

        playButton.setOnAction(event -> {
            logger.log(Level.FINE, "Pressed play button");

            if (currTime == audioDuration) {
                audio.setAudioPlaybackTime(0);
            }
            isPaused = togglePaused(isPaused);
        });

        stopButton.setOnAction(event -> {
            logger.log(Level.FINE, "Pressed stop button");

            // First stop the audio
            audio.stopAudio();

            // Then update the timings shown on the GUI
            seekToTime(0);

            // Finally, toggle the paused flag
            isPaused = togglePaused(false);
        });

        playSkipBackButton.setOnAction(event -> {
            logger.log(Level.FINE, "Pressed skip back button");

            // Seek to the start of the audio
            seekToTime(0);

            // Pause the audio
            isPaused = togglePaused(false);
        });

        playSkipForwardButton.setOnAction(event -> {
            logger.log(Level.FINE, "Pressed skip forward button");

            // Seek to the end of the audio
            seekToTime(audioDuration);

            // Force the audio to play at the end
            // (This is to avoid a nasty seek to end issue where user needs to click on play button twice)
            isPaused = togglePaused(true);
        });

        volumeButton.setOnAction(event -> {
            if (isMuted) {
                // Change the icon of the volume button from mute to non-mute
                volumeButtonImage.setImage(new Image(FileUtils.getFileURLAsString("images/icons/PNGs/volume-high.png")));

                // Unmute the audio by setting the volume back to the value before the mute
                audio.setPlaybackVolume(volume);
            } else {
                // Change the icon of the volume button from non-mute to mute
                volumeButtonImage.setImage(new Image(FileUtils.getFileURLAsString("images/icons/PNGs/volume-mute.png")));

                // Mute the audio by setting the volume to zero
                audio.setPlaybackVolume(0);
            }

            // Toggle the `isMuted` flag
            isMuted = !isMuted;

            logger.log(Level.FINE, "Pressed volume toggle button (muted is now " + isMuted + ")");
        });

        scrollButton.setOnAction(event -> {
            if (scrollToPlayhead) {
                // Change the icon of the scroll button from filled to non-filled
                scrollButtonImage.setImage(new Image(FileUtils.getFileURLAsString("images/icons/PNGs/footsteps-outline.png")));

            } else {
                // Change the icon of the scroll button from non-filled to filled
                scrollButtonImage.setImage(new Image(FileUtils.getFileURLAsString("images/icons/PNGs/footsteps-filled.png")));
            }

            // Toggle the `scrollToPlayhead` flag
            scrollToPlayhead = !scrollToPlayhead;

            logger.log(Level.FINE, "Pressed scroll toggle button (scroll is now " + scrollToPlayhead + ")");
        });

        // Set method on the volume slider
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Update the volume value
            volume = newValue.doubleValue();

            // Change the icon of the volume button from mute to non-mute
            if (isMuted) {
                volumeButtonImage.setImage(new Image(FileUtils.getFileURLAsString("images/icons/PNGs/volume-high.png")));
                isMuted = false;
            }

            // Update audio volume
            audio.setPlaybackVolume(volume);

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
                seekToTime(seekTime);
            }
        });
    }

    // Public methods

    /**
     * Method that sets the audio file for the spectrogram view.
     *
     * @param audioObj Audio object representing the audio file.
     * @throws IOException If something went wrong when reading in a file.
     */
    public void setAudioFile(Audio audioObj) throws IOException {
        // Set the audio file attribute
        audio = audioObj;

        // Update audio duration attribute and total time label
        audioDuration = audio.getDuration();
        totalTimeLabel.setText(UnitConversion.secondsToTimeString(audioDuration));

        // Set initial volume
        audio.setPlaybackVolume(volume);

        // Generate spectrogram
        Spectrogram spectrogram = new Spectrogram(
                audio, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER, BINS_PER_OCTAVE, PX_PER_SECOND, NUM_PX_PER_OCTAVE,
                SPECTROGRAM_HOP_LENGTH
        );
        WritableImage image = spectrogram.generateSpectrogram(Window.HANN_WINDOW, ColourScale.VIRIDIS);

        // Get the final width and height
        double finalWidth = image.getWidth() * SPECTROGRAM_ZOOM_SCALE_X;
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
                currTime = audio.getCurrAudioTime();

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
                    audio.setAudioStartTime(0);

                    // We need to do this so that the status is set to paused
                    audio.stopAudio();
                    audio.pauseAudio();
                }

                // Update scrolling
                if (scrollToPlayhead) {
                    // Get the 'half width' of the spectrogram area
                    double spectrogramAreaHalfWidth = spectrogramPane.getWidth() / 2;

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
            }
        }, 0, UPDATE_PLAYBACK_SCHEDULER_PERIOD, TimeUnit.MILLISECONDS);

        // Set image on the spectrogram area
        spectrogramImage.setFitHeight(finalWidth);
        spectrogramImage.setFitWidth(finalHeight);
        spectrogramImage.setImage(image);

        // Add note labels and note lines
        noteLabels = PlottingStuffHandler.addNoteLabels(
                notePane, noteLabels, key, finalHeight, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER,
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
}
