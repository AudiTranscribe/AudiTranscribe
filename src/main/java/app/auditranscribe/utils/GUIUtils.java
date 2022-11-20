/*
 * GUIUtils.java
 * Description: GUI utilities.
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

package app.auditranscribe.utils;

import app.auditranscribe.system.OSMethods;
import app.auditranscribe.system.OSType;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * GUI utilities.
 */
@ExcludeFromGeneratedCoverageReport
public final class GUIUtils {
    private GUIUtils() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that opens the desired URL in the browser.
     *
     * @param url URL (as a string) to open in the browser.
     */
    public static void openURLInBrowser(String url) {
        OSType os = OSMethods.getOS();

        if (os == OSType.WINDOWS || os == OSType.MAC) {
            // Get the desktop instance
            Desktop desktop = Desktop.getDesktop();

            // Try and browse to the URL
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        } else {
            // List of common browsers on Linux
            String[] browsers = {
                    "google-chrome", "firefox", "mozilla", "opera", "epiphany", "konqueror", "netscape", "links", "lynx"
            };

            // Form the shell command to launch browser
            StringBuilder cmd = new StringBuilder();
            for (int i = 0; i < browsers.length; i++) {
                cmd.append(i == 0 ? "" : " || ").append(browsers[i]).append(" \"").append(url).append("\" ");
            }

            // Run the command
            try {
                Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd.toString()});
            } catch (Exception ignored) {
            }
        }
    }

    public static void openFolderInGUI(String folderPath) {
        OSType os = OSMethods.getOS();

        if (os == OSType.WINDOWS || os == OSType.MAC) {
            // Get the desktop instance
            Desktop desktop = Desktop.getDesktop();

            // Try and browse to the URL
            try {
                desktop.open(new File(folderPath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            // Run a command to open the file
            try {
                Runtime.getRuntime().exec(new String[]{"sh", "-c", "/usr/bin/xdg-open '" + folderPath + "'"});
            } catch (Exception ignored) {
            }
        }
    }
}
