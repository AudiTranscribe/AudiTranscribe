/*
 * AskToInstallManuallyController.java
 *
 * Created on 2022-07-10
 * Updated on 2022-08-27
 *
 * Description: View controller of the view that asks the user whether to install FFmpeg manually or
 *              automatically.
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

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * View controller of the view that asks the user whether to install FFmpeg manually or
 * automatically.
 */
public class AskToInstallManuallyViewController implements Initializable {
    // Attributes
    private boolean isManualInstallation;

    // FXML Elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private Button manualButton, automaticButton;

    // Initialization method
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        manualButton.setOnMouseClicked(event -> {
            isManualInstallation = true;
            ((Stage) rootPane.getScene().getWindow()).close();
        });
        automaticButton.setOnMouseClicked(event -> {
            isManualInstallation = false;
            ((Stage) rootPane.getScene().getWindow()).close();
        });

        MyLogger.log(Level.INFO, "Asking whether to install FFmpeg automatically", this.getClass().getName());
    }

    // Getter methods

    /**
     * Method that returns the value of <code>isManualInstallation</code>.
     *
     * @return Value of <code>isManualInstallation</code>.
     */
    public boolean getIsManualInstallation() {
        return isManualInstallation;
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
