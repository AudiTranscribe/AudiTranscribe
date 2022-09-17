/*
 * WebContentHandler.java
 * Description: Methods that handle the reading and processing of content on webpages.
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

package site.overwrite.auditranscribe.network;

import site.overwrite.auditranscribe.io.IOMethods;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Methods that handle the reading and processing of content on webpages.
 */
public class WebContentHandler {
    private WebContentHandler() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that reads the content on a webpage and returns it as a string.<br>
     * This assumes that the content is <b>strictly text based</b>.
     * @param url   URL to access.
     * @return  String, representing the content of the webpage.
     * @throws IOException  If the reading of the webpage goes wrong.
     */
    public static String readContentOnWebpage(URL url) throws IOException {
        // Open a connection to the webpage
        URLConnection connection = url.openConnection();

        // Get the input stream for the webpage
        InputStream in = connection.getInputStream();

        // Return web content as string
        return IOMethods.inputStreamToString(in, "UTF-8");
    }
}
