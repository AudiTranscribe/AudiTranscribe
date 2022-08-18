/*
 * PersistentDataFile.java
 *
 * Created on 2022-08-13
 * Updated on 2022-08-13
 *
 * Description: Handles the interactions with the persistent data file.
 */

package site.overwrite.auditranscribe.io.data_files.file_classes;

import site.overwrite.auditranscribe.io.IOConstants;
import site.overwrite.auditranscribe.io.IOMethods;
import site.overwrite.auditranscribe.io.data_files.JSONDataFile;
import site.overwrite.auditranscribe.io.data_files.data_encapsulators.PersistentData;

/**
 * Handles the interactions with the persistent data file.
 */
public class PersistentDataFile extends JSONDataFile<PersistentData> {

    /**
     * Initialization method for a <code>PersistentDataFile</code> object.
     */
    public PersistentDataFile() {
        super(IOMethods.joinPaths(IOConstants.APP_DATA_FOLDER_PATH, "persistent.json"), PersistentData.class);
    }
}