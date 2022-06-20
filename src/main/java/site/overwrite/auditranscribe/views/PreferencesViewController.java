/*
 * PreferencesViewController.java
 *
 * Created on 2022-05-22
 * Updated on 2022-06-20
 *
 * Description: Contains the preferences view's controller class.
 */

package site.overwrite.auditranscribe.views;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import site.overwrite.auditranscribe.audio.FFmpegHandler;
import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.json_files.file_classes.SettingsFile;
import site.overwrite.auditranscribe.misc.Theme;
import site.overwrite.auditranscribe.misc.spinners.CustomDoubleSpinnerValueFactory;
import site.overwrite.auditranscribe.misc.spinners.CustomIntegerSpinnerValueFactory;
import site.overwrite.auditranscribe.spectrogram.ColourScale;
import site.overwrite.auditranscribe.views.helpers.Popups;
import site.overwrite.auditranscribe.views.helpers.ProjectIOHandlers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PreferencesViewController implements Initializable {
    // Attributes
    private String lastValidFFmpegPath;

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private SettingsFile settingsFile;

    // FXML Elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private ChoiceBox<ColourScale> colourScaleChoiceBox;

    @FXML
    private ChoiceBox<WindowFunction> windowFunctionChoiceBox;

    @FXML
    private ChoiceBox<Theme> themeChoiceBox;

    @FXML
    private Spinner<Integer> autosaveIntervalSpinner;

    @FXML
    private Spinner<Double> notePlayingDelayOffsetSpinner;

    @FXML
    private Button selectFFmpegBinaryButton;

    @FXML
    private TextField ffmpegBinaryPathTextField;

    @FXML
    private Button cancelButton, applyButton, okButton;

    // Initialization method
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set choice box selections
        for (Theme theme : Theme.values()) themeChoiceBox.getItems().add(theme);

        for (ColourScale colourScale : ColourScale.values()) colourScaleChoiceBox.getItems().add(colourScale);
        for (WindowFunction windowFunction : WindowFunction.values())
            windowFunctionChoiceBox.getItems().add(windowFunction);

        // Add methods to buttons
        selectFFmpegBinaryButton.setOnAction(event -> {
            // Define file extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                    "FFmpeg binary",
                    "*.exe", "*"  // Todo: check if works
            );

            // Get the file
            File possibleFFmpegBinary = ProjectIOHandlers.getFileFromFileDialog(
                    rootPane.getScene().getWindow(), extFilter
            );

            // Check if the FFmpeg binary is valid
            if (possibleFFmpegBinary != null) {
                // Update the value of the FFmpeg path text field
                ffmpegBinaryPathTextField.setText(possibleFFmpegBinary.getAbsolutePath());
            } else {
                Popups.showInformationAlert("Info", "No file selected.");
            }
        });

        cancelButton.setOnAction(event -> closePreferencesPane());

        applyButton.setOnAction(event -> applySettings());

        okButton.setOnAction(event -> {
            applySettings();
            closePreferencesPane();
        });

        // Add methods to text fields
        ffmpegBinaryPathTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            // Handle check for FFmpeg path only when unfocused
            if (!newValue) {
                // Get the absolute path to the FFmpeg binary
                String ffmpegBinaryPath = ffmpegBinaryPathTextField.getText();

                // Check if the FFmpeg binary is valid
                if (FFmpegHandler.checkFFmpegPath(ffmpegBinaryPath)) {
                    // Update the last valid FFmpeg path
                    lastValidFFmpegPath = ffmpegBinaryPath;

                    // Enable the apply button
                    applyButton.setDisable(false);

                    // Report success
                    logger.log(Level.INFO, "FFmpeg binary path updated to: " + ffmpegBinaryPath);
                } else {
                    // Reset the value of the text field to the last valid FFmpeg path
                    ffmpegBinaryPathTextField.setText(lastValidFFmpegPath);

                    // Show a warning message
                    Popups.showWarningAlert(
                            "Invalid FFmpeg Binary Path",
                            "The provided path does not seem to point to a valid FFmpeg binary."
                    );

                    // Report failure
                    logger.log(Level.INFO, "Selected FFmpeg binary path \"" + ffmpegBinaryPath + "\" invalid");
                }
            }
        });

        // Report that the preferences view is ready to be shown
        logger.log(Level.INFO, "Preferences view ready to be shown");
    }

    // Setter methods
    public void setSettingsFile(SettingsFile settingsFile) {
        this.settingsFile = settingsFile;
    }

    // Public methods

    /**
     * Method that sets the theme for the scene.<br>
     * Note that this method has to be called after the setting file has been set.
     *
     * @param theme The theme to set.
     */
    public void setThemeOnScene(Theme theme) {
        // Set stylesheets
        rootPane.getStylesheets().clear();  // Reset the stylesheets first before adding new ones

        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/base.css"));
        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/" + theme.cssFile));
    }

    /**
     * Method that sets the theme for the scene.<br>
     * Note that this method has to be called after the setting file has been set.
     */
    public void setThemeOnScene() {
        setThemeOnScene(Theme.values()[settingsFile.data.themeEnumOrdinal]);
    }

    /**
     * Method that sets up the fields <b>after</b> the <code>settingsFile</code> attribute has been
     * set.
     */
    public void setUpFields() {
        // Arrays that store the fields that just need to disable the apply button
        ChoiceBox<?>[] choiceBoxes = new ChoiceBox[]{colourScaleChoiceBox, windowFunctionChoiceBox};
        Spinner<?>[] spinners = new Spinner[]{notePlayingDelayOffsetSpinner, autosaveIntervalSpinner};

        // Set choice box values
        themeChoiceBox.setValue(Theme.values()[settingsFile.data.themeEnumOrdinal]);

        colourScaleChoiceBox.setValue(ColourScale.values()[settingsFile.data.colourScaleEnumOrdinal]);
        windowFunctionChoiceBox.setValue(WindowFunction.values()[settingsFile.data.windowFunctionEnumOrdinal]);

        // Add methods to choice boxes
        for (ChoiceBox<?> choiceBox : choiceBoxes) {
            choiceBox.setOnAction(event -> applyButton.setDisable(false));
        }

        themeChoiceBox.setOnAction(event -> {
            applyButton.setDisable(false);
            setThemeOnScene(themeChoiceBox.getValue());
        });

        // Set spinner factories and methods
        notePlayingDelayOffsetSpinner.setValueFactory(new CustomDoubleSpinnerValueFactory(
                -1, 1, settingsFile.data.notePlayingDelayOffset, 0.01, 2
        ));
        autosaveIntervalSpinner.setValueFactory(new CustomIntegerSpinnerValueFactory(
                1, Integer.MAX_VALUE, settingsFile.data.autosaveInterval, 1
        ));

        for (Spinner<?> spinner : spinners) {
            spinner.valueProperty().addListener((observable, oldValue, newValue) -> applyButton.setDisable(false));
        }

        // Update the last valid FFmpeg path, and set up the FFmpeg binary path text field
        lastValidFFmpegPath = settingsFile.data.ffmpegInstallationPath;
        ffmpegBinaryPathTextField.setText(lastValidFFmpegPath);
    }

    /**
     * Method that shows the preferences window.
     *
     * @param settingsFile The <code>SettingsFile</code> object that handles the reading and writing
     *                     of settings.
     */
    public static void showPreferencesWindow(SettingsFile settingsFile) {
        try {
            // Load the FXML file into the scene
            FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL("views/fxml/preferences-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Get the view controller
            PreferencesViewController controller = fxmlLoader.getController();

            // Update the `settingsFile` attribute
            controller.setSettingsFile(settingsFile);

            // Set the theme of the scene
            controller.setThemeOnScene();

            // Set choice boxes' values
            controller.setUpFields();

            // Set stage properties
            Stage preferencesStage = new Stage();
            preferencesStage.initStyle(StageStyle.UTILITY);
            preferencesStage.setTitle("Settings and Preferences");
            preferencesStage.setScene(scene);
            preferencesStage.setResizable(false);

            // Show the stage
            preferencesStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Private methods

    /**
     * Helper method that closes the preferences' pane window.
     */
    private void closePreferencesPane() {
        ((Stage) rootPane.getScene().getWindow()).close();
    }

    /**
     * Helper method that updates the settings file with the new settings.
     */
    private void applySettings() {
        // Update settings' values
        settingsFile.data.themeEnumOrdinal = themeChoiceBox.getValue().ordinal();

        settingsFile.data.notePlayingDelayOffset = notePlayingDelayOffsetSpinner.getValue();
        settingsFile.data.ffmpegInstallationPath = ffmpegBinaryPathTextField.getText();

        settingsFile.data.autosaveInterval = autosaveIntervalSpinner.getValue();

        settingsFile.data.colourScaleEnumOrdinal = colourScaleChoiceBox.getValue().ordinal();
        settingsFile.data.windowFunctionEnumOrdinal = windowFunctionChoiceBox.getValue().ordinal();

        // Apply settings to the settings file
        settingsFile.saveFile();

        // Disable the apply button again
        applyButton.setDisable(true);
    }
}
