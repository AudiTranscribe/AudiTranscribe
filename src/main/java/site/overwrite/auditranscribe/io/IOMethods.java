/*
 * IOMethods.java
 *
 * Created on 2022-03-15
 * Updated on 2022-06-25
 *
 * Description: Input/Output methods that are used in the AudiTranscribe project.
 */

package site.overwrite.auditranscribe.io;

import site.overwrite.auditranscribe.MainApplication;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Input/Output methods that are used in the AudiTranscribe project.
 */
public class IOMethods {
    // File path handling

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
     * @param shouldTreatPath   Whether this method should treat the path.
     * @return A string representing the absolute path to the file.
     */
    public static String getAbsoluteFilePath(String filePath, boolean shouldTreatPath) {
        String path = getFileURL(filePath).getPath();
        if (shouldTreatPath) path = treatPath(path);
        return path;
    }

    /**
     * Gets the absolute path of a file.<br>
     * This method will automatically treat the file path.
     *
     * @param filePath Path to the file, with respect to the <b>root resource path</b>.
     * @return A string representing the absolute path to the file.
     */
    public static String getAbsoluteFilePath(String filePath) {
        return getAbsoluteFilePath(filePath, true);
    }

    // IO Handling

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
    public static boolean deleteFile(String absolutePath) throws IOException {
        try {
            return Files.deleteIfExists(Paths.get(absolutePath));
        } catch (IOException e) {
            new File(absolutePath).deleteOnExit();
            return false;
        }
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
        createFolder(Paths.get(absolutePath));
    }

    /**
     * Method that creates the AudiTranscribe app data folder, if it doesn't already exist.
     */
    public static void createAppDataFolder() {
        createFolder(Paths.get(IOConstants.APP_DATA_FOLDER_PATH));
    }

    // Path handling

    /**
     * Method that builds a full path from the individual folders.
     *
     * @param elems The individual folders.
     * @return The full path.
     */
    public static String buildPath(String... elems) {
        StringBuilder buffer = new StringBuilder();
        String lastElem = null;
        for (String elem : elems) {
            if (elem == null) {  // Skip any empty/null paths
                continue;
            }

            if (lastElem == null) {  // Handle the first element
                buffer.append(elem);
            } else if (lastElem.endsWith(IOConstants.SEPARATOR)) {  // Check if the last path ends with the separator
                buffer.append(elem.startsWith(IOConstants.SEPARATOR) ? elem.substring(1) : elem);
            } else {
                if (!elem.startsWith(IOConstants.SEPARATOR)) {
                    buffer.append(IOConstants.SEPARATOR);  // Append the separator first...
                }
                buffer.append(elem);                       // ...before appending the element
            }

            // Update the last element
            lastElem = elem;
        }

        return buffer.toString();
    }

    /**
     * Method that joins several paths together.
     *
     * @param paths The paths to join.
     * @return The joined path.
     */
    public static String joinPaths(String... paths) {
        StringBuilder buffer = new StringBuilder();
        for (String path : paths) {
            if (path == null) {  // Skip any empty/null paths
                continue;
            }

            if (buffer.length() > 0) {                  // If there is something in the buffer
                buffer.append(IOConstants.SEPARATOR);   // Append the separator first...
            }
            buffer.append(path);                        // ...before appending the path
        }

        return buffer.toString();
    }

    /**
     * Method that splits the paths.
     *
     * @param paths The paths to split.
     * @return The split paths.
     */
    public static String[] splitPaths(String paths) {
        return paths.split(IOConstants.SEPARATOR);
    }

    /**
     * Method that treats a path so that it is correctly parsed by the operating system.
     *
     * @param path Path to treat.
     * @return Treated path.
     */
    public static String treatPath(String path) {
        if (getOSName().startsWith("WINDOWS")) {
            // If the path starts with something like "/C:" or "\C:", remove the first slash
            if (path.matches("[/\\\\][A-Z]:.*")) {
                path = path.substring(1);
            }

            // Now treat all the escaped characters
            path = path.replace("%20", " ");
        }

        return path;
    }

    // Environment variable management

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

    /**
     * Method that gets the operating system's name, <b>all in uppercase</b>.
     *
     * @return Operating system name in uppercase.
     */
    public static String getOSName() {
        return System.getProperty("os.name").toUpperCase();
    }
}
