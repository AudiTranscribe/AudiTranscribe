/*
 * IOMethods.java
 * Description: Input/Output handling methods.
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

package app.auditranscribe.io;

import app.auditranscribe.MainApplication;
import app.auditranscribe.misc.CustomLogger;
import app.auditranscribe.misc.ExcludeFromGeneratedCoverageReport;
import app.auditranscribe.system.OSMethods;
import app.auditranscribe.system.OSType;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Input/Output handling methods.
 */
public final class IOMethods {
    private IOMethods() {
        // Private constructor to signal this is a utility class
    }

    // File path methods

    /**
     * Method that gets the application data directory path.
     *
     * @return The <b>absolute</b> path to the application data directory.
     */
    @ExcludeFromGeneratedCoverageReport
    public static String getApplicationDataDirectory() {
        // Get the operating system
        OSType osType = OSMethods.getOS();

        // Get the user data directory based on the operating system name
        return switch (osType) {
            case WINDOWS -> IOMethods.joinPaths(
                    true,
                    System.getenv("AppData"), "AudiTranscribe"
            );
            case MAC -> IOMethods.joinPaths(
                    true,
                    IOConstants.USER_HOME_PATH, "/Library/Application Support", "AudiTranscribe"
            );
            default -> IOMethods.joinPaths(
                    true,
                    OSMethods.getOrDefault(
                            "XDG_DATA_HOME",
                            IOMethods.joinPaths(
                                    true,
                                    IOConstants.USER_HOME_PATH, "/.local/share"
                            )
                    ),
                    "AudiTranscribe"
            );
        };
    }

    /**
     * Gets a file's URL.
     *
     * @param filePath Path to the file, with respect to the <b>resource path</b>.
     * @return A URL representing the absolute path to the file (which is in the target folder).
     */
    public static URL getFileURL(String filePath) {
        URL url = MainApplication.class.getResource(filePath);
        if (url == null) {
            CustomLogger.log(
                    Level.WARNING,
                    "No file found at '" + filePath + "'; returning null URL",
                    IOMethods.class.getName()
            );
        }
        return url;
    }

    /**
     * Gets a file's URL as a string.
     *
     * @param filePath Path to the file, with respect to the <b>resource path</b>.
     * @return A string, representing a URL to the file (which is in the target folder).
     */
    public static String getFileURLAsString(String filePath) {
        try {
            return getFileURL(filePath).toString();
        } catch (NullPointerException e) {
            throw new RuntimeException(
                    new FileNotFoundException("No file was found at the resource path '" + filePath + "'.")
            );
        }
    }

    /**
     * Gets the absolute path of a file.
     *
     * @param filePath Path to the file, with respect to the <b>resource path</b>.
     * @return A string representing the <b>absolute path</b> to the file (which is in the target
     * folder).
     */
    public static String getAbsoluteFilePath(String filePath) {
        String path;
        try {
            path = getFileURL(filePath).getPath();
        } catch (NullPointerException e) {
            throw new NullPointerException("Could not find file at '" + filePath + "'.");
        }
        path = treatPath(path);
        return path;
    }

    // CRUD operations

    /**
     * Method that creates a file at the specified <code>path</code>.
     *
     * @param path The <b>absolute path</b> to the file.
     * @return Returns:
     * <ul>
     *     <li><code>0</code> if the file was successfully created;</li>
     *     <li><code>1</code> if the file already exists; or</li>
     *     <li><code>-1</code> if the file failed to be created (<em>and does not already
     *     exist</em>).</li>
     * </ul>
     */
    public static int createFile(String path) {
        return createFile(new File(path));
    }

    /**
     * Method that creates a file specified by a <code>File</code> object.
     *
     * @param file A <code>File</code> object that contains the absolute path to the file to create.
     * @return Returns:
     * <ul>
     *     <li><code>0</code> if the file was successfully created;</li>
     *     <li><code>1</code> if the file already exists; or</li>
     *     <li><code>-1</code> if the file failed to be created (<em>and does not already
     *     exist</em>).</li>
     * </ul>
     */
    public static int createFile(File file) {
        try {
            return file.createNewFile() ? 0 : 1;
        } catch (IOException e) {
            return -1;
        }
    }


    /**
     * Method that creates a folder, if it does not already exist.<br>
     * This method will also create any parent directories that does not already exist.
     *
     * @param folder File object representing the folder to create.
     * @return A boolean. Returns <code>true</code> if folder was created successfully, and
     * <code>false</code> otherwise.
     */
    public static boolean createFolder(File folder) {
        return createFolder(folder.getAbsolutePath());
    }

    /**
     * Method that creates a folder, if it does not already exist, at the specified
     * <code>absolutePath</code>.<br>
     * This method will also create any parent directories that does not already exist.
     *
     * @param absolutePath <b>Absolute path</b> to the folder.
     * @return A boolean. Returns <code>true</code> if folder was created successfully, and
     * <code>false</code> otherwise.
     */
    public static boolean createFolder(String absolutePath) {
        try {
            Files.createDirectory(Paths.get(absolutePath));
            return true;
        } catch (IOException e) {
            return new File(absolutePath).mkdirs();
        }
    }

    /**
     * Method that copies the contents of the original file into a new file.
     *
     * @param originalFilePath Original file's <b>absolute</b >path.
     * @param newFilePath      New file's <b>absolute</b> path.
     * @return A boolean. Returns <code>true</code> if the copy is successful and <code>false</code>
     * otherwise.
     */
    public static boolean copyFile(String originalFilePath, String newFilePath) {
        // Convert the given strings into `Path` objects
        Path origPath = Paths.get(originalFilePath);
        Path copyPath = Paths.get(newFilePath);

        // Properly copy the file
        try {
            Files.copy(origPath, copyPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Given a file's path (with respect to the <b>resource path</b>), will return an
     * <code>InputStream</code> for reading.
     *
     * @param filePath Path to the file, with respect to the <b>resource path</b>.
     * @return An <code>InputStream</code> object for reading.
     */
    public static InputStream readAsInputStream(String filePath) {
        // If the file path contains backslashes, replace with forward slashes
        filePath = filePath.replaceAll("\\\\", "/");
        return MainApplication.class.getResourceAsStream(filePath);
    }

    /**
     * Given a file's path (with respect to the <b>resource path</b>), will return a string,
     * representing the contents of the file.
     *
     * @param filePath Path to the file, with respect to the <b>resource path</b>.
     * @param encoding Encoding to use (e.g. <code>UTF-8</code>).
     * @return Contents of the file as a string.
     * @throws IOException If the encoding format is not recognized, or if something went wrong when
     *                     reading the input stream.
     */
    public static String readAsString(String filePath, String encoding) throws IOException {
        return inputStreamToString(readAsInputStream(filePath), encoding);
    }

    /**
     * Method that deletes a file or <b>empty</b> folder given a <code>File</code> object.
     *
     * @param file The <code>File</code> object, pointing to the location to delete.
     * @return A boolean; <code>true</code> is the file or folder was deleted and
     * <code>false</code> otherwise.
     */
    public static boolean delete(File file) {
        return delete(file.getAbsolutePath());
    }

    /**
     * Method that deletes a file or <b>empty</b> folder at the specified <code>absolutePath</code>.
     *
     * @param absolutePath <b>Absolute path</b> to the file or <b>empty</b> folder.
     * @return A boolean; is <code>true</code> is the file or folder was deleted and
     * <code>false</code> otherwise.
     */
    public static boolean delete(String absolutePath) {
        try {
            return Files.deleteIfExists(Paths.get(absolutePath));
        } catch (IOException e) {
            new File(absolutePath).deleteOnExit();
            return false;
        }
    }

    // File location handling

    /**
     * Method that checks if a file or folder is present at the specified absolute path.
     *
     * @param absolutePath <b>Absolute path</b> to determine if a file or folder exists there.
     * @return Boolean. Returns <code>true</code> if the file or folder exists at the specified
     * path, and <code>false</code> otherwise.
     */
    public static boolean isSomethingAt(String absolutePath) {
        if (absolutePath == null) return false;
        return (new File(absolutePath)).exists();
    }

    /**
     * Method that moves the file to a new location.
     *
     * @param originalAbsolutePath Original <b>absolute</b> path of the file.
     * @param newAbsolutePath      New <b>absolute</b> path of the file.
     * @throws IOException If movement of the file fails.
     */
    public static void moveFile(String originalAbsolutePath, String newAbsolutePath) throws IOException {
        // Check if the file exists first
        File origFile = new File(originalAbsolutePath);

        if (!origFile.exists()) {
            throw new IOException("A file does not exist at '" + originalAbsolutePath + "'.");
        }

        // Rename file to the new absolute path
        boolean success = origFile.renameTo(new File(newAbsolutePath));

        if (!success) {
            throw new IOException("Movement to the new location '" + newAbsolutePath + "' failed.");
        }
    }

    // Path handling

    /**
     * Method that joins several paths together.
     *
     * @param useOSSeparator Whether to use the native OS separator to join the paths, or to use the
     *                       UNIX separator of `/` to join paths.
     * @param paths          The paths to join.
     * @return The joined path.
     */
    public static String joinPaths(boolean useOSSeparator, String... paths) {
        // Determine the separator character to use
        String separator = "/";
        if (useOSSeparator) separator = IOConstants.SEPARATOR;

        // Build the path
        StringBuilder buffer = new StringBuilder();
        String prevElem = null;
        for (String path : paths) {
            if (path == null || path.equals("")) {
                continue;  // Empty paths can be skipped
            }

            if (prevElem == null) {  // Handle the first element
                buffer.append(path);
            } else if (prevElem.endsWith(separator)) {  // Check if the previous path ended with the separator
                buffer.append(path.startsWith(separator) ? path.substring(separator.length()) : path);
            } else {
                if (!path.startsWith(separator)) {  // If it does not start with a separator,
                    buffer.append(separator);       // append the separator first...
                }
                buffer.append(path);                // ...before appending the element
            }

            // Update the previous path
            prevElem = path;
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
        return joinPaths(false, paths);
    }

    /**
     * Method that treats a path so that it is correctly parsed by the operating system.
     *
     * @param path Path to treat.
     * @return Treated path.
     */
    public static String treatPath(String path) {
        // If the path starts with something like "/C:" or "\C:", we know we are on Windows
        if (path.matches("[/\\\\][A-Z]:.*")) {
            // Remove the first slash
            path = path.substring(1);

            // Now treat all the escaped characters with percentage signs
            String finalPath = path;

            Pattern pattern = Pattern.compile("%(?<seq>[\\da-fA-F]{2})");
            Matcher matcher = pattern.matcher(finalPath);
            while (matcher.find()) {
                // Get the sequence to convert
                String sequence = matcher.group("seq");

                // Replace the sequence with the character
                finalPath = matcher.replaceFirst(Character.toString((char) Integer.parseInt(sequence, 16)));

                // Attempt to find next match
                matcher = pattern.matcher(finalPath);
            }

            return finalPath;
        } else {
            return path;  // No treatment necessary on other systems
        }
    }

    // Miscellaneous methods

    /**
     * Method that converts the bytes passed by an input stream <code>in</code> to a string with the
     * specified encoding.
     *
     * @param in       Input stream.
     * @param encoding Encoding to use (e.g. <code>UTF-8</code>).
     * @return String representation of the input stream.
     * @throws IOException If the encoding format is not recognized, or if something went wrong when
     *                     reading the input stream.
     */
    public static String inputStreamToString(InputStream in, String encoding) throws IOException {
        // Define a buffer for writing the output to
        byte[] buff = new byte[8192];

        // Write input stream to the output stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len;
        while ((len = in.read(buff)) != -1) {
            out.write(buff, 0, len);
        }

        // Convert the output stream bytes into a string by using the encoding
        return out.toString(encoding);
    }

    /**
     * Method that gets the number of files/folders in the specified directory.
     *
     * @param dirPath The <b>absolute</b> path to the directory.
     * @return An integer, representing the number of files/folders in the directory. Returns
     * <code>-1</code> if the directory does not exist.<br>
     * <b>Note: this ignores any <code>.DS_Store</code> that may be present in the directory</b>.
     */
    public static int numThingsInDir(String dirPath) {
        if (isSomethingAt(dirPath)) {
            int numItems = Objects.requireNonNull(new File(dirPath).list()).length;
            if (isSomethingAt(joinPaths(dirPath, ".DS_Store"))) return numItems - 1;
            return numItems;
        }
        return -1;
    }
}
