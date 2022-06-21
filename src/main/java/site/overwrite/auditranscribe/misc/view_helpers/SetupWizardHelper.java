/*
 * SetupWizardHelper.java
 *
 * Created on 2022-06-19
 * Updated on 2022-06-21
 *
 * Description: Class that handles the setup wizard.
 */

package site.overwrite.auditranscribe.misc.view_helpers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import site.overwrite.auditranscribe.audio.FFmpegHandler;
import site.overwrite.auditranscribe.exceptions.FFmpegNotFoundException;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.StreamGobbler;
import site.overwrite.auditranscribe.io.json_files.file_classes.SettingsFile;
import site.overwrite.auditranscribe.misc.Theme;
import site.overwrite.auditranscribe.views.setup_wizard_views.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Executors;

/**
 * Class that handles the setup wizard.
 */
public class SetupWizardHelper {
    // Attributes
    Stage stage;
    SettingsFile settingsFile;
    Theme theme;

    /**
     * Initializes the setup wizard helper.
     *
     * @param settingsFile The settings file.
     */
    public SetupWizardHelper(SettingsFile settingsFile) {
        // Set attributes
        this.stage = new Stage(StageStyle.UTILITY);
        this.settingsFile = settingsFile;
        this.theme = Theme.values()[settingsFile.data.themeEnumOrdinal];

        // Set stage properties
        stage.setTitle("Setup Wizard");
        stage.setResizable(false);
    }

    // Public methods
    public void showSetupWizard() {
        // Check if the setup is complete
        if (settingsFile.data.isSetupCompleted) return;  // Don't need to perform setup again

        // Show the initial view for the setup wizard
        boolean userSayFFmpegInstalled = showInitialView();

        // Define variables
        boolean isFFmpegInstalled = false;
        boolean usingCustomInstallation = false;
        boolean canFindFFmpeg = true;
        String ffmpegPath = null;

        // Repeat the following until FFmpeg has been successfully installed
        while (!isFFmpegInstalled) {
            // Show the installation instructions for FFmpeg
            if (!userSayFFmpegInstalled && !usingCustomInstallation) showInstallingFFmpegView();

            // Attempt to find the FFmpeg binary
            try {
                // Immediately throw an exception if the program can't find FFmpeg in previous loop
                if (!canFindFFmpeg) {
                    throw new FFmpegNotFoundException("FFmpeg not found at manually specified path.");
                }

                // Attempt to get the path to the FFmpeg binary and save to persistent data file
                ffmpegPath = getPathToFFmpeg();

                // Update the `isFFmpegInstalled` flag
                isFFmpegInstalled = true;

            } catch (FFmpegNotFoundException e) {
                // Show view reporting that FFmpeg could not be found
                boolean isSpecifyManually = showCannotFindFFmpegView();

                if (isSpecifyManually) {
                    // Update the `usingCustomInstallation` variable
                    usingCustomInstallation = true;

                    // Ask user to specify the path to FFmpeg
                    ffmpegPath = showSpecifyFFmpegPathView();

                } else {
                    // Update variables
                    userSayFFmpegInstalled = false;
                    usingCustomInstallation = false;

                    // Restart loop
                    continue;
                }

                // Check the custom FFmpeg path for the FFmpeg binary
                if (ffmpegPath != null) {
                    if (FFmpegHandler.checkFFmpegPath(ffmpegPath)) {
                        isFFmpegInstalled = true;
                    } else {
                        // Note that FFmpeg cannot be found
                        canFindFFmpeg = false;
                    }
                }
            }
        }

        // Report that the setup process has completed
        showFinishSetupView();

        // Update settings file data
        settingsFile.data.ffmpegInstallationPath = ffmpegPath;
        settingsFile.data.isSetupCompleted = true;
        settingsFile.saveFile();
    }

    // Private methods

    /**
     * Helper method that shows the initial view of the setup wizard.
     *
     * @return Boolean representing whether the user has indicated that they have installed FFmpeg
     * (or not).
     */
    private boolean showInitialView() {
        try {
            // Load the FXML file into the scene
            FXMLLoader fxmlLoader = new FXMLLoader(getSetupWizardView("initial-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Get the view controller
            InitialViewController controller = fxmlLoader.getController();

            // Set the theme on the scene
            controller.setThemeOnScene(theme);

            // Set the stage's scene
            stage.setScene(scene);

            // Show the stage
            stage.showAndWait();

            // Return the value of the `hasFFmpegInstalled` flag
            return controller.getHasFFmpegInstalled();

        } catch (IOException ignored) {
        }
        return false;
    }

    /**
     * Helper method that shows the view of the instructions on how to install FFmpeg.
     */
    private void showInstallingFFmpegView() {
        try {
            // Load the FXML file into the scene
            FXMLLoader fxmlLoader = new FXMLLoader(getSetupWizardView("installing-ffmpeg-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Get the view controller
            InstallingFFmpegViewController controller = fxmlLoader.getController();

            // Set the theme on the scene
            controller.setThemeOnScene(theme);

            // Set the stage's scene
            stage.setScene(scene);

            // Show the stage
            stage.showAndWait();

        } catch (IOException ignored) {
        }
    }

    /**
     * Helper method that shows a view that reports the FFmpeg could not be found.
     *
     * @return Boolean representing whether the user chooses whether to specify the FFmpeg path
     * manually, or not (which results in the FFmpeg installation instructions shown again).
     */
    private boolean showCannotFindFFmpegView() {
        try {
            // Load the FXML file into the scene
            FXMLLoader fxmlLoader = new FXMLLoader(getSetupWizardView("cannot-find-ffmpeg-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Get the view controller
            CannotFindFFmpegViewController controller = fxmlLoader.getController();

            // Set the theme on the scene
            controller.setThemeOnScene(theme);

            // Set the stage's scene
            stage.setScene(scene);

            // Show the stage
            stage.showAndWait();

            // Return the value of the `isSpecifyManually` flag
            return controller.getIsSpecifyManually();

        } catch (IOException ignored) {
        }
        return false;
    }

    /**
     * Helper method that shows a view that allows the user to specify the FFmpeg path manually.
     *
     * @return String representing the path to the FFmpeg binary.
     */
    private String showSpecifyFFmpegPathView() {
        try {
            // Load the FXML file into the scene
            FXMLLoader fxmlLoader = new FXMLLoader(getSetupWizardView("specify-ffmpeg-path-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Get the view controller
            SpecifyFFmpegPathViewController controller = fxmlLoader.getController();

            // Set the theme on the scene
            controller.setThemeOnScene(theme);

            // Set the stage's scene
            stage.setScene(scene);

            // Show the stage
            stage.showAndWait();

            // Return the value of the `customFFmpegPath`
            return controller.getCustomFFmpegPath();

        } catch (IOException ignored) {
        }
        return null;
    }

    /**
     * Helper method that shows the view that marks the end of the setup process.
     */
    private void showFinishSetupView() {
        try {
            // Load the FXML file into the scene
            FXMLLoader fxmlLoader = new FXMLLoader(getSetupWizardView("finish-setup-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Get the view controller
            FinishSetupViewController controller = fxmlLoader.getController();

            // Set the theme on the scene
            controller.setThemeOnScene(theme);

            // Set the stage's scene
            stage.setScene(scene);

            // Show the stage
            stage.showAndWait();

        } catch (IOException ignored) {
        }
    }

    /**
     * Helper method that gets the URL for a setup wizard view.
     *
     * @param viewFile The name and extension of the view file.
     * @return The URL of the view file.
     */
    private URL getSetupWizardView(String viewFile) {
        return IOMethods.getFileURL("views/fxml/setup-wizard-views/" + viewFile);
    }

    /**
     * Helper method that attempts to find the FFmpeg installation path automatically by using the
     * command-line interface of FFmpeg.
     *
     * @return A string, representing the FFmpeg installation path.
     * @throws FFmpegNotFoundException If the program fails to find the FFmpeg installation.
     */
    private static String getPathToFFmpeg() throws FFmpegNotFoundException {
        // Check the operating system
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        // Generate the command to execute
        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command("cmd.exe", "/c", "where ffmpeg");
        } else {
            builder.command("sh", "-c", "which ffmpeg");
        }

        // Specify the working directory
        builder.directory(new File(System.getProperty("user.home")));

        // Define variables
        final String[] ffmpegPath = new String[1];
        try {
            // Build the process
            Process process = builder.start();

            // Define stream gobbler
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), s -> ffmpegPath[0] = s);

            // Start the process
            Executors.newSingleThreadExecutor().submit(streamGobbler);

            // Check exit code of the command
            int exitCode = process.waitFor();
            if (exitCode != 0) throw new FFmpegNotFoundException("FFmpeg binary cannot be located.\n" + ffmpegPath[0]);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Return the ffmpeg path
        return ffmpegPath[0];
    }
}
