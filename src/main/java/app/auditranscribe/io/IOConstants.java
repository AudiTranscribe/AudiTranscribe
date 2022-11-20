/*
 * IOConstants.java
 * Description: Constants involved in the IO methods.
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

package app.auditranscribe.io;

import java.io.File;

/**
 * Constants involved in the IO methods.
 */
public final class IOConstants {
    // Constants
    public static final String SEPARATOR = File.separator;

    public static final String ROOT_ABSOLUTE_PATH = System.getProperty("user.dir");
    public static final String TARGET_FOLDER_ABSOLUTE_PATH = IOMethods.joinPaths(
            ROOT_ABSOLUTE_PATH, "target", "classes"
    );

    public static final String USER_HOME_PATH = System.getProperty("user.home");
    public static final String TEMP_FOLDER_PATH = System.getProperty("java.io.tmpdir");

    public static final String APP_DATA_FOLDER_PATH =
            ApplicationDirectory.getUserDataDirectory("AudiTranscribe", null);
    public static final String PROJECT_BACKUPS_FOLDER_PATH = IOMethods.joinPaths(
            IOConstants.APP_DATA_FOLDER_PATH, "project_backups"
    );
    public static final String OTHER_RESOURCES_DATA_FOLDER_PATH = IOMethods.joinPaths(
            IOConstants.APP_DATA_FOLDER_PATH, "other_resources"
    );

    public static final String RESOURCES_FOLDER_PATH = IOMethods.joinPaths("site", "overwrite", "auditranscribe");

    private IOConstants() {
        // Private constructor to signal this is a utility class
    }
}
