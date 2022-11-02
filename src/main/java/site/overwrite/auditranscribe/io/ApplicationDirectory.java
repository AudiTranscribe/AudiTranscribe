/*
 * ApplicationDirectory.java
 * Description: Application directory factory class.
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

package site.overwrite.auditranscribe.io;

import site.overwrite.auditranscribe.misc.IgnoredFromCoverage;
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
    @IgnoredFromCoverage
    public static String getUserDataDirectory(String appName, String appVersion) {
        // Get the operating system
        OSType osType = OSMethods.getOS();

        // Get the user data directory based on the operating system name
        return switch (osType) {
            case WINDOWS -> IOMethods.buildPath(
                    System.getenv("AppData"), appName, appVersion
            );
            case MAC -> IOMethods.buildPath(
                    IOConstants.USER_HOME_PATH, "/Library/Application Support", appName, appVersion
            );
            default -> IOMethods.buildPath(
                    OSMethods.getOrDefault(
                            "XDG_DATA_HOME",
                            IOMethods.buildPath(
                                    IOConstants.USER_HOME_PATH, "/.local/share"
                            )
                    ),
                    appName,
                    appVersion
            );
        };
    }
}
