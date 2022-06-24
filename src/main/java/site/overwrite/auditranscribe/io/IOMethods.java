/*
 * IOMethods.java
 *
 * Created on 2022-03-15
 * Updated on 2022-06-24
 *
 * Description: Input/Output methods that are used in the AudiTranscribe project.
 */

package site.overwrite.auditranscribe.io;

import site.overwrite.auditranscribe.MainApplication;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Input/Output methods that are used in the AudiTranscribe project.
 */
public class IOMethods {
    // Public methods

    /**
     * Gets a file's URL.
     *
     * @param filePath Path to the file, with respect to the <b>root resource path</b>.
     * @return A URL representing the absolute path to the file.
     */
    public static URL getFileURL(String filePath) {
        return MainApplication.class.getResource(filePath);
    }

    /**
     * Gets a file's URL as a string.
     *
     * @param filePath Path to the file, with respect to the <b>root resource path</b>.
     * @return A string, representing a URL to the file.
     */
    public static String getFileURLAsString(String filePath) {
        return getFileURL(filePath).toString();
    }

    /**
     * Gets the absolute path of a file.
     *
     * @param filePath Path to the file, with respect to the <b>root resource path</b>.
     * @return A string representing the absolute path to the file.
     */
    public static String getAbsoluteFilePath(String filePath) {
        return getFileURL(filePath).getPath();
    }

    /**
     * Gets the input stream of a file, with respect to the <b>root resource path</b>.
     *
     * @param filePath Path to the file, with respect to the <b>root resource path</b>.
     * @return Input stream of the file.
     */
    public static InputStream getInputStream(String filePath) {
        return MainApplication.class.getResourceAsStream(filePath);
    }

    /**
     * Method that creates a file at the specified <code>absolutePath</code>.
     *
     * @param absolutePath Absolute path to the file.
     * @return An integer.
     * <ul>
     *     <li>Returns <code>0</code> if the file was successfully created.</li>
     *     <li>Returns <code>1</code> if the file already exists.</li>
     *     <li>Returns <code>-1</code> if the file failed to be created (<em>and does not already
     *     exist</em>).</li>
     * </ul>
     */
    public static int createFile(String absolutePath) {
        try {
            File myFile = new File(absolutePath);
            if (myFile.createNewFile()) {
                return 0;  // Success
            } else {
                return 1;  // File already exists
            }
        } catch (IOException e) {
            return -1;  // Failed to create file
        }
    }

    /**
     * Method that deletes a file at the specified <code>absolutePath</code>.
     *
     * @param absolutePath Absolute path to the file.
     * @return Boolean. Is <code>true</code> is the file was deleted and <code>false</code>
     * otherwise.
     */
    public static boolean deleteFile(String absolutePath) {
        File myFile = new File(absolutePath);
        return myFile.delete();
    }

    /**
     * Method that creates a folder, if it does not already exist, at the specified
     * <code>absolutePath</code>.
     *
     * @param absolutePath Absolute path to the folder.
     */
    public static void createFolder(Path absolutePath) {
        try {
            // Try to create the folder
            Files.createDirectory(absolutePath);

        } catch (IOException ignored) {
        }
    }

    /**
     * Method that creates a folder, if it does not already exist, at the specified
     * <code>absolutePath</code>.
     *
     * @param absolutePath Absolute path to the folder.
     */
    public static void createFolder(String absolutePath) {
        createFolder(Path.of(absolutePath));
    }

    /**
     * Method that creates the AudiTranscribe app data folder, if it doesn't already exist.
     */
    public static void createAppDataFolder() {
        createFolder(IOConstants.APP_DATA_FOLDER_PATH);
    }

    /**
     * Method that gets the home directory of the user.
     *
     * @return The home directory of the user.
     */
    public static String getHomeDir() {
        return System.getProperty("user.home");
    }

    /**
     * Method that builds a full path from the individual folders.
     *
     * @param elems The individual folders.
     * @return The full path.
     */
    public static String buildPath(String... elems) {
        // Get the file separator
        String separator = IOConstants.SEPARATOR;

        // Build the path
        StringBuilder buffer = new StringBuilder();
        String lastElem = null;
        for (String elem : elems) {
            if (elem == null) {  // Skip any empty/null paths
                continue;
            }

            if (lastElem == null) {  // Handle the first element
                buffer.append(elem);
            } else if (lastElem.endsWith(separator)) {  // Check if the last path ends with the separator
                buffer.append(elem.startsWith(separator) ? elem.substring(1) : elem);
            } else {
                if (!elem.startsWith(separator)) {
                    buffer.append(separator);  // Append the separator first...
                }
                buffer.append(elem);           // ...before appending the element
            }

            // Update the last element
            lastElem = elem;
        }

        // Return the built path
        return buffer.toString();
    }

    /**
     * Method that joins several paths together.
     *
     * @param paths The paths to join.
     * @return The joined path.
     */
    public static String joinPaths(String... paths) {
        // Get the file separator
        String separator = IOConstants.SEPARATOR;

        // Build the joined path
        StringBuilder buffer = new StringBuilder();
        for (String path : paths) {
            if (path == null) {  // Skip any empty/null paths
                continue;
            }

            if (buffer.length() > 0) {      // If there is something in the buffer
                buffer.append(separator);   // Append the separator first...
            }
            buffer.append(path);            // ...before appending the path
        }

        // Return the joined path
        return buffer.toString();
    }

    /**
     * Method that splits the paths.
     *
     * @param paths The paths to split.
     * @return The split paths.
     */
    public static String[] splitPaths(String paths) {
        // Split the paths by the path separator
        String separator = System.getProperty("path.separator");
        return paths.split(separator);
    }

    /**
     * Method that returns the value of the environment variable or the default value.
     *
     * @param key The environment variable key.
     * @param def The default value.
     * @return The value of the environment variable or the default value.
     */
    public static String getOrDefault(String key, String def) {
        String val = System.getenv().get(key);
        return val == null ? def : val;
    }
}
