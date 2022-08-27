/*
 * DownloadingFFmpegViewController.java
 *
 * Created on 2022-07-09
 * Updated on 2022-08-27
 *
 * Description: View controller that handles the downloading of FFmpeg.
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
import site.overwrite.auditranscribe.setup_wizard.helpers.FFmpegDownloadManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * View controller that handles the downloading of FFmpeg.
 */
public class DownloadingFFmpegViewController implements Initializable {
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
        CustomTask<String> downloadTask = new CustomTask<>() {
            @Override
            protected String call() throws Exception {
                return downloadManager.downloadFFmpeg(DEST_FOLDER, this);
            }
        };
        downloadTask.setOnFailed((event) -> {
            ffmpegPath = null;
            ((Stage) rootPane.getScene().getWindow()).close();
        });
        downloadTask.setOnSucceeded((event) -> {
            ffmpegPath = downloadTask.getValue();
            ((Stage) rootPane.getScene().getWindow()).close();
        });

        // Set progress bar progress property
        downloadProgressBar.progressProperty().bind(downloadTask.progressProperty());

        // Define a thread to start the download on
        Thread downloadThread = new Thread(downloadTask);
        downloadThread.setDaemon(true);
        downloadThread.start();

        MyLogger.log(Level.INFO, "Showing FFmpeg download view", this.getClass().getName());
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
