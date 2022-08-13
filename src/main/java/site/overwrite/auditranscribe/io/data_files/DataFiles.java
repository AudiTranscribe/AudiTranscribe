/*
 * DataFiles.java
 *
 * Created on 2022-08-13
 * Updated on 2022-08-13
 *
 * Description: Enum that stores all the data files currently available.
 */

package site.overwrite.auditranscribe.io.data_files;

import site.overwrite.auditranscribe.io.data_files.file_classes.PersistentDataFile;
import site.overwrite.auditranscribe.io.data_files.file_classes.SettingsDataFile;

/**
 * Enum that stores all the data files currently available.
 */
public final class DataFiles {
    // Constants
    public static final PersistentDataFile PERSISTENT_DATA_FILE = new PersistentDataFile();
    public static final SettingsDataFile SETTINGS_DATA_FILE = new SettingsDataFile();

    private DataFiles() {
        // Private constructor to signal this is a utility class
    }
}
