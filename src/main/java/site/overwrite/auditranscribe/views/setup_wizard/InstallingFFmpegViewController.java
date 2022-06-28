/*
 * InstallingFFmpegViewController.java
 *
 * Created on 2022-06-19
 * Updated on 2022-06-28
 *
 * Description: View controller that show instructions on how to install FFmpeg.
 */

package site.overwrite.auditranscribe.views.setup_wizard;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.misc.Theme;
import site.overwrite.auditranscribe.utils.GUIUtils;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * View controller that show instructions on how to install FFmpeg.
 */
public class InstallingFFmpegViewController implements Initializable {
    // FXML Elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private Hyperlink ffmpegHyperlink;

    @FXML
    private Button confirmButton;

    // Initialization method
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set hyperlink methods
        ffmpegHyperlink.setOnAction(event -> GUIUtils.openURLInBrowser("https://ffmpeg.org/"));

        // Add button methods
        confirmButton.setOnAction(event -> ((Stage) rootPane.getScene().getWindow()).close());
    }

    // Public methods

    /**
     * Method that sets the scene's theme.
     * @param theme Theme to set.
     */
    public void setThemeOnScene(Theme theme) {
        rootPane.getStylesheets().clear();  // Reset the stylesheets first before adding new ones

        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/base.css"));
        rootPane.getStylesheets().add(IOMethods.getFileURLAsString("views/css/" + theme.cssFile));
    }
}
