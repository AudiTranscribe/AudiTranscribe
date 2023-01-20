/*
 * DataFiles.java
 * Description: Enum that stores all the data files currently available.
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

package app.auditranscribe.io.data_files;

import app.auditranscribe.io.data_files.file_classes.PersistentDataFile;
import app.auditranscribe.io.data_files.file_classes.SettingsDataFile;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;

/**
 * Enum that stores all the data files currently available.
 */
@ExcludeFromGeneratedCoverageReport
public final class DataFiles {
    // Constants
    public static final PersistentDataFile PERSISTENT_DATA_FILE = new PersistentDataFile();
    public static final SettingsDataFile SETTINGS_DATA_FILE = new SettingsDataFile();

    private DataFiles() {
        // Private constructor to signal this is a utility class
    }
}
