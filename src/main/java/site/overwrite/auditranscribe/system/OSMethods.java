/*
 * OSMethods.java
 * Description: Class that contains operating system methods.
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

package site.overwrite.auditranscribe.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.Scanner;

public final class OSMethods {
    private OSMethods() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that returns the value of the environment variable or the default value.
     *
     * @param key        The environment variable key.
     * @param defaultVal The default value if the environment variable is not found.
     * @return The value of the environment variable, or the default value.
     */
    public static String getOrDefault(String key, String defaultVal) {
        String val = System.getenv().get(key);
        return val == null ? defaultVal : val;
    }

    /**
     * Method that gets the operating system of the user.
     *
     * @return Operating system enum value.
     */
    public static OSType getOS() {
        // Get the OS name
        String osName = System.getProperty("os.name").toUpperCase();

        // Split by spaces
        String[] split = osName.split(" ");

        // Get the first value of the split
        String proposedOSName = split[0];

        // If the proposed OS name is "WINDOWS" or "MAC" we are OK
        if (Objects.equals(proposedOSName, "WINDOWS") || Objects.equals(proposedOSName, "MAC")) {
            return OSType.valueOf(proposedOSName);
        } else {  // More work needed...
            // Check if the "/etc/lsb-release" file exists
            File lsbReleaseFile = new File("/etc/lsb-release");
            try {
                // Check that the LSB release file contains "Ubuntu"
                StringBuilder sb = new StringBuilder();
                Scanner s = new Scanner(new FileInputStream(lsbReleaseFile));
                while (s.hasNext()) {
                    sb.append(s.nextLine());
                }

                if (sb.toString().toUpperCase().contains("UBUNTU")) {
                    return OSType.UBUNTU;
                }
            } catch (FileNotFoundException ignored) {
            }

            // Failure to find the file, or failure to find "UBUNTU" in the file, means that the OS is not Ubuntu
            return OSType.OTHER;
        }
    }
}
