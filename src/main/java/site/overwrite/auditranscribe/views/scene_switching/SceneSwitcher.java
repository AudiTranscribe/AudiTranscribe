/*
 * SceneSwitcher.java
 *
 * Created on 2022-06-22
 * Updated on 2022-06-23
 *
 * Description: Class that handles the switching between the main scene and transcription scenes.
 */

package site.overwrite.auditranscribe.views.scene_switching;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.commons.compress.utils.FileNameUtils;
import org.javatuples.Pair;
import site.overwrite.auditranscribe.audio.Audio;
import site.overwrite.auditranscribe.audio.AudioProcessingMode;
import site.overwrite.auditranscribe.audio.FFmpegHandler;
import site.overwrite.auditranscribe.exceptions.audio.AudioTooLongException;
import site.overwrite.auditranscribe.exceptions.audio.FFmpegNotFoundException;
import site.overwrite.auditranscribe.exceptions.io.audt_file.FailedToReadDataException;
import site.overwrite.auditranscribe.exceptions.io.audt_file.IncorrectFileFormatException;
import site.overwrite.auditranscribe.exceptions.io.audt_file.OutdatedFileFormatException;
import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.audt_file.AUDTFileReader;
import site.overwrite.auditranscribe.io.audt_file.ProjectData;
import site.overwrite.auditranscribe.io.audt_file.data_encapsulators.*;
import site.overwrite.auditranscribe.io.json_files.file_classes.SettingsFile;
import site.overwrite.auditranscribe.views.helpers.Popups;
import site.overwrite.auditranscribe.views.main_views.MainViewController;
import site.overwrite.auditranscribe.views.main_views.TranscriptionViewController;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that handles the switching between the main scene and transcription scenes.
 */
public class SceneSwitcher {
    // Attributes
    private final SettingsFile settingsFile;

    private final Stage mainStage = new Stage();
    private final Stage transcriptionStage = new Stage();

    private Pair<SceneSwitchingState, File> returnedPair = null;
    private SceneSwitchingState state = SceneSwitchingState.SHOW_MAIN_SCENE;
    private File selectedFile = null;

    private static final Logger logger = Logger.getLogger(SceneSwitcher.class.getName());

    /**
     * Initialization method for a <code>SceneSwitcher</code> object.
     *
     * @param settingsFile Settings file to use.
     */
    // Todo: somehow use the main scene
    public SceneSwitcher(SettingsFile settingsFile) {
        this.settingsFile = settingsFile;
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
            // Handle the different cases of the returned state
            switch (state) {
                case NEW_PROJECT -> returnedPair = newProjectInTranscriptionScene(selectedFile);
                case OPEN_PROJECT -> returnedPair = openProjectInTranscriptionScene(selectedFile);
                case SHOW_MAIN_SCENE -> returnedPair = showMainScene();
                case CLOSE_SCENE -> {
                    // Since close scene was called, shutdown scene handler
                    shutdown = true;
                    continue;
                }
            }

            // Check if the returned pair is null
            if (returnedPair == null) {
                // If the returned pair is `null`, that means something went wrong
                // If we are currently in the transcription scene, then the next state is `SHOW_MAIN_SCENE`
                if (state == SceneSwitchingState.NEW_PROJECT || state == SceneSwitchingState.OPEN_PROJECT) {
                    state = SceneSwitchingState.SHOW_MAIN_SCENE;
                } else {  // State has to be `SHOW_MAIN_SCENE` now because `CLOSE_SCENE` immediately exits
                    // The next state will be `CLOSE_SCENE`
                    state = SceneSwitchingState.CLOSE_SCENE;
                }

                // Regardless of the state, the newly selected file will be `null`
                selectedFile = null;
            } else {
                // Otherwise get the state and the selected file
                state = returnedPair.getValue0();
                selectedFile = returnedPair.getValue1();
            }
        }
    }

    // Private methods

    /**
     * Helper method that shows the main scene on the screen.
     *
     * @return Pair of values. First value is the scene switching state, and the second is the
     * selected file.
     */
    private Pair<SceneSwitchingState, File> showMainScene() {
        try {
            // Load the FXML file into the scene
            FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL(
                    "views/fxml/main/main-view.fxml"
            ));
            Scene scene = new Scene(fxmlLoader.load());

            // Get the view controller
            MainViewController controller = fxmlLoader.getController();

            // Set the settings file on the main scene
            controller.setSettingsFile(settingsFile);

            // Set the theme of the scene
            controller.setThemeOnScene();

            // Set main stage properties
            mainStage.setTitle("Welcome to AudiTranscribe");
            mainStage.setScene(scene);
            mainStage.setResizable(false);

            // Show the main stage
            mainStage.showAndWait();

            // Obtain the scene switching state and the selected file and return
            return new Pair<>(
                    controller.getSceneSwitchingState(),
                    controller.getSelectedFile()
            );
        } catch (IOException ignored) {
        }

        return null;
    }

    /**
     * Helper method that handles the creation of a new project in the transcription scene.
     *
     * @param audioFile Audio file to create a new project of.<br>
     *                  By this point, we should have verified that <code>audioFile</code> is not
     *                  <code>null</code>.
     */
    private Pair<SceneSwitchingState, File> newProjectInTranscriptionScene(File audioFile) {
        try {
            // Get the extension of the provided audio file
            String fileExt = "." + FileNameUtils.getExtension(audioFile.getName()).toLowerCase();

            // Check if the file is supported
            if (!FFmpegHandler.VALID_EXTENSIONS.contains(fileExt)) {
                throw new UnsupportedAudioFileException("The audio file is not supported.");
            }

            // Attempt creation of temporary folder if it doesn't exist
            IOMethods.createFolder(IOConstants.TEMP_FOLDER);
            logger.log(Level.FINE, "Temporary folder: " + IOConstants.TEMP_FOLDER);

            // Get the base path for the auxiliary files
            String baseName = IOConstants.TEMP_FOLDER + audioFile.getName().replace(fileExt, "");

            // Generate a new WAV file
            FFmpegHandler FFmpegHandler = new FFmpegHandler(settingsFile.data.ffmpegInstallationPath);
            File auxiliaryWAVFile = new File(
                    FFmpegHandler.convertAudio(audioFile, baseName + "-auxiliary-wav.wav")
            );

            // Try and read the auxiliary WAV file as an `Audio` object
            // (Failure to read will throw exceptions)
            Audio audio = new Audio(auxiliaryWAVFile, audioFile.getName(), AudioProcessingMode.SAMPLES_AND_PLAYBACK);

            // Delete auxiliary WAV file
            boolean successfullyDeleted = auxiliaryWAVFile.delete();
            if (successfullyDeleted) {
                logger.log(Level.FINE, "Successfully deleted auxiliary WAV file.");
            } else {
                logger.log(Level.WARNING, "Failed to delete auxiliary WAV file.");
            }

            // Get the current scene and the spectrogram view controller
            Pair<Scene, TranscriptionViewController> stageSceneAndController = setupTranscriptionScene();
            Scene scene = stageSceneAndController.getValue0();
            TranscriptionViewController controller = stageSceneAndController.getValue1();

            // Update the `settingsFile` attribute
            controller.setSettingsFile(settingsFile);

            // Set the theme of the scene
            controller.setThemeOnScene();

            // Set the project data for the existing project
            controller.setAudioAndSpectrogramData(audio);
            controller.finishSetup();

            // Set the scene for the transcription page
            transcriptionStage.setScene(scene);

            // Set new scene properties
            transcriptionStage.setMaximized(true);
            transcriptionStage.setResizable(true);
            transcriptionStage.setTitle(audioFile.getName());

            // Set width and height of the new scene
            Rectangle2D screenBounds = Screen.getPrimary().getBounds();
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
                    controller.getSelectedFile()
            );

        } catch (IOException | UnsupportedAudioFileException e) {
            Popups.showExceptionAlert(
                    "Error loading audio data.",
                    "An error occurred when loading the audio data. Does the audio file " +
                            "still exist at the original location? Is the audio format supported?",
                    e
            );
            e.printStackTrace();
        } catch (FFmpegNotFoundException e) {
            Popups.showExceptionAlert(
                    "Error finding FFmpeg.",
                    "FFmpeg was not found. Please install it and try again.",
                    e
            );
            e.printStackTrace();
        } catch (AudioTooLongException e) {
            Popups.showExceptionAlert(
                    "Audio too long.",
                    "The audio file is too long. Please select a shorter audio file.",
                    e
            );
            e.printStackTrace();
        }

        // If an exception occurred, return `null`
        return null;
    }

    /**
     * Helper method that handles the opening of an existing project in the transcription scene.
     *
     * @param audtFile AudiTranscribe file to open.<br>
     *                 By this point, we should have verified that <code>audtFile</code> is not
     *                 <code>null</code>.
     */
    private Pair<SceneSwitchingState, File> openProjectInTranscriptionScene(File audtFile) {
        try {
            // Try and read the file as an AUDT file
            String audtFilePath = audtFile.getAbsolutePath();
            String audtFileName = audtFile.getName();
            AUDTFileReader reader = new AUDTFileReader(audtFilePath);

            // Read the data from the file
            UnchangingDataPropertiesObject unchangingDataProperties = reader.readUnchangingDataProperties();
            QTransformDataObject qTransformData = reader.readQTransformData();
            AudioDataObject audioData = reader.readAudioData();
            GUIDataObject guiData = reader.readGUIData();
            MusicNotesDataObject musicNotesData = reader.readMusicNotesData();

            // Pass these data into a `ProjectData`
            ProjectData projectData = new ProjectData(
                    unchangingDataProperties, qTransformData, audioData, guiData, musicNotesData
            );

            // Get the current scene and the spectrogram view controller
            Pair<Scene, TranscriptionViewController> stageSceneAndController = setupTranscriptionScene();
            Scene scene = stageSceneAndController.getValue0();
            TranscriptionViewController controller = stageSceneAndController.getValue1();

            // Update the `settingsFile` attribute
            controller.setSettingsFile(settingsFile);

            // Set the theme of the scene
            controller.setThemeOnScene();

            // Set the project data for the existing project
            controller.useExistingData(audtFilePath, audtFileName, projectData);
            controller.finishSetup();

            // Set the scene for the transcription page
            transcriptionStage.setScene(scene);

            // Set new scene properties
            transcriptionStage.setMaximized(true);
            transcriptionStage.setResizable(true);
            transcriptionStage.setTitle(audioData.audioFileName);

            // Set width and height of the new scene
            Rectangle2D screenBounds = Screen.getPrimary().getBounds();
            transcriptionStage.setMinWidth(screenBounds.getWidth());
            transcriptionStage.setMinHeight(screenBounds.getHeight());

            // Update scroll position
            controller.updateScrollPosition(
                    projectData.guiData.currTimeInMS / 1000. *
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
                    controller.getSelectedFile()
            );

        } catch (FileNotFoundException e) {
            Popups.showExceptionAlert(
                    "Failed to find '" + audtFile.getName() + "'.",
                    "The program failed to find'" + audtFile.getName() +
                            "' at its designated location. Please check if it is still there.",
                    e
            );
            e.printStackTrace();
        } catch (OutdatedFileFormatException e) {
            Popups.showExceptionAlert(
                    "File version mismatch in '" + audtFile.getName() + "'.",
                    "The AUDT file '" + audtFile.getName() + "' is outdated, or is not current. Please " +
                            "check the version the file was saved in.",
                    e
            );
            e.printStackTrace();
        } catch (IOException | IncorrectFileFormatException | FailedToReadDataException e) {
            Popups.showExceptionAlert(
                    "Failed to read '" + audtFile.getName() + "' as an AUDT ile.",
                    "The program failed to read '" + audtFile.getName() +
                            "' as an AUDT file. Is the file format correct?",
                    e
            );
            e.printStackTrace();
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
