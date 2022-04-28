/*
 * SpectrogramViewController.java
 *
 * Created on 2022-02-12
 * Updated on 2022-04-28
 *
 * Description: Contains the spectrogram view's controller class.
 */

package site.overwrite.auditranscribe.views;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.util.Pair;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.Window;
import site.overwrite.auditranscribe.plotting.SpectrogramStuffHandler;
import site.overwrite.auditranscribe.spectrogram.ColourScale;
import site.overwrite.auditranscribe.spectrogram.Spectrogram;
import site.overwrite.auditranscribe.utils.FileUtils;
import site.overwrite.auditranscribe.utils.UnitConversion;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
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

    // Attributes
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private Audio audio;

    private double finalWidth;
    private double finalHeight;
    private double audioDuration;

    private String key = "C";
    private int bpm = 120;
    private int beatsPerBar = 4;
    private double offset = 0.;
    private boolean isPaused = true;
    private double volume = 0.5;
    private boolean isMuted = false;

    private Line[] beatLines;
    private StackPane[] barNumberEllipses;

    // FXML Elements
    // Top HBox
    @FXML
    private HBox topHBox;

    @FXML
    private Button newProjectButton, openProjectButton, saveProjectButton;

    @FXML
    private ChoiceBox<String> musicKeyChoice, timeSignatureChoice;

    @FXML
    private Spinner<Integer> bpmSpinner;

    @FXML
    private Spinner<Double> offsetSpinner;

    // Mid-view
    @FXML
    private ScrollPane leftPane, spectrogramPane, bottomPane;

    @FXML
    private AnchorPane leftPaneAnchor, spectrogramPaneAnchor, bottomPaneAnchor;

    @FXML
    private Pane notePane, barNumberPane;

    @FXML
    private ImageView spectrogramImage;

    // Bottom HBox
    @FXML
    private Label currTimeLabel, totalTimeLabel;

    @FXML
    private Button playButton, stopButton, playSkipBackButton, playSkipForwardButton, scrollButton, volumeButton;

    @FXML
    private ImageView playButtonImage, volumeButtonImage;

    @FXML
    private Slider volumeSlider;

    // Helper methods
    protected boolean togglePaused(boolean isPaused) {
        if (isPaused) {
            // Change the icon of the play button from the play icon to the paused icon
            playButtonImage.setImage(new Image(FileUtils.getFilePath("images/icons/PNGs/pause.png")));

            // Unpause the audio (i.e. play the audio)
            audio.playAudio();
        } else {
            // Change the icon of the play button from the paused icon to the play icon
            playButtonImage.setImage(new Image(FileUtils.getFilePath("images/icons/PNGs/play.png")));

            // Pause the audio
            audio.pauseAudio();
        }

        // Return the toggled version of the `isPaused` flag
        return !isPaused;
    }

    // FXML Methods
    protected void updateBPMValue(int newBPM) {
        // Update the beat lines
        beatLines = SpectrogramStuffHandler.updateBeatLines(
                spectrogramPaneAnchor, beatLines, audioDuration, bpm, newBPM, offset, offset, finalHeight, beatsPerBar,
                beatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
        );

        // Update the bar number ellipses
        barNumberEllipses = SpectrogramStuffHandler.updateBarNumberEllipses(
                barNumberPane, barNumberEllipses, audioDuration, bpm, newBPM, offset, offset,
                barNumberPane.getPrefHeight(), beatsPerBar, beatsPerBar, PX_PER_SECOND,
                SPECTROGRAM_ZOOM_SCALE_X);

        // Update the BPM value
        bpm = newBPM;
    }

    protected void updateOffsetValue(double newOffset) {
        // Update the beat lines
        beatLines = SpectrogramStuffHandler.updateBeatLines(
                spectrogramPaneAnchor, beatLines, audioDuration, bpm, bpm, offset, newOffset, finalHeight, beatsPerBar,
                beatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
        );

        // Update the bar number ellipses
        barNumberEllipses = SpectrogramStuffHandler.updateBarNumberEllipses(
                barNumberPane, barNumberEllipses, audioDuration, bpm, bpm, offset, newOffset,
                barNumberPane.getPrefHeight(), beatsPerBar, beatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
        );

        // Update the offset value
        offset = newOffset;
    }

    // Initialization function
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get the audio file
        try {
            audio = new Audio("testing-audio-files/A440.wav");
//            audio = new Audio("testing-audio-files/Melancholy.wav");

            // Update audio duration attribute and label
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
            finalWidth = image.getWidth() * SPECTROGRAM_ZOOM_SCALE_X;
            finalHeight = image.getHeight() * SPECTROGRAM_ZOOM_SCALE_Y;

            // Fix panes' properties
            leftPane.setFitToWidth(true);
            leftPaneAnchor.setPrefHeight(finalHeight);

            spectrogramPaneAnchor.setPrefWidth(finalWidth);
            spectrogramPaneAnchor.setPrefHeight(finalHeight);

            bottomPane.setFitToHeight(true);
            bottomPaneAnchor.setPrefWidth(finalWidth);

            // Set scrolling for panes
            leftPane.vvalueProperty().bindBidirectional(spectrogramPane.vvalueProperty());
            bottomPane.hvalueProperty().bindBidirectional(spectrogramPane.hvalueProperty());

            // Update spinners' ranges
            SpinnerValueFactory.IntegerSpinnerValueFactory bpmSpinnerFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(
                            BPM_RANGE.getKey(), BPM_RANGE.getValue(), 120, 1
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
            timeSignatureChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue,
                                                                                        newValue) -> {
                // Get the old and new beats per bar
                int oldBeatsPerBar = TIME_SIGNATURE_TO_BEATS_PER_BAR.get(oldValue);
                int newBeatsPerBar = TIME_SIGNATURE_TO_BEATS_PER_BAR.get(newValue);

                // Update the beat lines and bar number ellipses
                beatLines = SpectrogramStuffHandler.updateBeatLines(
                        spectrogramPaneAnchor, beatLines, audioDuration, bpm, bpm, offset, offset, finalHeight,
                        oldBeatsPerBar, newBeatsPerBar, PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
                );

                barNumberEllipses = SpectrogramStuffHandler.updateBarNumberEllipses(
                        barNumberPane, barNumberEllipses, audioDuration, bpm, bpm, offset, offset,
                        barNumberPane.getPrefHeight(), oldBeatsPerBar, newBeatsPerBar, PX_PER_SECOND,
                        SPECTROGRAM_ZOOM_SCALE_X
                );

                // Update the beats per bar
                beatsPerBar = newBeatsPerBar;
            });

            // Add methods to buttons
            playButton.setOnAction(event -> {
                if (audio.getCurrAudioTime() == audioDuration) {
                    audio.setAudioPlaybackTime(0);
                }
                isPaused = togglePaused(isPaused);
            });
            stopButton.setOnAction(event -> {
                audio.stopAudio();
                isPaused = togglePaused(false);
            });
            playSkipBackButton.setOnAction(event -> audio.setAudioPlaybackTime(0));
            playSkipForwardButton.setOnAction(event -> audio.setAudioPlaybackTime(audioDuration));

            volumeButton.setOnAction(event -> {
                if (isMuted) {
                    // Change the icon of the volume button from mute to non-mute
                    volumeButtonImage.setImage(new Image(FileUtils.getFilePath("images/icons/PNGs/volume-high.png")));

                    // Unmute the audio by setting the volume back to the value before the mute
                    audio.setPlaybackVolume(volume);
                } else {
                    // Change the icon of the volume button from non-mute to mute
                    volumeButtonImage.setImage(new Image(FileUtils.getFilePath("images/icons/PNGs/volume-mute.png")));

                    // Mute the audio by setting the volume to zero
                    audio.setPlaybackVolume(0);
                }

                // Toggle the `isMuted` flag
                isMuted = !isMuted;
            });

            // Set method on the volume slider
            volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                // Update the volume value
                volume = newValue.doubleValue();

                // Change the icon of the volume button from mute to non-mute
                if (isMuted) {
                    volumeButtonImage.setImage(new Image(FileUtils.getFilePath("images/icons/PNGs/volume-high.png")));
                    isMuted = false;
                }

                // Update audio volume
                audio.setPlaybackVolume(volume);
            });

            // Constantly update the current playback time
            Task<Void> updatePlaybackTimeTask = new Task<>() {
                @Override
                public Void call() throws Exception {
                    while (true) {  // Fixme: `while` statement cannot complete without throwing an exception
                        // Get the current time
                        double currTime = audio.getCurrAudioTime();

                        // Update the current time label
                        Platform.runLater(() -> currTimeLabel.setText(UnitConversion.secondsToTimeString(currTime)));

                        // Check if the current time has exceeded
                        if (currTime >= audioDuration) {
                            // Pause the audio
                            if (!isPaused) {
                                isPaused = togglePaused(false);
                            }
                        }

                        // Wait for 50 ms
                        Thread.sleep(50);  // Fixme: Call to `Thread.sleep()` in a loop, probably busy-waiting
                    }
                }
            };

            Thread updatePlaybackTimeThread = new Thread(updatePlaybackTimeTask);
            updatePlaybackTimeThread.setDaemon(true);
            updatePlaybackTimeThread.start();

            // Set image on the spectrogram area
            spectrogramImage.setFitHeight(finalWidth);
            spectrogramImage.setFitWidth(finalHeight);
            spectrogramImage.setImage(image);

            // Add note labels and note lines
            SpectrogramStuffHandler.addNoteLabels(notePane, finalHeight, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER);
            SpectrogramStuffHandler.addNoteLines(spectrogramPaneAnchor, finalHeight, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER);

            // Add the beat lines and bar number ellipses
            beatLines = SpectrogramStuffHandler.getBeatLines(
                    bpm, beatsPerBar, PX_PER_SECOND, finalHeight, audioDuration, offset, SPECTROGRAM_ZOOM_SCALE_X
            );
            SpectrogramStuffHandler.addBeatLines(spectrogramPaneAnchor, beatLines);

            barNumberEllipses = SpectrogramStuffHandler.getBarNumberEllipses(
                    bpm, beatsPerBar, PX_PER_SECOND, barNumberPane.getPrefHeight(), audioDuration, offset,
                    SPECTROGRAM_ZOOM_SCALE_X
            );
            SpectrogramStuffHandler.addBarNumberEllipses(barNumberPane, barNumberEllipses);

            // Resize image pane
            spectrogramImage.setFitWidth(finalWidth);
            spectrogramImage.setFitHeight(finalHeight);

            // Show the spectrogram from the middle
            spectrogramPane.setVvalue(0.5);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
