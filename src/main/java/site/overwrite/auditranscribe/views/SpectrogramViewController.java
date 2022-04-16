/*
 * SpectrogramViewController.java
 *
 * Created on 2022-02-12
 * Updated on 2022-04-16
 *
 * Description: Contains the spectrogram view's controller class.
 */

package site.overwrite.auditranscribe.views;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.Window;
import site.overwrite.auditranscribe.plotting.NoteStuffAdder;
import site.overwrite.auditranscribe.spectrogram.ColourScale;
import site.overwrite.auditranscribe.spectrogram.Spectrogram;

import java.net.URL;
import java.util.ResourceBundle;

public class SpectrogramViewController implements Initializable {
    // Attributes
    final double SPECTROGRAM_ZOOM_SCALE_X = 2;
    final double SPECTROGRAM_ZOOM_SCALE_Y = 5;
    final int BINS_PER_OCTAVE = 60;

    final int MIN_NOTE_NUMBER = 0;  // C0
    final int MAX_NOTE_NUMBER = 119;  // B9

    final int UPDATE_SCROLLBAR_INTERVAL = 5;  // In milliseconds

    // FXML Elements
    @FXML
    private ScrollPane leftPane;

    @FXML
    private AnchorPane leftPaneAnchor;

    @FXML
    private ScrollPane spectrogramPane;

    @FXML
    private AnchorPane spectrogramPaneAnchor;

    @FXML
    private Pane notePane;

    @FXML
    private ImageView spectrogramImage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get the audio file
        try {
            Audio audio = new Audio("testing-audio-files/Tones.wav");

            // Generate spectrogram
            Spectrogram spectrogram = new Spectrogram(audio, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER, BINS_PER_OCTAVE,120, 72, 1024);
            WritableImage image = spectrogram.generateSpectrogram(Window.HANN_WINDOW, ColourScale.VIRIDIS);

            // Get the final width and height
            double finalWidth = image.getWidth() * SPECTROGRAM_ZOOM_SCALE_X;
            double finalHeight = image.getHeight() * SPECTROGRAM_ZOOM_SCALE_Y;

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

            // Set image on the spectrogram area
            spectrogramImage.setFitHeight(finalWidth);
            spectrogramImage.setFitWidth(finalHeight);
            spectrogramImage.setImage(image);

            // Add note labels
            NoteStuffAdder.addNoteLabels(notePane, finalHeight, MIN_NOTE_NUMBER, MAX_NOTE_NUMBER);

            // Resize image pane
            spectrogramImage.setFitWidth(finalWidth);
            spectrogramImage.setFitHeight(finalHeight);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
