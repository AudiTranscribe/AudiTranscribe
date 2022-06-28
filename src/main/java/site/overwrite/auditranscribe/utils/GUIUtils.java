/*
 * GUIUtils.java
 *
 * Created on 2022-06-28
 * Updated on 2022-06-28
 *
 * Description: GUI utilities.
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
