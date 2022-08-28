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

package site.overwrite.auditranscribe.utils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * GUI utilities.
 */
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
        // Get the desktop instance
        Desktop desktop = Desktop.getDesktop();

        // Try and browse to the URL
        try {
            desktop.browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
