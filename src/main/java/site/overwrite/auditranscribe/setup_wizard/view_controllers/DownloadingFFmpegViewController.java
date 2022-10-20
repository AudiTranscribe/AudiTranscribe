/*
 * DownloadingFFmpegViewController.java
 * Description: View controller that handles the downloading of FFmpeg.
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
import site.overwrite.auditranscribe.generic.ClassWithLogging;
import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.misc.Theme;
import site.overwrite.auditranscribe.network.DownloadTask;
import site.overwrite.auditranscribe.setup_wizard.download_handlers.FFmpegDownloadManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * View controller that handles the downloading of FFmpeg.
 */
public class DownloadingFFmpegViewController extends ClassWithLogging implements Initializable {
    // Constants
    private final String DEST_FOLDER = IOConstants.APP_DATA_FOLDER_PATH;

    // Attributes
    FFmpegDownloadManager downloadManager;
    String ffmpegPath;

    // FXML Elements
    @FXML
    private AnchorPane rootPane;

    @FXML
    private ProgressBar downloadProgressBar;

    // Initialization method
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        downloadManager = new FFmpegDownloadManager(3);
    }

    // Getter methods

    /**
     * Method that returns the <b>absolute</b> path to the FFmpeg binary.
     *
     * @return <b>Absolute</b> path to the FFmpeg binary. Returns <code>null</code> if the
     * downloading fails.
     */
    public String getFFmpegPath() {
        return ffmpegPath;
    }

    // Public methods

    /**
     * Method that starts the download of the FFmpeg binary.
     */
    public void startDownload() {
        // Define the task that handles the downloading of the FFmpeg binary
        DownloadTask<String> downloadTask = new DownloadTask<>() {
            @Override
            protected String call() throws Exception {
                String ffmpegZipPath = downloadManager.downloadResource(DEST_FOLDER, this);
                downloadManager.processDownload(ffmpegZipPath);

                return downloadManager.ffmpegBinPath;
            }
        };
        downloadTask.setOnFailed((event) -> {
            ffmpegPath = null;
            ((Stage) rootPane.getScene().getWindow()).close();
        });
        downloadTask.setOnSucceeded((event) -> {
            ffmpegPath = downloadTask.getValue();
            log(Level.INFO, "FFmpeg downloaded; path set to '" + ffmpegPath + "'.");
            ((Stage) rootPane.getScene().getWindow()).close();
        });

        // Set progress bar progress property
        downloadProgressBar.progressProperty().bind(downloadTask.progressProperty());

        // Define a thread to start the download on
        Thread downloadThread = new Thread(downloadTask);
        downloadThread.setDaemon(true);
        downloadThread.start();

        log(Level.INFO, "Showing FFmpeg download view");
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
