/*
 * OSMethods.java
 * Description: Methods involving system operations.
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

package app.auditranscribe.system;

import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;

/**
 * Methods involving system operations.
 */
public final class OSMethods {
    private OSMethods() {
        // Private constructor to signal this is a utility class
    }

    // Public methods


    /**
     * Method that returns the value of the requested environment variable, or the default value if
     * it is not found.
     *
     * @param key        The environment variable whose value we want to query.
     * @param defaultVal The default value if the environment variable is not defined.
     * @return The environment variable's value, or the default value if it is not defined.
     */
    public static String getOrDefault(String key, String defaultVal) {
        String val = System.getenv().get(key);
        return val == null ? defaultVal : val;
    }

    /**
     * Method that gets the operating system of the user.
     *
     * @return An <code>OSType</code> value, representing the operating system of the user.
     */
    @ExcludeFromGeneratedCoverageReport  // Todo: eventually we could consider a fourth OS for testing
    public static OSType getOS() {
        String osName = System.getProperty("os.name").toUpperCase();
        String[] split = osName.split(" ");

        String proposedOSName = split[0];  // First value of the split is (supposedly) the OS name

        // If the proposed OS name is one of the 3 standard ones we are OK
        if (proposedOSName.equals("WINDOWS") || proposedOSName.equals("MAC") || proposedOSName.equals("LINUX")) {
            return OSType.valueOf(proposedOSName);
        } else {
            return OSType.OTHER;
        }
    }
}
