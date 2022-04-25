/*
 * SpectrogramViewController.java
 *
 * Created on 2022-02-12
 * Updated on 2022-04-25
 *
 * Description: Contains the spectrogram view's controller class.
 */

package site.overwrite.auditranscribe.views;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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
import site.overwrite.auditranscribe.utils.StyleUtils;
import site.overwrite.auditranscribe.utils.ValidationUtils;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
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
    private TextField bpmField, offsetField;

    // Mid-view
    @FXML
    private ScrollPane leftPane, spectrogramPane, bottomPane;

    @FXML
    private AnchorPane leftPaneAnchor, spectrogramPaneAnchor, bottomPaneAnchor;

    @FXML
    private Pane notePane, barNumberPane;

    @FXML
    private ImageView spectrogramImage;

    // Validation methods
    boolean validateBPMField(String bpmFieldValue) {
        // Check if the `bpmFieldValue` is empty
        if (ValidationUtils.isEmpty(bpmFieldValue)) {
            bpmField.setStyle(StyleUtils.ERROR_BORDER);
            logger.log(Level.INFO, "Validation: BPM field empty");
            return false;
        }

        // Ensure that the `bpmFieldValue` is an integer
        if (!ValidationUtils.isStringInteger(bpmFieldValue)) {
            bpmField.setStyle(StyleUtils.ERROR_FIELD);
            logger.log(Level.INFO, "Validation: BPM field not an integer");
            return false;
        }

        // Ensure that the BPM value lies within the accepted range
        int bpmValue = Integer.parseInt(bpmFieldValue);

        if (!ValidationUtils.isInRange(bpmValue, BPM_RANGE.getKey(), BPM_RANGE.getValue())) {
            bpmField.setStyle(StyleUtils.ERROR_FIELD);
            logger.log(Level.INFO, "Validation: BPM field value out of range");
            return false;
        }

        // All checks passed; reset field style
        logger.log(Level.FINE, "BPM field value updated to " + bpmFieldValue);
        bpmField.setStyle(null);
        return true;
    }

    boolean validateOffsetField(String offsetFieldValue) {
        // Check if the `offsetFieldValue` is empty
        if (ValidationUtils.isEmpty(offsetFieldValue)) {
            offsetField.setStyle(StyleUtils.ERROR_BORDER);
            logger.log(Level.INFO, "Validation: Offset field empty");
            return false;
        }

        // Ensure that the `offsetFieldValue` is a double
        if (!ValidationUtils.isStringDouble(offsetFieldValue)) {
            offsetField.setStyle(StyleUtils.ERROR_FIELD);
            logger.log(Level.INFO, "Validation: Offset field not a double");
            return false;
        }

        // Ensure that the BPM value lies within the accepted range
        double offsetValue = Double.parseDouble(offsetFieldValue);

        if (!ValidationUtils.isInRange(offsetValue, OFFSET_RANGE.getKey(), OFFSET_RANGE.getValue())) {
            offsetField.setStyle(StyleUtils.ERROR_FIELD);
            logger.log(Level.INFO, "Validation: Offset field value out of range");
            return false;
        }

        // All checks passed; reset field style
        logger.log(Level.FINE, "Offset field value updated to " + offsetFieldValue);
        offsetField.setStyle(null);
        return true;
    }

    // FXML Methods
    @FXML
    protected void onFinishedEditingBPMField() {
        // Get the processed string from the BPM field
        String bpmFieldValue = bpmField.getText();

        // Run validation on the BPM field text
        if (!validateBPMField(bpmFieldValue)) return;

        // Convert the field value into the new BPM value
        int newBPM = Integer.parseInt(bpmFieldValue);

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

        // Set the field text to the new BPM value
        bpmField.setText(newBPM + "");  // `newBPM` is the BPM but with no leading zeros
    }

    @FXML
    protected void onFinishedEditingOffsetField() {
        // Get the string from the offset field
        String offsetFieldValue = offsetField.getText();

        // Run validation on the offset field text
        if (!validateOffsetField(offsetFieldValue)) return;

        // Convert the field value into the new offset
        double newOffset = Double.parseDouble(offsetFieldValue);

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

        // Set the field text to the new offset value
        offsetField.setText(newOffset + "");  // `newOffset` is the offset but with no leading nor trailing zeros
    }

    // Initialization function
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get the audio file
        try {
//            Audio audio = new Audio("testing-audio-files/A440.wav");
            Audio audio = new Audio("testing-audio-files/Melancholy.wav");

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

            // Set the choice boxes' choices
            for (String musicKey : MUSIC_KEYS) {
                musicKeyChoice.getItems().add(musicKey);
            }

            for (String timeSignature : TIME_SIGNATURES) {
                timeSignatureChoice.getItems().add(timeSignature);
            }

            musicKeyChoice.setValue("C");
            timeSignatureChoice.setValue("4/4");

            // Set methods on text fields
            bpmField.textProperty().addListener((observable, oldValue, newValue) -> {
                // Remove/prevent entering of non-digit characters
                if (!newValue.matches("\\d*")) {
                    bpmField.setText(newValue.replaceAll("\\D+", ""));
                }

                // Validate what was entered inside the BPM field
                validateBPMField(bpmField.getText());
            });  // See https://stackoverflow.com/a/30796829

            offsetField.textProperty().addListener((observable, oldValue, newValue) -> {
                // Remove/prevent entering of non-digit characters (except for the decimal point and a minus sign)
                if (!newValue.matches("[\\d.-]*")) {
                    offsetField.setText(newValue.replaceAll("[^\\d.-]+", ""));
                }

                // Validate what was entered inside the offset field
                validateOffsetField(offsetField.getText());
            });

            bpmField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {  // Lost focus
                    onFinishedEditingBPMField();
                }
            });

            offsetField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {  // Lost focus
                    onFinishedEditingOffsetField();
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
