/*
 * InitialViewController.java
 *
 * Created on 2022-06-19
 * Updated on 2022-08-27
 *
 * Description: View controller that handles the initial view for the setup wizard.
 */

package site.overwrite.auditranscribe.setup_wizard.view_controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.misc.MyLogger;
import site.overwrite.auditranscribe.misc.Theme;
import site.overwrite.auditranscribe.setup_wizard.SetupWizard;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * View controller that handles the initial view for the FFmpeg installation procedure.
 */
public class InitialViewController implements Initializable {
    // Attributes
    private boolean hasFFmpegInstalled = false;

    // FXML Elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private Button hasFFmpegButton, doNotHaveFFmpegButton;

    // Initialization method
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Add methods on buttons
        hasFFmpegButton.setOnMouseClicked(event -> {
            hasFFmpegInstalled = true;
            ((Stage) rootPane.getScene().getWindow()).close();
        });
        doNotHaveFFmpegButton.setOnMouseClicked(event -> {
            hasFFmpegInstalled = false;
            ((Stage) rootPane.getScene().getWindow()).close();
        });

        MyLogger.log(Level.INFO, "Showing setup wizard initial view", this.getClass().getName());
    }

    // Getter methods

    /**
     * Method that returns the value of <code>hasFFmpegInstalled</code>.
     *
     * @return Value of <code>hasFFmpegInstalled</code>.
     */
    public boolean getHasFFmpegInstalled() {
        return hasFFmpegInstalled;
    }

    // Public methods

    /**
     * Method that sets the scene's theme.
     *
     * @param theme Theme to set.
     */
    public void setThemeOnScene(Theme theme) {
        rootPane.getStylesheets().clear();  // Reset the stylesheets first before adding new ones

        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/base.css"));
        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/" + theme.cssFile));
    }
}
