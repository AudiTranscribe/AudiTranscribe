/*
 * MainApplication.java
 *
 * Created on 2022-02-09
 * Updated on 2022-07-07
 *
 * Description: Contains the main application class.
 */

package site.overwrite.auditranscribe;

import javafx.application.Application;
import javafx.stage.Stage;
import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.json_files.file_classes.SettingsFile;
import site.overwrite.auditranscribe.main_views.scene_switching.SceneSwitcher;
import site.overwrite.auditranscribe.setup_wizard.view_controllers.SetupWizardHelper;

import java.io.IOException;

public class MainApplication extends Application {
    // Initialization method
    @Override
    public void start(Stage stage) throws IOException {
        // Ensure that an application folder exists
        IOMethods.createFolder(IOConstants.APP_DATA_FOLDER_PATH);

        // Update the settings file attribute
        SettingsFile settingsFile = new SettingsFile();

        // Run setup wizard
        SetupWizardHelper setupWizardHelper = new SetupWizardHelper(settingsFile);
        setupWizardHelper.showSetupWizard();

        // Start scene handler
        SceneSwitcher sceneHandler = new SceneSwitcher(settingsFile);
        sceneHandler.startHandler();
    }

    public static void main(String[] args) {
        launch();
    }
}
