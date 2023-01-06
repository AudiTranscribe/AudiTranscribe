/*
 * PersistentDataFile.java
 * Description: Handles the interactions with the persistent data file.
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

package app.auditranscribe.io.data_files.file_classes;

import app.auditranscribe.io.IOConstants;
import app.auditranscribe.io.IOMethods;
import app.auditranscribe.io.data_files.JSONDataFile;
import app.auditranscribe.io.data_files.data_encapsulators.PersistentData;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;

/**
 * Handles the interactions with the persistent data file.
 */
@ExcludeFromGeneratedCoverageReport
public class PersistentDataFile extends JSONDataFile<PersistentData> {
    /**
     * Initialization method for a <code>PersistentDataFile</code> object.
     */
    public PersistentDataFile() {
        super(IOMethods.joinPaths(IOConstants.APP_DATA_FOLDER_PATH, "persistent.json"), PersistentData.class);
    }
}
