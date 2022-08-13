/*
 * MainApplication.java
 *
 * Created on 2022-02-09
 * Updated on 2022-08-13
 *
 * Description: Contains the main application class.
 */

package site.overwrite.auditranscribe;

import javafx.application.Application;
import javafx.stage.Stage;
import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.data_files.DataFiles;
import site.overwrite.auditranscribe.main_views.scene_switching.SceneSwitcher;
import site.overwrite.auditranscribe.misc.MyLogger;
import site.overwrite.auditranscribe.setup_wizard.SetupWizard;

import java.io.IOException;

public class MainApplication extends Application {
    // Initialization method
    @Override
    public void start(Stage stage) throws IOException {
        // Ensure that an application folder exists
        IOMethods.createFolder(IOConstants.APP_DATA_FOLDER_PATH);

        // Clear any old logs
        MyLogger.clearOldLogs(DataFiles.SETTINGS_DATA_FILE.data.logFilePersistence);

        // Run setup wizard
        SetupWizard setupWizard = new SetupWizard();
        setupWizard.showSetupWizard();

        // Start scene handler
        SceneSwitcher sceneHandler = new SceneSwitcher();
        sceneHandler.startHandler();
    }

    public static void main(String[] args) {
        launch();
    }
}
