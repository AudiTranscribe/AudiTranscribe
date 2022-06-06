/*
 * FFmpegInstallationPopupsHandler.java
 *
 * Created on 2022-06-04
 * Updated on 2022-06-06
 *
 * Description: View controller for the FFmpeg installation window.
 */

package site.overwrite.auditranscribe.views.helpers;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import net.bramp.ffmpeg.FFmpeg;
import site.overwrite.auditranscribe.audio.ffmpeg.FFmpegNotFound;
import site.overwrite.auditranscribe.io.StreamGobbler;
import site.overwrite.auditranscribe.io.json_files.file_classes.SettingsFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executors;

public class FFmpegInstallationPopupsHandler {
    // Public methods

    /**
     * Method that shows the FFmpeg installation window.
     *
     * @param settingsFile  The settings file.
     */
    public static void showFFmpegInstallationView(SettingsFile settingsFile) {
        // If FFmpeg has a specified path skip this
        if (settingsFile.data.ffmpegInstallationPath != null) return;

        // Ask whether FFmpeg has been installed or not
        boolean userSayFFmpegInstalled = false;
        ButtonType ffmpegInstalled = new ButtonType("Yes");
        ButtonType notInstalled = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

        Optional<ButtonType> selectedChoice = Popups.showMultiButtonAlert(
                "FFmpeg Installation",
                "Do you have FFmpeg installed?",
                "We need FFmpeg for audio processing and audio conversion.",
                ffmpegInstalled, notInstalled
        );

        if (selectedChoice.isPresent()) {
            if (selectedChoice.get() == ffmpegInstalled) {
                userSayFFmpegInstalled = true;
            }
        }

        // Repeatedly attempt to affirm that FFmpeg is installed
        boolean isFFmpegInstalled = false;
        boolean usingCustomInstallation = false;
        boolean canFindFFmpeg = true;
        String ffmpegPath = null;

        while (!isFFmpegInstalled) {
            // Give FFmpeg installation instructions if the user says that they have not installed it
            // Todo: add custom installation instructions view as this is quite ugly
            if (!userSayFFmpegInstalled && !usingCustomInstallation) {
                Popups.showMultiButtonAlert(
                        "FFmpeg Installation",
                        "Instructions to Install FFmpeg",
                        "Please had to https://ffmpeg.org/download.html and install FFmpeg. " +
                                "It is recommended to install FFmpeg version 5.0.x or higher.",
                        new ButtonType("I've installed FFmpeg.", ButtonBar.ButtonData.CANCEL_CLOSE)
                );
            }

            // Check the FFmpeg installation
            try {
                // Immediately throw an exception if can't find FFmpeg in previous loop
                if (!canFindFFmpeg) {
                    throw new FFmpegNotFound("FFmpeg not found at manually specified path.");
                }

                // Attempt to get the path to the FFmpeg binary and save to persistent data file
                ffmpegPath = getPathToFFmpeg();

                // Update the `isFFmpegInstalled` variable
                isFFmpegInstalled = true;

            } catch (FFmpegNotFound e) {
                // Show error that the FFmpeg binary was not found
                ButtonType specifyManually = new ButtonType("Specify Manually");
                ButtonType readInstructionsAgain = new ButtonType(
                        "Read Instructions Again",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

                selectedChoice = Popups.showMultiButtonAlert(
                        "FFmpeg Installation",
                        "FFmpeg Not Found",
                        "FFmpeg could not be located automatically. " +
                                "Would you like to specify its path manually?",
                        specifyManually, readInstructionsAgain
                );

                if (selectedChoice.isPresent()) {
                    if (selectedChoice.get() == specifyManually) {
                        // Update the `usingCustomInstallation` variable
                        usingCustomInstallation = true;

                        // Ask user to specify the path to FFmpeg
                        Optional<String> customPath = Popups.showTextInputDialog(
                                "FFmpeg Installation",
                                "Specify Path To FFmpeg",
                                "Path to FFmpeg:",
                                null
                        );

                        if (customPath.isPresent()) {
                            // Update the `ffmpegPath` variable
                            ffmpegPath = customPath.get();
                        } else {
                            // Show error that the user did not specify a path
                            Popups.showInformationAlert(
                                    "FFmpeg Installation",
                                    "No path specified. Please specify a path to FFmpeg."
                            );

                            // Restart loop
                            continue;
                        }

                    } else {
                        // Update variables
                        userSayFFmpegInstalled = false;
                        usingCustomInstallation = false;

                        // Restart loop
                        continue;
                    }

                } else {
                    // Update variables
                    userSayFFmpegInstalled = false;
                    usingCustomInstallation = false;

                    // Restart loop
                    continue;
                }

                // Check the custom FFmpeg path for the FFmpeg binary
                try {
                    new FFmpeg(ffmpegPath);
                    isFFmpegInstalled = true;
                } catch (IOException e1) {
                    // Note that FFmpeg cannot be found
                    canFindFFmpeg = false;
                }
            }
        }

        // Sve to settings file
        settingsFile.data.ffmpegInstallationPath = ffmpegPath;
        settingsFile.saveFile();

        // Confirm that FFmpeg has been detected
        Popups.showInformationAlert(
                "FFmpeg Installation Completed",
                "FFmpeg has been detected. You can now use AudiTranscribe."
        );
    }

    // Private methods

    /**
     * Method that attempts to find the ffmpeg installation path.
     *
     * @return A string, representing the ffmpeg installation path.
     * @throws FFmpegNotFound If the program fails to find the ffmpeg installation.
     */
    private static String getPathToFFmpeg() throws FFmpegNotFound {
        // Check the operating system
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        // Generate the command to execute
        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command("cmd.exe", "/c", "where ffmpeg");  // Todo: check if this works on windows
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
            if (exitCode != 0) throw new FFmpegNotFound("ffmpeg binary cannot be located.\n" + ffmpegPath[0]);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Return the ffmpeg path
        return ffmpegPath[0];
    }
}
