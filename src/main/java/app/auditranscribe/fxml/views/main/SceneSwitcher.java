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

package app.auditranscribe.fxml.views.main;

import app.auditranscribe.audio.Audio;
import app.auditranscribe.audio.FFmpegHandler;
import app.auditranscribe.fxml.Popups;
import app.auditranscribe.fxml.views.main.controllers.HomepageViewController;
import app.auditranscribe.fxml.views.main.controllers.TranscriptionViewController;
import app.auditranscribe.generic.LoggableClass;
import app.auditranscribe.generic.tuples.Pair;
import app.auditranscribe.io.IOConstants;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.audt_file.AUDTFileConstants;
import app.auditranscribe.io.audt_file.ProjectData;
import app.auditranscribe.io.audt_file.base.AUDTFileReader;
import app.auditranscribe.io.audt_file.base.data_encapsulators.*;
import app.auditranscribe.io.data_files.DataFiles;
import app.auditranscribe.io.exceptions.FailedToReadDataException;
import app.auditranscribe.io.exceptions.IncorrectFileFormatException;
import app.auditranscribe.io.exceptions.InvalidFileVersionException;
import app.auditranscribe.music.MusicKey;
import app.auditranscribe.music.TimeSignature;
import app.auditranscribe.system.OSMethods;
import app.auditranscribe.system.OSType;
import app.auditranscribe.utils.MiscUtils;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.commons.compress.utils.FileNameUtils;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Class that handles the switching between the main scene and the transcription scene.
 */
public class SceneSwitcher extends LoggableClass {
    // Attributes
    private final boolean debugMode;
    private final String currentVersion;

    private final Stage mainStage = new Stage();
    private final Stage transcriptionStage = new Stage();

    private final Rectangle2D screenBounds;

    private Pair<State, Data> returnedPair = null;

    private State state = State.SHOW_MAIN_SCENE;
    private Data data = new Data();

    /**
     * Initialization method for a <code>SceneSwitcher</code> object.
     *
     * @param currentVersion Current version of AudiTranscribe.
     */
    public SceneSwitcher(String currentVersion) {
        // Update attributes
        this.currentVersion = currentVersion;
        this.screenBounds = Screen.getPrimary().getVisualBounds();

        // Check if debug mode is enabled
        debugMode = IOMethods.isSomethingAt(
                IOMethods.joinPaths(IOConstants.APP_DATA_FOLDER_PATH, "debug-mode.txt")
        );

        // Set icon for the main stage and transcription stage, if not on macOS
        if (OSMethods.getOS() != OSType.MAC) {
            Image icon = new Image(IOMethods.readAsInputStream("images/logo-and-banner/icon.png"));
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
                    case SHOW_MAIN_SCENE -> returnedPair = showHomepageScene();
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
                    if (state == State.NEW_PROJECT || state == State.OPEN_PROJECT) {
                        state = State.SHOW_MAIN_SCENE;
                    } else {  // State has to be `SHOW_MAIN_SCENE` now because `CLOSE_SCENE` immediately exits
                        // The next state will be `CLOSE_SCENE`
                        state = State.CLOSE_SCENE;
                    }

                    // Regardless of the state, the data will be `null`
                    data = null;
                } else {
                    // Otherwise get the state and the data
                    state = returnedPair.value0();
                    data = returnedPair.value1();
                }
            } catch (Exception e) {  // Catch any alert that was not handled correctly
                logException(e);
                Popups.showExceptionAlert(
                        null,
                        "An Exception Occurred",
                        "An exception occurred during the execution of the program.",
                        e
                );
            }
        }

        log(Level.INFO, "Shutdown ordered");
        System.exit(0);  // Forces JVM to shut down
    }

    // Private methods

    /**
     * Helper method that shows the homepage on the screen.
     *
     * @return Pair of values. First value is the scene switching state, and the second is the scene
     * switching data. Returns <code>null</code> if an unrecoverable exception occurs.
     */
    private Pair<State, Data> showHomepageScene() {
        try {
            // Load the FXML file into the scene
            FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL(
                    "fxml/views/main/homepage-view.fxml"
            ));
            Scene scene = new Scene(fxmlLoader.load());

            // Get the view controller
            HomepageViewController controller = fxmlLoader.getController();

            // Setup main scene
            controller.setThemeOnScene();
            controller.setVersionLabel(currentVersion);

            // Set main stage properties
            mainStage.setTitle("Welcome to AudiTranscribe");
            mainStage.setScene(scene);
            mainStage.setResizable(false);

            mainStage.centerOnScreen();

            // Show the main stage
            mainStage.showAndWait();

            // Remove view from active
            controller.removeControllerFromActive();

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
    private Pair<State, Data> newProjectInTranscriptionScene() {
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

            // Get the paths for the auxiliary file
            String wavFilePath = IOMethods.joinPaths(
                    IOConstants.TEMP_FOLDER_PATH,
                    audioFile.getName().replace(fileExt, ".wav")
            );

            // Set up FFmpeg handler
            // (Failure to do so will throw exceptions)
            FFmpegHandler.initFFmpegHandler(DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath);

            // Convert original audio file into a WAV file for processing
            Audio audio = new Audio(
                    new File(FFmpegHandler.convertAudio(audioFile, wavFilePath)),
                    Audio.ProcessingMode.WITH_SAMPLES, Audio.ProcessingMode.WITH_PLAYBACK
            );

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

            transcriptionStage.setMinWidth(screenBounds.getWidth());
            transcriptionStage.setMinHeight(screenBounds.getHeight());

            transcriptionStage.setX(0);
            transcriptionStage.setY(0);

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
            logException(e);
            Popups.showExceptionAlert(
                    null,
                    "Error loading audio data",
                    "An error occurred when loading the audio data. Does the audio file " +
                            "still exist at the original location? Is the audio format supported?",
                    e
            );
        } catch (FFmpegHandler.BinaryNotFoundException e) {
            logException(e);
            Popups.showExceptionAlert(
                    null,
                    "Error finding FFmpeg",
                    "FFmpeg was not found. Please install it and try again.",
                    e
            );
        } catch (Audio.TooLongException e) {
            logException(e);
            Popups.showExceptionAlert(
                    null,
                    "Audio too long",
                    "The audio file is too long. Please select a shorter audio file.",
                    e
            );
        }

        // Return `null` if something went wrong
        return null;
    }

    /**
     * Helper method that handles the opening of an existing project in the transcription scene.
     *
     * @return Pair of values. First value is the scene switching state, and the second is the scene
     * switching data. Returns <code>null</code> if an unrecoverable exception occurs.
     */
    private Pair<State, Data> openProjectInTranscriptionScene() {
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
                        noExtension + "-" + MiscUtils.intAsPaddedHexStr(fileVersion) + ".audt"
                );
                boolean success = IOMethods.copyFile(audtFile.getAbsolutePath(), backupPath);

                if (!success) {
                    Popups.showInformationAlert(
                            null, "Failed to make backup of '" + audtFileName + "'.",
                            "The program failed to make a backup of '" + audtFile.getName() + "'."
                    );
                    log(Level.WARNING, "Failed to make backup of '" + audtFileName + "' to '" + backupPath + "'");
                } else {
                    log("Made backup of '" + audtFileName + "' to '" + backupPath + "'");
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
            controller.fileVersion = fileVersion;

            // Set the project data for the existing project
            controller.useExistingData(audtFilePath, audtFileName, projectData);
            controller.finishSetup();

            // Set the scene for the transcription page
            transcriptionStage.setScene(scene);

            // Set new scene properties
            transcriptionStage.setMaximized(true);
            transcriptionStage.setResizable(true);
            transcriptionStage.setTitle(projectData.projectInfoData.projectName);

            transcriptionStage.setMinWidth(screenBounds.getWidth());
            transcriptionStage.setMinHeight(screenBounds.getHeight());

            transcriptionStage.setX(0);
            transcriptionStage.setY(0);

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
                    null,
                    "Failed to find '" + audtFile.getName() + "'",
                    "The program failed to find'" + audtFile.getName() +
                            "' at its designated location. Please check if it is still there.",
                    e
            );
            logException(e);
        } catch (InvalidFileVersionException e) {
            Popups.showExceptionAlert(
                    null,
                    "Invalid file version in '" + audtFile.getName() + "'",
                    "The AUDT file '" + audtFile.getName() + "' has an invalid file version. Please " +
                            "check the version the file was saved in.",
                    e
            );
            logException(e);
        } catch (IOException | IncorrectFileFormatException | FailedToReadDataException e) {
            Popups.showExceptionAlert(
                    null,
                    "Failed to read '" + audtFile.getName() + "' as an AUDT ile",
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
                "fxml/views/main/transcription-view.fxml"
        ));

        // Get the spectrogram view scene
        Scene scene = new Scene(fxmlLoader.load());

        // Get the spectrogram view controller
        TranscriptionViewController controller = fxmlLoader.getController();

        // Set debug mode attribute
        controller.setDebugMode(debugMode);

        // Return the scene and controller
        return new Pair<>(scene, controller);
    }

    // Helper classes

    /**
     * Data that is used for the scene switcher.
     */
    public static class Data {
        // Attributes
        public String projectName;
        public File file;  // Can either be an audio file or an AUDT file

        public boolean isProjectSetup = false;  // False by default

        public boolean estimateMusicKey;
        public MusicKey musicKey;

        public boolean estimateBPM;
        public double manualBPM;

        public TimeSignature timeSignature;
        public double offset;

        // Public methods
        @Override
        public String toString() {
            return "SceneSwitcher.Data{" +
                    "projectName='" + projectName + "'" +
                    ", file=" + file +
                    ", isProjectSetup=" + isProjectSetup +
                    ", estimateMusicKey=" + estimateMusicKey +
                    ", musicKey=" + musicKey +
                    ", estimateBPM=" + estimateBPM +
                    ", manualBPM=" + manualBPM +
                    ", timeSignature=" + timeSignature +
                    ", offset=" + offset +
                    '}';
        }
    }

    public enum State {NEW_PROJECT, OPEN_PROJECT, SHOW_MAIN_SCENE, CLOSE_SCENE}
}
