/*
 * FileUtils.java
 *
 * Created on 2022-03-15
 * Updated on 2022-04-23
 *
 * Description: File utilities.
 */

package site.overwrite.auditranscribe.utils;

import site.overwrite.auditranscribe.MainApplication;

import java.io.*;
import java.net.URL;

/**
 * File utilities.
 */
public class FileUtils {
    // Public methods

    /**
     * Gets a file with respect to the root resource path.
     *
     * @param filePath  Path to the file.
     * @return  A URL representing the absolute path to the file.
     */
    public static URL getFileURL(String filePath) {
        return MainApplication.class.getResource(filePath);
    }

    /**
     * Gets a file with respect to the root resource path.
     *
     * @param filePath  Path to the file.
     * @return  A string representing the absolute path to the file.
     */
    public static String getFilePath(String filePath) {
        return getFileURL(filePath).toString();
    }

    /**
     * Gets the input stream of a file.
     * @param filePath   Path to the file.
     * @return Input stream of the file.
     */
    public static InputStream getInputStream(String filePath) {
        return MainApplication.class.getResourceAsStream(filePath);
    }
}
