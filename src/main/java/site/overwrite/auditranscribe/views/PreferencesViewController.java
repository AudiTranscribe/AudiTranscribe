/*
 * PreferencesViewController.java
 *
 * Created on 2022-05-22
 * Updated on 2022-06-12
 *
 * Description: Contains the preferences view's controller class.
 */

package site.overwrite.auditranscribe.views;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import site.overwrite.auditranscribe.audio.WindowFunction;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.json_files.file_classes.SettingsFile;
import site.overwrite.auditranscribe.misc.Theme;
import site.overwrite.auditranscribe.spectrogram.ColourScale;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PreferencesViewController implements Initializable {
    // Attributes
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
        cancelButton.setOnAction(event -> closePreferencesPane());

        applyButton.setOnAction(event -> applySettings());

        okButton.setOnAction(event -> {
            applySettings();
            closePreferencesPane();
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
        // Set choice box values
        themeChoiceBox.setValue(Theme.values()[settingsFile.data.themeEnumOrdinal]);

        colourScaleChoiceBox.setValue(ColourScale.values()[settingsFile.data.colourScaleEnumOrdinal]);
        windowFunctionChoiceBox.setValue(WindowFunction.values()[settingsFile.data.windowFunctionEnumOrdinal]);

        // Add methods to choice boxes
        for (ChoiceBox<?> choiceBox : new ChoiceBox[]{colourScaleChoiceBox, windowFunctionChoiceBox}) {
            choiceBox.setOnAction(event -> applyButton.setDisable(false));
        }

        themeChoiceBox.setOnAction(event -> {
            applyButton.setDisable(false);
            setThemeOnScene(themeChoiceBox.getValue());
        });

        // Set spinner factories and methods
        autosaveIntervalSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1, Integer.MAX_VALUE, settingsFile.data.autosaveInterval, 1
        ));

        autosaveIntervalSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyButton.setDisable(false);
        });
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

        settingsFile.data.colourScaleEnumOrdinal = colourScaleChoiceBox.getValue().ordinal();
        settingsFile.data.windowFunctionEnumOrdinal = windowFunctionChoiceBox.getValue().ordinal();

        settingsFile.data.autosaveInterval = autosaveIntervalSpinner.getValue();

        // Apply settings to the settings file
        settingsFile.saveFile();

        // Disable the apply button again
        applyButton.setDisable(true);
    }
}
