/*
 * DownloadingAudioResourceViewController.java
 * Description: View controller that handles the downloading of the audio resource to fix the note
 *              delay.
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

package site.overwrite.auditranscribe.setup_wizard.view_controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.misc.CustomTask;
import site.overwrite.auditranscribe.misc.MyLogger;
import site.overwrite.auditranscribe.misc.Theme;
import site.overwrite.auditranscribe.setup_wizard.helpers.AudioResourceDownloadManager;
import site.overwrite.auditranscribe.setup_wizard.helpers.FFmpegDownloadManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * View controller that handles the downloading of the audio resource to fix the note delay.
 */
public class DownloadingAudioResourceViewController implements Initializable {
    // Constants
    private final String DEST_FOLDER = IOMethods.joinPaths(IOConstants.OTHER_RESOURCES_DATA_FOLDER_PATH, "audio");

    // Attributes
    AudioResourceDownloadManager downloadManager;
    String audioResourcePath;

    // FXML Elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private ProgressBar downloadProgressBar;

    // Initialization method
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        downloadManager = new AudioResourceDownloadManager(3);
    }

    // Getter methods

    public String getAudioResourcePath() {
        return audioResourcePath;
    }

    // Public methods

    /**
     * Method that starts the download of the audio resource.
     */
    public void startDownload() {
        // Define the task that handles the downloading of the audio resource
        CustomTask<String> downloadTask = new CustomTask<>() {
            @Override
            protected String call() throws Exception {
                return downloadManager.downloadAudioResource(DEST_FOLDER, this);
            }
        };
        downloadTask.setOnFailed((event) -> {
            audioResourcePath = null;
            ((Stage) rootPane.getScene().getWindow()).close();
        });
        downloadTask.setOnSucceeded((event) -> {
            audioResourcePath = downloadTask.getValue();
            MyLogger.log(
                    Level.INFO,
                    "Audio resource downloaded to '" + audioResourcePath + "'.",
                    this.getClass().getName()
            );
            ((Stage) rootPane.getScene().getWindow()).close();
        });

        // Set progress bar progress property
        downloadProgressBar.progressProperty().bind(downloadTask.progressProperty());

        // Define a thread to start the download on
        Thread downloadThread = new Thread(downloadTask);
        downloadThread.setDaemon(true);
        downloadThread.start();

        MyLogger.log(Level.INFO, "Showing audio resource download view", this.getClass().getName());
    }

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
