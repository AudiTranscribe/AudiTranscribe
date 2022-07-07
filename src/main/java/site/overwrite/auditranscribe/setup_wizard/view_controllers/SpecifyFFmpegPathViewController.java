/*
 * SpecifyFFmpegPathViewController.java
 *
 * Created on 2022-06-19
 * Updated on 2022-07-07
 *
 * Description: View controller of the view that allows the user to manually specify the path to
 *              FFmpeg.
 */

package site.overwrite.auditranscribe.setup_wizard.view_controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.misc.Theme;
import site.overwrite.auditranscribe.misc.Popups;
import site.overwrite.auditranscribe.main_views.helpers.ProjectIOHandlers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * View controller of the view that allows the user to manually specify the path to FFmpeg.
 */
public class SpecifyFFmpegPathViewController implements Initializable {
    // FXML Elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private TextField ffmpegBinaryPathTextField;

    @FXML
    private Button selectFFmpegBinaryButton, checkFFmpegPathButton;

    // Initialization method
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Add methods on buttons
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

        checkFFmpegPathButton.setOnAction(event -> ((Stage) rootPane.getScene().getWindow()).close());
    }

    // Getter methods

    /**
     * Method that returns the value of <code>customFFmpegPath</code>.
     *
     * @return Value of <code>customFFmpegPath</code>.
     */
    public String getCustomFFmpegPath() {
        return ffmpegBinaryPathTextField.getText();
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
