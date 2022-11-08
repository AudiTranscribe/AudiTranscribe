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

package site.overwrite.auditranscribe.views.setup_wizard.view_controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import site.overwrite.auditranscribe.generic.ClassWithLogging;
import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.misc.Theme;
import site.overwrite.auditranscribe.network.DownloadTask;
import site.overwrite.auditranscribe.utils.MathUtils;
import site.overwrite.auditranscribe.views.setup_wizard.download_handlers.AudioResourceDownloadManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * View controller that handles the downloading of the audio resource to fix the note delay.
 */
public class DownloadingAudioResourceViewController extends ClassWithLogging implements Initializable {
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

    @FXML
    private Label currDownloadAmountLabel, fileSizeLabel;

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
        DownloadTask<String> downloadTask = new DownloadTask<>() {
            @Override
            protected String call() throws Exception {
                return downloadManager.downloadResource(DEST_FOLDER, this);
            }
        };
        downloadTask.setOnFailed((event) -> {
            audioResourcePath = null;
            ((Stage) rootPane.getScene().getWindow()).close();
        });
        downloadTask.setOnSucceeded((event) -> {
            audioResourcePath = downloadTask.getValue();
            log(Level.INFO, "Audio resource downloaded to '" + audioResourcePath + "'.");
            ((Stage) rootPane.getScene().getWindow()).close();
        });

        // Set progress bar progress property
        downloadProgressBar.progressProperty().bind(downloadTask.progressProperty());

        // Make the labels update once the downloaded amount changes
        downloadTask.downloadedAmountProperty().addListener((obs, oldVal, newVal) -> {
            // Convert the number of bytes to kilobytes
            double downloadedAmtInKB = MathUtils.round(newVal.doubleValue() / 1e3, 2);
            double fileSizeInKB = MathUtils.round(downloadTask.getDownloadFileSize() / 1e3, 2);

            // Set text to display
            currDownloadAmountLabel.setText(String.valueOf(downloadedAmtInKB));
            fileSizeLabel.setText(String.valueOf(fileSizeInKB));
        });

        // Define a thread to start the download on
        Thread downloadThread = new Thread(downloadTask);
        downloadThread.setDaemon(true);
        downloadThread.start();

        log(Level.INFO, "Showing audio resource download view");
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
