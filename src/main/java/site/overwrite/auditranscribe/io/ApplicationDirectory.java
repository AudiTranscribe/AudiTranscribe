/*
 * ApplicationDirectory.java
 *
 * Created on 2022-05-28
 * Updated on 2022-07-03
 *
 * Description: Application directory factory class.
 */

package site.overwrite.auditranscribe.io;

import site.overwrite.auditranscribe.system.OSMethods;
import site.overwrite.auditranscribe.system.OSType;

/**
 * Application directory factory class.
 */
public final class ApplicationDirectory {
    private ApplicationDirectory() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that gets the application data directory of the user.
     *
     * @param appName    Name of the application.
     * @param appVersion The application's version.
     * @return The application data directory of the user.
     */
    public static String getUserDataDirectory(String appName, String appVersion) {
        // Get the operating system
        OSType osType = OSMethods.getOS();

        // Get the user data directory based on the operating system name
        String dataDirPath;
        switch (osType) {
            case WINDOWS ->
                    dataDirPath = IOMethods.buildPath(
                            System.getenv("AppData"), appName, appVersion
                    );
            case MAC ->
                    dataDirPath = IOMethods.buildPath(
                    IOConstants.USER_HOME_PATH, "/Library/Application Support", appName, appVersion
                    );
            default -> {
                // Assume other *nix
                String dir = OSMethods.getOrDefault(
                        "XDG_DATA_HOME", IOMethods.buildPath(
                                IOConstants.USER_HOME_PATH, "/.local/share"
                        )
                );
                dataDirPath = IOMethods.buildPath(dir, appName, appVersion);
            }
        }

        return dataDirPath;
    }
}
