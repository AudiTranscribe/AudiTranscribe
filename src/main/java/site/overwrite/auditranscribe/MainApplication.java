/*
 * MainApplication.java
 *
 * Created on 2022-02-09
 * Updated on 2022-06-06
 *
 * Description: Contains the main application class.
 */

package site.overwrite.auditranscribe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.json_files.file_classes.SettingsFile;
import site.overwrite.auditranscribe.views.helpers.FFmpegInstallationPopupsHandler;
import site.overwrite.auditranscribe.views.MainViewController;

import java.io.IOException;

public class MainApplication extends Application {
    // Attributes
    private final SettingsFile settingsFile = new SettingsFile();

    // Initialization method
    @Override
    public void start(Stage stage) throws IOException {
        // Ensure that an application folder exists
        IOMethods.createAppDataFolder();

        // Ask user for FFmpeg installation
        FFmpegInstallationPopupsHandler.showFFmpegInstallationView(settingsFile);

        // Load the FXML file into the scene
        FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL("views/fxml/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        // Get the view controller
        MainViewController controller = fxmlLoader.getController();

        // Set the settings file on the main scene
        controller.setSettingsFile(settingsFile);

        // Set the theme of the scene
        controller.setThemeOnScene();

        // Set stage properties
        stage.setTitle("Welcome to AudiTranscribe");
        stage.setScene(scene);
        stage.setResizable(false);

        // Show the stage
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
