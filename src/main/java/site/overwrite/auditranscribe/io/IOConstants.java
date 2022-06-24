/*
 * IOConstants.java
 *
 * Created on 2022-06-06
 * Updated on 2022-06-24
 *
 * Description: Constants involved in the IO methods.
 */

package site.overwrite.auditranscribe.io;

import java.io.File;
import java.nio.file.Path;

/**
 * Constants involved in the IO methods.
 */
public class IOConstants {
    // Constants
    public static final String SEPARATOR = File.separator;

    public static final String ROOT_ABSOLUTE_PATH_STRING = System.getProperty("user.dir");
    public static final String TEMP_FOLDER = System.getProperty("java.io.tmpdir");

    public static final String APP_DATA_FOLDER_PATH_STRING =
            ApplicationDirectory.getUserDataDirectory("AudiTranscribe", null);
    public static final Path APP_DATA_FOLDER_PATH = Path.of(APP_DATA_FOLDER_PATH_STRING);

    public static final String RESOURCES_FOLDER_PATH_STRING = IOMethods.joinPaths(
            "src", "main", "resources", "site", "overwrite", "auditranscribe"
    );
}
