/*
 * ApplicationDirectory.java
 *
 * Created on 2022-05-28
 * Updated on 2022-07-02
 *
 * Description: Application directory factory class.
 */

package site.overwrite.auditranscribe.io;

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
        // Get the operating system's name
        String osName = IOMethods.getOSName();

        // Get the user data directory based on the operating system name
        if (osName.startsWith("MAC OS X")) {
            return IOMethods.buildPath(
                    IOConstants.USER_HOME_PATH, "/Library/Application Support", appName, appVersion
            );
        } else if (osName.startsWith("WINDOWS")) {
            return IOMethods.buildPath(
                    System.getenv("AppData"), appName, appVersion
            );
        } else {
            // Assume other *nix
            String dir = IOMethods.getOrDefault(
                    "XDG_DATA_HOME", IOMethods.buildPath(
                            IOConstants.USER_HOME_PATH, "/.local/share"
                    )
            );
            return IOMethods.buildPath(dir, appName, appVersion);
        }
    }
}
