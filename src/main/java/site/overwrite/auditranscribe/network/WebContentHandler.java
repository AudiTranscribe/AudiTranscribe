/*
 * WebContentHandler.java
 *
 * Created on 2022-07-07
 * Updated on 2022-07-07
 *
 * Description: Methods that handle the reading and processing of content on webpages.
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
public final class WebContentHandler {
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
