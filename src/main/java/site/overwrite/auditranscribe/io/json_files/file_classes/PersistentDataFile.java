/*
 * PersistentDataFile.java
 *
 * Created on 2022-05-30
 * Updated on 2022-05-30
 *
 * Description: Handles the interactions with the persistent data file.
 */

package site.overwrite.auditranscribe.io.json_files.file_classes;

import site.overwrite.auditranscribe.io.json_files.JSONFile;
import site.overwrite.auditranscribe.io.json_files.data_encapsulators.PersistentData;

/**
 * Handles the interactions with the persistent data file.
 */
public class PersistentDataFile extends JSONFile<PersistentData> {
    /**
     * Initialization method for a new <code>PersistentDataFile</code> object.
     */
    public PersistentDataFile() {
        super("persistent.json", PersistentData.class);
    }
}
