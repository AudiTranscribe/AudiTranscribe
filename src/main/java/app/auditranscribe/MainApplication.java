/*
 * MainApplication.java
 * Description: Contains the main application class.
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

package app.auditranscribe;

import app.auditranscribe.fxml.views.main.SceneSwitcher;
import app.auditranscribe.fxml.views.setup_wizard.SetupWizard;
import app.auditranscribe.io.IOConstants;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.PropertyFile;
import app.auditranscribe.io.data_files.DataFiles;
import app.auditranscribe.misc.CustomLogger;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application class.
 */
@ExcludeFromGeneratedCoverageReport
public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Ensure the needed application folders exists
        String[] neededFolders = new String[]{
                IOConstants.APP_DATA_FOLDER_PATH,
                IOConstants.PROJECT_BACKUPS_FOLDER_PATH,
                IOConstants.OTHER_RESOURCES_DATA_FOLDER_PATH
        };
        for (String path : neededFolders) {
            IOMethods.createFolder(path);
        }

        // Get the current version
        String currentVersion;
        try {
            // Get the project properties file
            PropertyFile projectPropertiesFile = new PropertyFile("project.properties");

            // Update the version label with the version number
            currentVersion = projectPropertiesFile.getProperty("version");
        } catch (IOException | PropertyFile.NoSuchPropertyException e) {
            CustomLogger.logException(e);
            throw new RuntimeException(e);
        }

        // Clear any old logs
        CustomLogger.clearOldLogs(DataFiles.SETTINGS_DATA_FILE.data.logFilePersistence);

        // Run setup wizard if setup is not complete
        if (!DataFiles.PERSISTENT_DATA_FILE.data.isSetupComplete) {
            SetupWizard setupWizard = new SetupWizard(currentVersion);
            setupWizard.showSetupWizard();
        }

        // Check if there are any updates
        UpdateChecker.checkForUpdates(currentVersion);

        // Start scene handler
        SceneSwitcher sceneHandler = new SceneSwitcher(currentVersion);
        sceneHandler.startHandler();
    }

    public static void main(String[] args) {
        launch();
    }
}
