/*
 * SpectrogramViewController.java
 *
 * Created on 2022-02-12
 * Updated on 2022-04-23
 *
 * Description: Contains the spectrogram view's controller class.
 */

package site.overwrite.auditranscribe.views;

import javafx.application.Platform;
import javafx.concurrent.Task;
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

    final Pair<Integer, Integer> BPM_RANGE = new Pair<>(1, 512);  // In the format [min, max]
    final Pair<Double, Double> OFFSET_RANGE = new Pair<>(-5., 5.);  // In the format [min, max]

    final double SPECTROGRAM_ZOOM_SCALE_X = 2;
    final double SPECTROGRAM_ZOOM_SCALE_Y = 5;
    final int PX_PER_SECOND = 120;
    final int BINS_PER_OCTAVE = 60;

    final int MIN_NOTE_NUMBER = 0;  // C0
    final int MAX_NOTE_NUMBER = 119;  // B9

    final int UPDATE_SCROLLBAR_INTERVAL = 5;  // In milliseconds

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
    private ScrollPane leftPane, spectrogramPane;

    @FXML
    private AnchorPane leftPaneAnchor, spectrogramPaneAnchor;

    @FXML
    private Pane notePane;

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
        // Remove any leading zeros
        bpmField.setText(bpmField.getText().replaceFirst("^0+(?!$)", ""));

        // Get the processed string from the BPM field
        String bpmFieldValue = bpmField.getText();

        // Run validation on the BPM field text
        if (!validateBPMField(bpmFieldValue)) return;

        // Convert the field value into the new BPM value
        int newBPM = Integer.parseInt(bpmFieldValue);

        // Update the beat lines
        beatLines = SpectrogramStuffHandler.updateBeatLines(
                spectrogramPaneAnchor, beatLines, audioDuration, bpm, newBPM, offset, offset, finalHeight, beatsPerBar,
                PX_PER_SECOND, SPECTROGRAM_ZOOM_SCALE_X
        );

        // Update the BPM value
        bpm = newBPM;
    }

    @FXML
    protected void onFinishedEditingOffsetField() {
        // Remove any leading zeros, except when there is one zero followed by a decimal point which we will keep
        offsetField.setText(offsetField.getText().replaceFirst("^0{2,}(?!$)", "0"));

        // Get the string from the offset field
        String offsetFieldValue = offsetField.getText();

        // Run validation on the offset field text
        if (!validateOffsetField(offsetFieldValue)) return;

        // Todo: update offset stuff
        System.out.println("Update offset stuff");
    }

    // Initialization function
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get the audio file
        try {
            Audio audio = new Audio("testing-audio-files/A440.wav");

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

            // Update spectrogram plane width and height
            spectrogramPaneAnchor.setPrefWidth(finalWidth);
            spectrogramPaneAnchor.setPrefHeight(finalHeight);

            // Set note pane scrolling
            Task<Void> notePaneScrollingTask = new Task<>() {
                @Override
                public Void call() throws Exception {
                    while (true) {  // Fixme: `while` statement cannot complete without throwing an exception
                        Platform.runLater(() -> leftPane.setVvalue(spectrogramPane.getVvalue()));
                        Thread.sleep(UPDATE_SCROLLBAR_INTERVAL);  // Fixme: Call to `Thread.sleep()` in a loop, probably busy-waiting
                    }
                }
            };

            Thread notePaneScrollingThread = new Thread(notePaneScrollingTask);
            notePaneScrollingThread.setDaemon(true);
            notePaneScrollingThread.start();

            // Set the choice boxes' choices
            for (String musicKey : MUSIC_KEYS) {
                musicKeyChoice.getItems().add(musicKey);
            }

            for (String key : TIME_SIGNATURE_TO_BEATS_PER_BAR.keySet()) {
                timeSignatureChoice.getItems().add(key);
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

            // Set image on the spectrogram area
            spectrogramImage.setFitHeight(finalWidth);
            spectrogramImage.setFitWidth(finalHeight);
            spectrogramImage.setImage(image);

            // Add note labels and note lines
            SpectrogramStuffHandler.addNoteLabels(notePane, finalHeight, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER);
            SpectrogramStuffHandler.addNoteLines(spectrogramPaneAnchor, finalHeight, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER);

            // Generate the beat lines and update the attribute
            beatLines = SpectrogramStuffHandler.getBeatLines(
                    bpm, beatsPerBar, PX_PER_SECOND, finalHeight, audioDuration, offset, SPECTROGRAM_ZOOM_SCALE_X
            );
            SpectrogramStuffHandler.addBeatLines(spectrogramPaneAnchor, beatLines);

            // Resize image pane
            spectrogramImage.setFitWidth(finalWidth);
            spectrogramImage.setFitHeight(finalHeight);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
