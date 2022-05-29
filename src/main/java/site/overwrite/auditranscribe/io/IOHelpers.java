/*
 * IOHelpers.java
 *
 * Created on 2022-05-28
 * Updated on 2022-05-28
 *
 * Description: Helper methods for I/O methods.
 */

package site.overwrite.auditranscribe.io;

/**
 * Helper methods for I/O methods.
 */
public class IOHelpers {
    // Public methods

    /**
     * Method that gets the home directory of the user.
     *
     * @return The home directory of the user.
     */
    public static String getHome() {
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
        String separator = System.getProperty("file.separator");

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
        String separator = System.getProperty("path.separator");

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
