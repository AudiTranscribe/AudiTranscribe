/*
 * SetupWizard.java
 * Description: Handles the setup wizard processes.
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

package app.auditranscribe.fxml.views.setup_wizard;

import app.auditranscribe.fxml.views.setup_wizard.controllers.FFmpegSetupViewController;
import app.auditranscribe.fxml.views.setup_wizard.controllers.InitialViewController;
import app.auditranscribe.fxml.views.setup_wizard.controllers.SetupCompleteViewController;
import app.auditranscribe.fxml.views.setup_wizard.controllers.ThemeSetupViewController;
import app.auditranscribe.generic.LoggableClass;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.data_files.DataFiles;
import app.auditranscribe.io.data_files.data_encapsulators.SettingsData;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Handles the setup wizard processes.
 */
@ExcludeFromGeneratedCoverageReport
public class SetupWizard extends LoggableClass {
    // Attributes
    private final Stage stage;
    private final String currentVersion;

    /**
     * Initializes the setup wizard handler.
     */
    public SetupWizard(String currentVersion) {
        // Update attributes
        this.currentVersion = currentVersion;

        // Set up the stage
        stage = new Stage();
        stage.setTitle("Setup Wizard");
        stage.setResizable(false);
    }

    // Public methods

    /**
     * Method that displays the setup wizard.
     */
    public void showSetupWizard() {
        // Show required views
        showInitialView();
        DataFiles.SETTINGS_DATA_FILE.data.ffmpegInstallationPath = showFFmpegSetupView();
        DataFiles.SETTINGS_DATA_FILE.data.themeEnumOrdinal = showThemeSetupView();
        showSetupCompleteView();

        // Update files' data
        DataFiles.PERSISTENT_DATA_FILE.data.isSetupComplete = true;

        DataFiles.SETTINGS_DATA_FILE.saveFile();
        DataFiles.PERSISTENT_DATA_FILE.saveFile();

        log("AudiTranscribe setup complete");
    }

    // View display methods

    /**
     * Helper method that shows the initial view of the setup wizard.
     */
    private void showInitialView() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getSetupWizardView("initial-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            InitialViewController controller = fxmlLoader.getController();
            controller.setThemeOnScene();
            controller.setCurrentVersion(currentVersion);

            stage.setScene(scene);
            stage.showAndWait();
            controller.removeControllerFromActive();
        } catch (IOException ignored) {
        }
    }

    /**
     * Helper method that shows the FFmpeg setup view.
     *
     * @return The path to the FFmpeg binary.
     */
    private String showFFmpegSetupView() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getSetupWizardView("ffmpeg-setup-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            FFmpegSetupViewController controller = fxmlLoader.getController();
            controller.setThemeOnScene();

            stage.setScene(scene);
            while (!controller.isFFmpegInstalled()) stage.showAndWait();

            controller.removeControllerFromActive();
            return controller.getFFmpegPath();
        } catch (IOException ignored) {
        }
        return null;
    }

    /**
     * Helper method that shows the theme setup view.
     *
     * @return The ordinal for the theme that the user wants.
     */
    private int showThemeSetupView() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getSetupWizardView("theme-setup-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            ThemeSetupViewController controller = fxmlLoader.getController();
            controller.setThemeOnScene();

            stage.setScene(scene);
            stage.showAndWait();

            controller.removeControllerFromActive();
            return controller.getTheme().ordinal();
        } catch (IOException ignored) {
        }
        return SettingsData.THEME_ENUM_ORDINAL;
    }

    /**
     * Helper method that shows the "setup complete" view of the setup wizard.
     */
    private void showSetupCompleteView() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getSetupWizardView("setup-complete-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            SetupCompleteViewController controller = fxmlLoader.getController();
            controller.setThemeOnScene();

            stage.setScene(scene);
            stage.showAndWait();
            controller.removeControllerFromActive();
        } catch (IOException ignored) {
        }
    }

    // Miscellaneous methods

    /**
     * Helper method that gets the URL for a setup wizard view.
     *
     * @param viewFile The name and extension of the view file.
     * @return The URL of the view file.
     */
    private URL getSetupWizardView(String viewFile) {
        return IOMethods.getFileURL("fxml/views/setup-wizard/" + viewFile);
    }
}
