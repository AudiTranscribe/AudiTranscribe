/*
 * FFmpegSetupViewController.java
 * Description: Controller for the FFmpeg setup view.
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

import app.auditranscribe.audio.FFmpegHandler;
import app.auditranscribe.audio.exceptions.FFmpegNotFoundException;
import app.auditranscribe.fxml.IconHelper;
import app.auditranscribe.fxml.Popups;
import app.auditranscribe.fxml.Theme;
import app.auditranscribe.fxml.views.AbstractViewController;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import app.auditranscribe.system.OSMethods;
import app.auditranscribe.system.OSType;
import app.auditranscribe.utils.GUIUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * Controller for the FFmpeg setup view.
 */
@ExcludeFromGeneratedCoverageReport
public class FFmpegSetupViewController extends AbstractViewController {
    // Attributes
    private OSType os;

    private boolean isFFmpegInstalled = false;
    private String ffmpegPath = null;

    // FXML elements
    @FXML
    private TabPane rootPane;

    // "Do you have FFmpeg" view
    @FXML
    private Button hasFFmpegButton, doNotHaveFFmpegButton;

    // "Automatic or manual installation" view
    @FXML
    private Button automaticInstallationButton, manualInstallationButton;

    // "FFmpeg installation instructions" view
    @FXML
    private Hyperlink ffmpegHyperlink;

    @FXML
    private Button confirmInstallFFmpegButton;

    // "Specify FFmpeg path" view
    @FXML
    private TextField ffmpegPathTextField;

    @FXML
    private Button selectFFmpegBinaryButton, checkFFmpegPathButton;

    // "Cannot find FFmpeg" view
    @FXML
    private Button specifyManuallyButton, seeInstructionsButton;

    // "Finish FFmpeg setup" view
    @FXML
    private Button continueButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        os = OSMethods.getOS();

        // Add methods to buttons
        hasFFmpegButton.setOnAction(event -> checkFFmpegPath());
        doNotHaveFFmpegButton.setOnAction(event -> {
            if (os != OSType.LINUX && os != OSType.OTHER) {
                switchToTab(FFmpegSetupPaneTab.AUTOMATIC_OR_MANUAL_INSTALLATION);
            } else {
                switchToTab(FFmpegSetupPaneTab.FFMPEG_INSTALLATION_INSTRUCTIONS);
            }
        });

        automaticInstallationButton.setOnAction(event -> {
            ffmpegPath = showDownloadingFFmpegView();
            checkFFmpegPath();
        });
        manualInstallationButton.setOnAction(event -> switchToTab(FFmpegSetupPaneTab.FFMPEG_INSTALLATION_INSTRUCTIONS));

        confirmInstallFFmpegButton.setOnAction(event -> checkFFmpegPath());

        selectFFmpegBinaryButton.setOnAction(event -> {
            // Define file extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                    "FFmpeg binary",
                    "*.exe", "*"
            );

            // Get the file
            File possibleFFmpegBinary = GUIUtils.openFileDialog(rootPane.getScene().getWindow(), extFilter);

            // Check if the FFmpeg binary is valid
            if (possibleFFmpegBinary != null) {
                // Update the value of the FFmpeg path text field
                ffmpegPathTextField.setText(possibleFFmpegBinary.getAbsolutePath());
                log("FFmpeg path set to '" + ffmpegPathTextField.getText() + "'");
            } else {
                Popups.showInformationAlert(rootPane.getScene().getWindow(), "Info", "No file selected.");
            }
        });
        checkFFmpegPathButton.setOnAction(event -> {
            ffmpegPath = ffmpegPathTextField.getText();
            checkFFmpegPath();
        });

        specifyManuallyButton.setOnAction(event -> switchToTab(FFmpegSetupPaneTab.SPECIFY_FFMPEG_PATH));
        seeInstructionsButton.setOnAction(event -> switchToTab(FFmpegSetupPaneTab.FFMPEG_INSTALLATION_INSTRUCTIONS));

        continueButton.setOnAction(event -> {
            isFFmpegInstalled = true;
            ((Stage) rootPane.getScene().getWindow()).close();
        });

        // Add other methods
        ffmpegHyperlink.setOnAction(event -> GUIUtils.openURLInBrowser("https://ffmpeg.org/"));

        log("FFmpeg setup view ready to be shown");
    }

    // Getter/setter methods
    public boolean isFFmpegInstalled() {
        return isFFmpegInstalled;
    }

    public String getFFmpegPath() {
        return ffmpegPath;
    }

    // Public methods
    @Override
    public void setThemeOnScene(Theme theme) {
        updateThemeCSS(rootPane, theme);
        setGraphics(theme);
    }

    // Protected methods
    @Override
    protected void setGraphics(Theme theme) {
        IconHelper.setSVGOnButton(selectFFmpegBinaryButton, 15, 30, "folder-line");
    }

    // Private methods

    /**
     * Helper method that switches to the correct tab.
     *
     * @param tab Tab to switch to.
     */
    private void switchToTab(FFmpegSetupPaneTab tab) {
        rootPane.getSelectionModel().select(tab.ordinal());
    }

    /**
     * Helper method that checks the set <code>ffmpegPath</code> attribute.
     */
    private void checkFFmpegPath() {
        log(Level.FINE, "Checking FFmpeg path");

        // If `ffmpegPath` is null, we try to get it automatically
        if (ffmpegPath == null) {
            try {
                ffmpegPath = FFmpegHandler.getPathToFFmpeg();
                log(Level.FINE, "Obtained FFmpeg path from OS: '" + ffmpegPath + "'");
            } catch (FFmpegNotFoundException e) {
                log(Level.WARNING, "Failed to obtain FFmpeg path from OS");
                switchToTab(FFmpegSetupPaneTab.CANNOT_FIND_FFMPEG);
                return;
            }
        }

        // Otherwise, check if the provided path is valid
        if (FFmpegHandler.checkFFmpegPath(ffmpegPath)) {
            log("Valid FFmpeg path found: '" + ffmpegPath + "'");
            switchToTab(FFmpegSetupPaneTab.FINISH_FFMPEG_SETUP);
        } else {
            log(Level.WARNING, "Path '" + ffmpegPath + "' is not a valid FFmpeg binary");
            switchToTab(FFmpegSetupPaneTab.CANNOT_FIND_FFMPEG);
        }
    }

    /**
     * Helper method that shows the view that handles the downloading of FFmpeg.
     *
     * @return The <b>absolute</b> path to the FFmpeg binary. Returns <code>null</code> if something
     * went wrong.
     */
    private String showDownloadingFFmpegView() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL(
                    "fxml/views/setup-wizard/downloading-ffmpeg-view.fxml"
            ));
            Scene scene = new Scene(fxmlLoader.load());

            DownloadingFFmpegViewController controller = fxmlLoader.getController();
            controller.setThemeOnScene();

            Stage downloadStage = new Stage(StageStyle.UNDECORATED);
            downloadStage.setScene(scene);
            downloadStage.initModality(Modality.APPLICATION_MODAL);

            controller.startDownload();
            downloadStage.showAndWait();

            return controller.getFFmpegPath();
        } catch (IOException ignored) {
        }
        return null;
    }

    // Helper classes
    enum FFmpegSetupPaneTab {
        DO_YOU_HAVE_FFMPEG,
        AUTOMATIC_OR_MANUAL_INSTALLATION,
        FFMPEG_INSTALLATION_INSTRUCTIONS,
        SPECIFY_FFMPEG_PATH,
        CANNOT_FIND_FFMPEG,
        FINISH_FFMPEG_SETUP
    }
}
