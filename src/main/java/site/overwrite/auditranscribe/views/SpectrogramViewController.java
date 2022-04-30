/*
 * SpectrogramViewController.java
 *
 * Created on 2022-02-12
 * Updated on 2022-04-26
 *
 * Description: Contains the spectrogram view's controller class.
 */

package site.overwrite.auditranscribe.views;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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

    final int MIN_NOTE_NUMBER = 0;  // C0
    final int MAX_NOTE_NUMBER = 119;  // B9

    // Attributes
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private double finalWidth;
    private double finalHeight;
    private double audioDuration;

    private String key = "C";
    private int bpm = 120;
    private int beatsPerBar = 4;
    private double offset = 0.;

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
//            Audio audio = new Audio("testing-audio-files/A440.wav");
            Audio audio = new Audio("testing-audio-files/Melancholy.wav");
//            audio = new Audio("testing-audio-files/A440.mp3");

            // Update audio duration attribute
            audioDuration = audio.getDuration();

            // Generate spectrogram
            Spectrogram spectrogram = new Spectrogram(
                    audio, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER, BINS_PER_OCTAVE, PX_PER_SECOND, 72, 1024
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
