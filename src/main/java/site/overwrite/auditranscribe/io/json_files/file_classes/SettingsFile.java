/*
 * SettingsFile.java
 *
 * Created on 2022-05-22
 * Updated on 2022-05-30
 *
 * Description: Handles the interactions with the settings file.
 */

package site.overwrite.auditranscribe.io.json_files.file_classes;

import site.overwrite.auditranscribe.io.json_files.JSONFile;
import site.overwrite.auditranscribe.io.json_files.data_encapsulators.SettingsData;

/**
 * Handles the interactions with the settings file.
 */
public class SettingsFile extends JSONFile<SettingsData> {
    /**
     * Initialization method for a new <code>SettingsFile</code> object.
     */
    public SettingsFile() {
        super("settings.json", SettingsData.class);
    }
}
