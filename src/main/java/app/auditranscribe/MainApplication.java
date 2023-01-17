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

import app.auditranscribe.fxml.views.main.scene_switching.SceneSwitcher;
import app.auditranscribe.io.IOConstants;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.PropertyFile;
import app.auditranscribe.io.data_files.DataFiles;
import app.auditranscribe.io.exceptions.NoSuchPropertyException;
import app.auditranscribe.misc.CustomLogger;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

@ExcludeFromGeneratedCoverageReport
public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Todo: update
//        FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL(
//                "fxml/views/main/homepage-view.fxml"
//        ));
//        Scene scene = new Scene(fxmlLoader.load());
//
//        HomepageViewController controller = fxmlLoader.getController();
//        controller.setThemeOnScene();
//
//        stage.setTitle("AudiTranscribe");
//        stage.setScene(scene);
//        stage.show();

//        FXMLLoader fxmlLoader = new FXMLLoader(IOMethods.getFileURL(
//                "fxml/views/main/transcription-view.fxml"
//        ));
//        Scene scene = new Scene(fxmlLoader.load());
//
//        AbstractViewController controller = fxmlLoader.getController();
//        controller.setThemeOnScene();
//
//        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
//
//        stage.setTitle("AudiTranscribe");
//        stage.setScene(scene);
//        stage.setMaximized(true);
//        stage.setResizable(true);
//        stage.setMinWidth(screenBounds.getWidth());
//        stage.setMinHeight(screenBounds.getHeight());
//        stage.setX(0);
//        stage.setY(0);
//
//        stage.show();
//        controller.finishSetup();

        // Ensure the needed application folders exists
        String[] neededFolders = new String[]{
                IOConstants.APP_DATA_FOLDER_PATH,
                IOConstants.PROJECT_BACKUPS_FOLDER_PATH,
                IOConstants.OTHER_RESOURCES_DATA_FOLDER_PATH
        };
        for (String path : neededFolders) {
            IOMethods.createFolder(path);
        }

        // Clear any old logs
        CustomLogger.clearOldLogs(DataFiles.SETTINGS_DATA_FILE.data.logFilePersistence);

        // Todo: Run setup wizard
//        SetupWizard setupWizard = new SetupWizard();
//        setupWizard.showSetupWizard();

        // Get the current version
        String currentVersion;
        try {
            // Get the project properties file
            PropertyFile projectPropertiesFile = new PropertyFile("project.properties");

            // Update the version label with the version number
            currentVersion = projectPropertiesFile.getProperty("version");
        } catch (IOException | NoSuchPropertyException e) {
            CustomLogger.logException(e);
            throw new RuntimeException(e);
        }

        // Todo: Check if there are any updates
//        UpdateChecker.checkForUpdates(currentVersion);

        // Start scene handler
        SceneSwitcher sceneHandler = new SceneSwitcher(currentVersion);
        sceneHandler.startHandler();
    }

    public static void main(String[] args) {
        launch();
    }
}
