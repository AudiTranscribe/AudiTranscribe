/*
 * IOMethods.java
 *
 * Created on 2022-03-15
 * Updated on 2022-05-10
 *
 * Description: Input/Output methods that are used in the AudiTranscribe project.
 */

package site.overwrite.auditranscribe.io;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import site.overwrite.auditranscribe.MainApplication;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Input/Output methods that are used in the AudiTranscribe project.
 */
public class IOMethods {
    // Constants
    final static AppDirs APP_DIRS_INSTANCE = AppDirsFactory.getInstance();

    public static final Path APP_DATA_FOLDER_PATH = Path.of(
            APP_DIRS_INSTANCE.getUserDataDir("AudiTranscribe", null, "AudiTranscribe")
    );
    public static final String APP_DATA_FOLDER_PATH_STRING = APP_DATA_FOLDER_PATH + "/";

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
     * Method that creates the AudiTranscribe app data folder, if it doesn't already exist.
     *
     * @return Boolean. Is <code>true</code> is the folder was created and <code>false</code>
     * otherwise.
     */
    public static boolean createAppDataFolder() {
        try {
            // Try to create the app data folder
            Files.createDirectory(APP_DATA_FOLDER_PATH);

            // Return `true` for successful directory creation
            return true;

        } catch (IOException e) {
            return false;
        }
    }
}
