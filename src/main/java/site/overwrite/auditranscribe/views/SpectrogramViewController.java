/*
 * SpectrogramViewController.java
 *
 * Created on 2022-02-12
 * Updated on 2022-03-15
 *
 * Description: Contains the spectrogram view's controller class.
 */

package site.overwrite.auditranscribe.views;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.Window;
import site.overwrite.auditranscribe.spectrogram.ColourScale;
import site.overwrite.auditranscribe.spectrogram.Spectrogram;

import java.net.URL;
import java.util.ResourceBundle;

public class SpectrogramViewController  implements Initializable {
    @FXML
    private AnchorPane spectrogramPlane;

    @FXML
    private ImageView spectrogramArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get the audio file
        Audio audio = new Audio("audioFiles/Choice.wav");

        // Generate spectrogram
        Spectrogram spectrogram = new Spectrogram(audio, 25, 512, 1024);
        WritableImage spectrogramImage = spectrogram.generateSpectrogram(Window.HANN_WINDOW, ColourScale.VIRIDIS);
        System.out.println("Width is " + spectrogram.width);

        // Update spectrogram plane width and height
        spectrogramPlane.setPrefWidth(spectrogram.width);
        spectrogramPlane.setPrefHeight(spectrogram.height);

        // Set image on the spectrogram area
        spectrogramArea.setFitHeight(spectrogram.height);
        spectrogramArea.setFitWidth(spectrogram.width);
        spectrogramArea.setImage(spectrogramImage);
    }
}
