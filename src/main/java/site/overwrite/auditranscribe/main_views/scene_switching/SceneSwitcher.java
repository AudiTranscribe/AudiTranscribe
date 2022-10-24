/*
 * SceneSwitcher.java
 * Description: Class that handles the switching between the main scene and the transcription scene.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public Licence as published by the Free Software Foundation, either version 3 of the
 * Licence, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public Licence for more details.
 *
 * You should have received a copy of the GNU General Public Licence along with this program. If
 * not, see <https://www.gnu.org/licenses/>
 *
 * Copyright © AudiTranscribe Team
 */

package site.overwrite.auditranscribe.main_views.scene_switching;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.commons.compress.utils.FileNameUtils;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.AudioProcessingMode;
import site.overwrite.auditranscribe.audio.FFmpegHandler;
import site.overwrite.auditranscribe.audio.exceptions.AudioTooLongException;
import site.overwrite.auditranscribe.audio.exceptions.FFmpegNotFoundException;
import site.overwrite.auditranscribe.generic.ClassWithLogging;
import site.overwrite.auditranscribe.generic.tuples.Pair;
import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.audt_file.AUDTFileConstants;
import site.overwrite.auditranscribe.io.audt_file.ProjectData;
import site.overwrite.auditranscribe.io.audt_file.base.AUDTFileReader;
import site.overwrite.auditranscribe.io.audt_file.base.data_encapsulators.*;
import site.overwrite.auditranscribe.io.data_files.DataFiles;
import site.overwrite.auditranscribe.io.exceptions.FailedToReadDataException;
import site.overwrite.auditranscribe.io.exceptions.IncorrectFileFormatException;
import site.overwrite.auditranscribe.io.exceptions.InvalidFileVersionException;
import site.overwrite.auditranscribe.main_views.MainViewController;
import site.overwrite.auditranscribe.main_views.TranscriptionViewController;
import site.overwrite.auditranscribe.misc.Popups;
import site.overwrite.auditranscribe.system.OSMethods;
import site.overwrite.auditranscribe.system.OSType;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Class that handles the switching between the main scene and the transcription scene.
 */
public class SceneSwitcher extends ClassWithLogging {
    // Attributes
    private final String currentVersion;

    private final Stage mainStage = new Stage();
    private final Stage transcriptionStage = new Stage();

    private Pair<SceneSwitchingState, SceneSwitchingData> returnedPair = null;

    private SceneSwitchingState state = SceneSwitchingState.SHOW_MAIN_SCENE;
    private SceneSwitchingData data = new SceneSwitchingData();

    /**
     * Initialization method for a <code>SceneSwitcher</code> object.
     *
     * @param currentVersion Current version of AudiTranscribe.
     */
    public SceneSwitcher(String currentVersion) {
        // Update attributes
        this.currentVersion = currentVersion;

        // Set icon for the main stage and transcription stage, if not on macOS
        if (OSMethods.getOS() != OSType.MAC) {
            Image icon = new Image(IOMethods.getInputStream("images/logo-and-banner/icon.png"));
            this.mainStage.getIcons().add(icon);
            this.transcriptionStage.getIcons().add(icon);
        }
    }

    // Public methods

    /**
     * Starts the scene switcher handler.<br>
     * The first thing this will do is show the <b>main scene</b>.
     */
    public void startHandler() {
        // Continue handler until shutdown
        boolean shutdown = false;

        while (!shutdown) {
            try {
                // Handle the different cases of the returned state
                switch (state) {
                    case NEW_PROJECT -> returnedPair = newProjectInTranscriptionScene();
                    case OPEN_PROJECT -> returnedPair = openProjectInTranscriptionScene();
                    case SHOW_MAIN_SCENE -> returnedPair = showMainScene();
                    case CLOSE_SCENE -> {
                        // Since close scene was called, shutdown scene handler
                        shutdown = true;
                        continue;
                    }
                }

                // Check if the returned pair is `null`
                if (returnedPair == null) {
                    // If the returned pair is `null`, that means something went wrong
                    // If we are currently in the transcription scene, then the next state is `SHOW_MAIN_SCENE`
                    if (state == SceneSwitchingState.NEW_PROJECT || state == SceneSwitchingState.OPEN_PROJECT) {
                        state = SceneSwitchingState.SHOW_MAIN_SCENE;
                    } else {  // State has to be `SHOW_MAIN_SCENE` now because `CLOSE_SCENE` immediately exits
                        // The next state will be `CLOSE_SCENE`
                        state = SceneSwitchingState.CLOSE_SCENE;
                    }

                    // Regardless of the state, the data will be `null`
                    data = null;
                } else {
                    // Otherwise get the state and the data
                    state = returnedPair.value0();
                    data = returnedPair.value1();
                }
            } catch (Exception e) {  // Catch any alert that was not handled correctly
                Popups.showExceptionAlert(
                        "An Exception Occurred",
                        "An exception occurred during the execution of the program.",
                        e
                );
                logException(e);
            }
        }

        log(Level.INFO, "Shutdown ordered");
        System.exit(0);  // Forces JVM to shut down
    }

    // Private methods

    /**
     * Helper method that shows the main scene on the screen.
     *
     * @return Pair of values. First value is the scene switching state, and the second is the scene
     * switching data. Returns <code>null</code> if an unrecoverable exception occurs.
     */
    private Pair<SceneSwitchingState, SceneSwitchingData> showMainScene() {
        try {
            // Load the FXML file into the scene
            FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL(
                    "views/fxml/main/main-view.fxml"
            ));
            Scene scene = new Scene(fxmlLoader.load());

            // Get the view controller
            MainViewController controller = fxmlLoader.getController();

            // Setup main scene
            controller.setThemeOnScene();
            controller.setVersionLabel(currentVersion);

            // Set main stage properties
            mainStage.setTitle("Welcome to AudiTranscribe");
            mainStage.setScene(scene);
            mainStage.setResizable(false);

            // Show the main stage
            mainStage.showAndWait();

            // Obtain the scene switching state and the selected file and return
            return new Pair<>(
                    controller.getSceneSwitchingState(),
                    controller.getSceneSwitchingData()
            );
        } catch (IOException e) {
            logException(e);
        }

        return null;
    }

    /**
     * Helper method that handles the creation of a new project in the transcription scene.
     *
     * @return Pair of values. First value is the scene switching state, and the second is the scene
     * switching data. Returns <code>null</code> if an unrecoverable exception occurs.
     */
    private Pair<SceneSwitchingState, SceneSwitchingData> newProjectInTranscriptionScene() {
        // Obtain the audio file from the scene switching data
        File audioFile = data.file;

        try {
            // Get the extension of the provided audio file
            String fileExt = "." + FileNameUtils.getExtension(audioFile.getName()).toLowerCase();

            // Check if the file is supported
            if (!FFmpegHandler.VALID_EXTENSIONS.contains(fileExt)) {
                throw new UnsupportedAudioFileException("The audio file is not supported.");
            }

            // Attempt creation of temporary folder if it doesn't exist
            IOMethods.createFolder(IOConstants.TEMP_FOLDER_PATH);
            log(Level.FINE, "Temporary folder: " + IOConstants.TEMP_FOLDER_PATH);

            // Get the paths for the auxiliary files
            String baseName = IOMethods.joinPaths(
                    IOConstants.TEMP_FOLDER_PATH,
                    audioFile.getName().replace(fileExt, "")
            );
            String samplesWAVPath = baseName + "-samples.wav";
            String slowedWAVPath = baseName + "-slowed.wav";

            // Set up FFmpeg handler
            // (Failure to do so will throw exceptions)
            FFmpegHandler FFmpegHandler = new FFmpegHandler(DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath);

            // Generate the WAV files
            File samplesWAVFile = new File(
                    FFmpegHandler.convertAudio(audioFile, samplesWAVPath)
            );
            File slowedWAVFile = new File(
                    FFmpegHandler.generateAltTempoAudio(audioFile, slowedWAVPath, 0.5)
            );

            // Try and read the WAV files as an `Audio` object
            // (Failure to read will throw exceptions)
            Audio audio = new Audio(
                    samplesWAVFile, slowedWAVFile,
                    AudioProcessingMode.WITH_SAMPLES, AudioProcessingMode.WITH_PLAYBACK,
                    AudioProcessingMode.WITH_SLOWDOWN
            );

            // Delete auxiliary WAV files
            boolean successfullyDeleted = IOMethods.delete(samplesWAVFile);
            successfullyDeleted = (successfullyDeleted && IOMethods.delete(slowedWAVFile));

            if (successfullyDeleted) {
                log(Level.FINE, "Successfully deleted auxiliary WAV files.");
            } else {
                log(Level.WARNING, "Failed to delete auxiliary WAV files now; will attempt delete after exit.");
            }

            // Get the current scene and the spectrogram view controller
            Pair<Scene, TranscriptionViewController> stageSceneAndController = setupTranscriptionScene();
            Scene scene = stageSceneAndController.value0();
            TranscriptionViewController controller = stageSceneAndController.value1();

            // Set the theme of the scene
            controller.setThemeOnScene();

            // Set the project data for the existing project
            controller.setAudioAndSpectrogramData(audio, data);
            controller.finishSetup();

            // Set the scene for the transcription page
            transcriptionStage.setScene(scene);

            // Set new scene properties
            transcriptionStage.setMaximized(true);
            transcriptionStage.setResizable(true);
            transcriptionStage.setTitle(data.projectName);

            // Set width and height of the new scene
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            transcriptionStage.setMinWidth(screenBounds.getWidth());
            transcriptionStage.setMinHeight(screenBounds.getHeight());

            // Hide main stage (if it is still showing)
            if (mainStage.isShowing()) mainStage.hide();

            // Show transcription stage
            transcriptionStage.showAndWait();

            // Handle transcription stage closing
            controller.handleSceneClosing();

            // Obtain the scene switching state and the selected file and return
            return new Pair<>(
                    controller.getSceneSwitchingState(),
                    controller.getSceneSwitchingData()
            );

        } catch (IOException | UnsupportedAudioFileException e) {
            Popups.showExceptionAlert(
                    "Error loading audio data.",
                    "An error occurred when loading the audio data. Does the audio file " +
                            "still exist at the original location? Is the audio format supported?",
                    e
            );
            logException(e);
        } catch (FFmpegNotFoundException e) {
            Popups.showExceptionAlert(
                    "Error finding FFmpeg.",
                    "FFmpeg was not found. Please install it and try again.",
                    e
            );
            logException(e);
        } catch (AudioTooLongException e) {
            Popups.showExceptionAlert(
                    "Audio too long.",
                    "The audio file is too long. Please select a shorter audio file.",
                    e
            );
            logException(e);
        }

        // If an exception occurred, return `null`
        return null;
    }

    /**
     * Helper method that handles the opening of an existing project in the transcription scene.
     *
     * @return Pair of values. First value is the scene switching state, and the second is the scene
     * switching data. Returns <code>null</code> if an unrecoverable exception occurs.
     */
    private Pair<SceneSwitchingState, SceneSwitchingData> openProjectInTranscriptionScene() {
        // Obtain the AUDT file from the scene switching data
        File audtFile = data.file;

        try {
            // Try and read the file as an AUDT file
            String audtFilePath = audtFile.getAbsolutePath();
            String audtFileName = audtFile.getName();
            AUDTFileReader reader = AUDTFileReader.getFileReader(audtFilePath);

            // Get the file version
            int fileVersion = reader.fileFormatVersion;

            // If file is not the latest version, make a backup
            if (fileVersion != AUDTFileConstants.FILE_VERSION_NUMBER) {
                // Get the filename without extension
                String noExtension = audtFileName;
                int pos = noExtension.lastIndexOf(".");
                if (pos > 0 && pos < (noExtension.length() - 1)) {
                    noExtension = noExtension.substring(0, pos);
                }

                // Save to backups folder
                String backupPath = IOMethods.joinPaths(
                        IOConstants.PROJECT_BACKUPS_FOLDER_PATH,
                        noExtension + "-" + Integer.toHexString(fileVersion) + ".audt"
                );
                boolean success = IOMethods.copyFile(audtFile.getAbsolutePath(), backupPath);

                if (!success) {
                    Popups.showInformationAlert(
                            "Failed to make backup of '" + audtFileName + "'.",
                            "The program failed to make a backup of '" + audtFile.getName() + "'."
                    );
                    log(Level.WARNING, "Failed to make backup of '" + audtFileName + "' to '" + backupPath + "'.");
                } else {
                    log(Level.INFO, "Made backup of '" + audtFileName + "' to '" + backupPath + "'.");
                }
            }

            // Read the data from the file
            UnchangingDataPropertiesObject unchangingDataProperties = reader.readUnchangingDataProperties();
            QTransformDataObject qTransformData = reader.readQTransformData();
            AudioDataObject audioData = reader.readAudioData();
            ProjectInfoDataObject guiData = reader.readProjectInfoData();
            MusicNotesDataObject musicNotesData = reader.readMusicNotesData();

            // Pass these data into a `ProjectData`
            ProjectData projectData = new ProjectData(
                    unchangingDataProperties, qTransformData, audioData, guiData, musicNotesData
            );

            // Get the current scene and the spectrogram view controller
            Pair<Scene, TranscriptionViewController> stageSceneAndController = setupTranscriptionScene();
            Scene scene = stageSceneAndController.value0();
            TranscriptionViewController controller = stageSceneAndController.value1();

            // Set the theme of the scene
            controller.setThemeOnScene();

            // Set the file version that is used
            controller.setFileVersion(fileVersion);

            // Set the project data for the existing project
            controller.useExistingData(audtFilePath, audtFileName, projectData);
            controller.finishSetup();

            // Set the scene for the transcription page
            transcriptionStage.setScene(scene);

            // Set new scene properties
            transcriptionStage.setMaximized(true);
            transcriptionStage.setResizable(true);
            transcriptionStage.setTitle(projectData.projectInfoData.projectName);

            // Set width and height of the new scene
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            transcriptionStage.setMinWidth(screenBounds.getWidth());
            transcriptionStage.setMinHeight(screenBounds.getHeight());

            // Update scroll position
            controller.updateScrollPosition(
                    projectData.projectInfoData.currTimeInMS / 1000. *
                            controller.PX_PER_SECOND *
                            controller.SPECTROGRAM_ZOOM_SCALE_X,
                    screenBounds.getWidth()
            );

            // Hide main stage (if it is still showing)
            if (mainStage.isShowing()) mainStage.hide();

            // Show transcription stage
            transcriptionStage.showAndWait();

            // Handle transcription stage closing
            controller.handleSceneClosing();

            // Obtain the scene switching state and the selected file and return
            return new Pair<>(
                    controller.getSceneSwitchingState(),
                    controller.getSceneSwitchingData()
            );

        } catch (FileNotFoundException e) {
            Popups.showExceptionAlert(
                    "Failed to find '" + audtFile.getName() + "'.",
                    "The program failed to find'" + audtFile.getName() +
                            "' at its designated location. Please check if it is still there.",
                    e
            );
            logException(e);
        } catch (InvalidFileVersionException e) {
            Popups.showExceptionAlert(
                    "Invalid file version in '" + audtFile.getName() + "'.",
                    "The AUDT file '" + audtFile.getName() + "' has an invalid file version. Please " +
                            "check the version the file was saved in.",
                    e
            );
            logException(e);
        } catch (IOException | IncorrectFileFormatException | FailedToReadDataException e) {
            Popups.showExceptionAlert(
                    "Failed to read '" + audtFile.getName() + "' as an AUDT ile.",
                    "The program failed to read '" + audtFile.getName() +
                            "' as an AUDT file. Is the file format correct?",
                    e
            );
            logException(e);
        }

        // If an exception occurred, return `null`
        return null;
    }

    /**
     * Helper method that gets the transcription scene and transcription view controller.
     *
     * @return Spectrogram scene object and the <code>TranscriptionViewController</code> object.
     * @throws IOException If the spectrogram view FXML file cannot be found.
     */
    private Pair<Scene, TranscriptionViewController> setupTranscriptionScene() throws IOException {
        // Unset full screen and maximized first
        transcriptionStage.setMaximized(false);
        transcriptionStage.setFullScreen(false);

        // Get the FXML loader for the spectrogram view
        FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL(
                "views/fxml/main/transcription-view.fxml"
        ));

        // Get the spectrogram view scene
        Scene scene = new Scene(fxmlLoader.load());

        // Get the spectrogram view controller
        TranscriptionViewController controller = fxmlLoader.getController();

        // Return the scene and controller
        return new Pair<>(scene, controller);
    }
}
