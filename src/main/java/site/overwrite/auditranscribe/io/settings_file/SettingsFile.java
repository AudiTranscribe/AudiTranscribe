/*
 * SettingsFile.java
 *
 * Created on 2022-05-22
 * Updated on 2022-05-22
 *
 * Description: Handles the interactions with the settings file.
 */

package site.overwrite.auditranscribe.io.settings_file;

import com.google.gson.Gson;
import site.overwrite.auditranscribe.io.IOMethods;

import java.io.*;

/**
 * Handles the interactions with the settings file.
 */
public class SettingsFile {
    // Constants
    public static String SETTINGS_FILE_NAME = "settings.json";
    public static String SETTINGS_FILE_PATH = IOMethods.APP_DATA_FOLDER_PATH_STRING + SETTINGS_FILE_NAME;

    // Attributes
    public SettingsData settingsData;

    /**
     * Initialization method for a new <code>SettingsFile</code> object.
     */
    public SettingsFile() {
        // Create the GSON object
        Gson gson = new Gson();

        try (Reader reader = new FileReader(SETTINGS_FILE_PATH)) {
            // Try loading the settings data
            settingsData = gson.fromJson(reader, SettingsData.class);

        } catch (FileNotFoundException e) {
            createNewSettingsFile();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Public methods

    /**
     * Method that saves the current settings data to the settings file.
     */
    public void saveSettingsFile() {
        // Create the GSON object
        Gson gson = new Gson();

        try (Writer writer = new FileWriter(SETTINGS_FILE_PATH)) {
            // Try writing to the settings data file
            gson.toJson(settingsData, writer);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Private methods

    /**
     * Helper method that creates a new settings file.
     */
    private void createNewSettingsFile() {
        // Initialize a new `SettingsData` object with no data inside it
        settingsData = new SettingsData();  // We use the blank data

        // Write that data to the settings file
        saveSettingsFile();
    }

}
