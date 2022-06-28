/*
 * IOConstants.java
 *
 * Created on 2022-06-06
 * Updated on 2022-06-28
 *
 * Description: Constants involved in the IO methods.
 */

package site.overwrite.auditranscribe.io;

import java.io.File;

/**
 * Constants involved in the IO methods.
 */
public final class IOConstants {
    // Constants
    public static final String SEPARATOR = File.separator;

    public static final String ROOT_ABSOLUTE_PATH = System.getProperty("user.dir");
    public static final String USER_HOME_PATH = System.getProperty("user.home");
    public static final String TEMP_FOLDER_PATH = System.getProperty("java.io.tmpdir");

    public static final String APP_DATA_FOLDER_PATH =
            ApplicationDirectory.getUserDataDirectory("AudiTranscribe", null);

    public static final String RESOURCES_FOLDER_PATH = IOMethods.joinPaths(
            "src", "main", "resources", "site", "overwrite", "auditranscribe"
    );

    private IOConstants() {
        // Private constructor to signal this is a utility class
    }
}
