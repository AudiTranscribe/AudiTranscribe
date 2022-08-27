/*
 * FixNoteDelayViewController.java
 *
 * Created on 2022-08-24
 * Updated on 2022-08-27
 *
 * Description: View controller that helps the user set up the update checking interval.
 */

package site.overwrite.auditranscribe.setup_wizard.view_controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.misc.Theme;
import site.overwrite.auditranscribe.misc.spinners.CustomIntegerSpinnerValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * View controller that helps the user set up the update checking interval.
 */
public class UpdateIntervalSetupViewController implements Initializable {
    // FXML elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private Spinner<Integer> updateCheckingIntervalSpinner;

    @FXML
    private Button confirmButton;

    // Initialization method
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        updateCheckingIntervalSpinner.setValueFactory(new CustomIntegerSpinnerValueFactory(
                1, 720, 24, 1
        ));
        confirmButton.setOnAction(event -> ((Stage) rootPane.getScene().getWindow()).close());
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

    /**
     * Method that gets the update checking interval.
     * @return An integer, representing the update checking interval.
     */
    public int getUpdateCheckingInterval() {
        return updateCheckingIntervalSpinner.getValue();
    }
}
