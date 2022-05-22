/*
 * PreferencesViewController.java
 *
 * Created on 2022-05-22
 * Updated on 2022-05-22
 *
 * Description: Contains the preferences view's controller class.
 */

package site.overwrite.auditranscribe.views;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.settings_file.SettingsFile;
import site.overwrite.auditranscribe.spectrogram.ColourScale;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PreferencesViewController implements Initializable {
    // Attributes
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final SettingsFile settingsFile = new SettingsFile();

    // FXML Elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private ChoiceBox<ColourScale> colourScaleChoiceBox;

    @FXML
    private ChoiceBox<WindowFunction> windowFunctionChoiceBox;

    // Initialization method
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Add CSS stylesheets to the scene
        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/base.css"));
        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/light-mode.css"));  // Todo: add theme support

        // Set choice box selections
        for (ColourScale colourScale : ColourScale.values()) colourScaleChoiceBox.getItems().add(colourScale);
        for (WindowFunction windowFunction : WindowFunction.values())
            windowFunctionChoiceBox.getItems().add(windowFunction);

        // Set choices
        colourScaleChoiceBox.setValue(ColourScale.values()[settingsFile.settingsData.colourScaleEnumOrdinal]);
        windowFunctionChoiceBox.setValue(WindowFunction.values()[settingsFile.settingsData.windowFunctionEnumOrdinal]);

        // Report that the preferences view is ready to be shown
        logger.log(Level.INFO, "Preferences view ready to be shown");

        // Todo: add buttons' methods
    }

    // Public methods
    /**
     * Method that shows the preferences window.
     */
    public static void showPreferencesWindow() {
        try {
            // Load the FXML file into the scene
            FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL("views/fxml/preferences-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Set stage properties
            Stage aboutStage = new Stage();
            aboutStage.setTitle("Settings and Preferences");
            aboutStage.setScene(scene);
            aboutStage.setResizable(false);

            // Show the stage
            aboutStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
