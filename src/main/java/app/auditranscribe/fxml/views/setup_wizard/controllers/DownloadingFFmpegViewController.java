/*
 * DownloadingFFmpegViewController.java
 * Description: Controller that displays the progress of the FFmpeg download.
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

package app.auditranscribe.fxml.views.setup_wizard.controllers;

import app.auditranscribe.fxml.Theme;
import app.auditranscribe.fxml.views.AbstractViewController;
import app.auditranscribe.fxml.views.setup_wizard.download_handlers.FFmpegDownloadManager;
import app.auditranscribe.io.IOConstants;
import app.auditranscribe.network.DownloadTask;
import app.auditranscribe.utils.MathUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller that displays the progress of the FFmpeg download.
 */
public class DownloadingFFmpegViewController extends AbstractViewController {
    // Constants
    private final String DEST_FOLDER = IOConstants.APP_DATA_FOLDER_PATH;

    // Attributes
    FFmpegDownloadManager downloadManager;
    String ffmpegPath;

    // FXML Elements
    @FXML
    private VBox rootPane;

    @FXML
    private ProgressBar downloadProgressBar;

    @FXML
    private Label currDownloadAmountLabel, fileSizeLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        downloadManager = new FFmpegDownloadManager(3);
        log("Download FFmpeg view ready to be shown");
    }

    // Getter/setter methods

    /**
     * Method that gets the <b>absolute</b> path to the FFmpeg binary.
     *
     * @return <b>Absolute</b> path to the FFmpeg binary. Returns <code>null</code> if the
     * downloading fails.
     */
    public String getFFmpegPath() {
        return ffmpegPath;
    }

    // Public methods
    @Override
    public void setThemeOnScene(Theme theme) {
        updateThemeCSS(rootPane, theme);
        setGraphics(theme);
    }

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
            logException((Exception) downloadTask.getException());
            ((Stage) rootPane.getScene().getWindow()).close();
        });
        downloadTask.setOnSucceeded((event) -> {
            ffmpegPath = downloadTask.getValue();
            log("FFmpeg downloaded; path set to '" + ffmpegPath + "'");
            ((Stage) rootPane.getScene().getWindow()).close();
        });

        // Set progress bar progress property
        downloadProgressBar.progressProperty().bind(downloadTask.progressProperty());

        // Make the labels update once the downloaded amount changes
        downloadTask.downloadedAmountProperty().addListener((obs, oldVal, newVal) -> {
            double downloadedAmtInKB = MathUtils.round(newVal.doubleValue() / 1e6, 2);
            double fileSizeInKB = MathUtils.round(downloadTask.getDownloadFileSize() / 1e6, 2);

            currDownloadAmountLabel.setText(String.valueOf(downloadedAmtInKB));
            fileSizeLabel.setText(String.valueOf(fileSizeInKB));
        });

        // Define a thread to start the download on
        Thread downloadThread = new Thread(downloadTask);
        downloadThread.setDaemon(true);
        downloadThread.start();

        log("Starting FFmpeg download");
    }

    // Protected methods
    @Override
    protected void setGraphics(Theme theme) {
        // No graphics to set
    }
}
