/*
 * PreferencesViewController.java
 *
 * Created on 2022-05-22
 * Updated on 2022-05-22
 *
 * Description: Contains the preferences view's controller class.
 */

package site.overwrite.auditranscribe.views;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Stage;
import site.overwrite.auditranscribe.io.IOMethods;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PreferencesViewController implements Initializable {
    // Attributes
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    // Initialization method
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.log(Level.INFO, "Preferences view ready to be shown");
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
