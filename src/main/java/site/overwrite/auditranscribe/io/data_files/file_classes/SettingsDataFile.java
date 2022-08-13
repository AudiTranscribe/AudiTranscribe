/*
 * SettingsDataFile.java
 *
 * Created on 2022-05-22
 * Updated on 2022-08-13
 *
 * Description: Handles the interactions with the settings file.
 */

package site.overwrite.auditranscribe.io.data_files.file_classes;

import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.data_files.JSONDataFile;
import site.overwrite.auditranscribe.io.data_files.data_encapsulators.SettingsData;

/**
 * Handles the interactions with the settings file.
 */
public class SettingsDataFile extends JSONDataFile<SettingsData> {
    /**
     * Initialization method for a new <code>SettingsDataFile</code> object.
     */
    public SettingsDataFile() {
        super(IOMethods.joinPaths(IOConstants.APP_DATA_FOLDER_PATH, "settings.json"), SettingsData.class);
    }
}
