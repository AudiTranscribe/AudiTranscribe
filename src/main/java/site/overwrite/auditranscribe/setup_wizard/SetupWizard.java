/*
 * SetupWizard.java
 *
 * Created on 2022-06-19
 * Updated on 2022-07-10
 *
 * Description: Class that handles the setup wizard.
 */

package site.overwrite.auditranscribe.setup_wizard;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import site.overwrite.auditranscribe.audio.FFmpegHandler;
import site.overwrite.auditranscribe.exceptions.audio.FFmpegNotFoundException;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.json_files.file_classes.SettingsFile;
import site.overwrite.auditranscribe.misc.Theme;
import site.overwrite.auditranscribe.setup_wizard.view_controllers.*;

import java.io.IOException;
import java.net.URL;

/**
 * Class that handles the setup wizard.
 */
public class SetupWizard {
    // Attributes
    private final Stage stage;
    private final SettingsFile settingsFile;
    private final Theme theme;

    /**
     * Initializes the setup wizard helper.
     *
     * @param settingsFile The settings file.
     */
    public SetupWizard(SettingsFile settingsFile) {
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

        // If user says that they have not installed FFmpeg, show relevant scenes
        if (!userSayFFmpegInstalled) {
            if (!showAskToInstallManuallyView()) {  // Automatic installation
                ffmpegPath = showDownloadingFFmpegView();
                userSayFFmpegInstalled = true;
            }
        }

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

                // Attempt to get the path to the FFmpeg binary
                if (ffmpegPath == null) ffmpegPath = FFmpegHandler.getPathToFFmpeg();

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
     * Helper method that shows the view that asks the user whether to install FFmpeg manually or automatically.
     *
     * @return A boolean. Is <code>true</code> if <b>manual</b> installation is selected, and
     * <code>false</code> if <b>automatic</b> installation is selected.
     */
    private boolean showAskToInstallManuallyView() {
        try {
            // Load the FXML file into the scene
            FXMLLoader fxmlLoader = new FXMLLoader(getSetupWizardView("ask-to-install-manually-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Get the view controller
            AskToInstallManuallyViewController controller = fxmlLoader.getController();

            // Set the theme on the scene
            controller.setThemeOnScene(theme);

            // Set the stage's scene
            stage.setScene(scene);

            // Show the stage
            stage.showAndWait();

            // Return the value of the `isManualInstallation` flag
            return controller.getIsManualInstallation();

        } catch (IOException ignored) {
        }
        return true;  // We want to make it manual installation by default
    }

    /**
     * Helper method that shows the view that handles the downloading of FFmpeg.
     *
     * @return The absolute path to the FFmpeg binary. Returns <code>null</code> if something went
     * wrong.
     */
    private String showDownloadingFFmpegView() {
        try {
            // Load the FXML file into the scene
            FXMLLoader fxmlLoader = new FXMLLoader(getSetupWizardView("downloading-ffmpeg-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Get the view controller
            DownloadingFFmpegViewController controller = fxmlLoader.getController();

            // Set the theme on the scene
            controller.setThemeOnScene(theme);

            // Set the stage's scene
            stage.setScene(scene);

            controller.startDownload();

            // Show the stage
            stage.showAndWait();

            // Return the value of the FFmpeg path
            return controller.getFFmpegPath();

        } catch (IOException ignored) {
        }
        return null;
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
        return IOMethods.getFileURL("views/fxml/setup-wizard/" + viewFile);
    }
}
