/*
 * FinishSetupViewController.java
 *
 * Created on 2022-06-19
 * Updated on 2022-07-07
 *
 * Description: View controller for the view that signals the end of the setup process.
 */

package site.overwrite.auditranscribe.setup_wizard.view_controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.misc.Theme;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * View controller for the view that signals the end of the setup process.
 */
public class FinishSetupViewController implements Initializable {
    // FXML Elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private Button closeButton;

    // Initialize method
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        closeButton.setOnAction(event -> ((Stage) rootPane.getScene().getWindow()).close());
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
