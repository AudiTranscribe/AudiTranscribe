/*
 * OSMethods.java
 *
 * Created on 2022-07-03
 * Updated on 2022-07-03
 *
 * Description: Class that contains operating system methods.
 */

package site.overwrite.auditranscribe.system;

public final class OSMethods {
    private OSMethods() {
        // Private constructor to signal this is a utility class
    }

    // Public methods

    /**
     * Method that returns the value of the environment variable or the default value.
     *
     * @param key        The environment variable key.
     * @param defaultVal The default value if the environment variable is not found.
     * @return The value of the environment variable, or the default value.
     */
    public static String getOrDefault(String key, String defaultVal) {
        String val = System.getenv().get(key);
        return val == null ? defaultVal : val;
    }

    /**
     * Method that gets the operating system of the user.
     *
     * @return Operating system enum value.
     */
    public static OSType getOS() {
        // Get the OS name
        String osName = System.getProperty("os.name").toUpperCase();

        // Split by spaces
        String[] split = osName.split(" ");

        // Attempt to get the OS based on the first value
        System.out.println("!!!!!!!!!! " + split[0] + " !!!!!!!!!!");
        try {
            return OSType.valueOf(split[0]);
        } catch (IllegalArgumentException e) {
            // Assume other *nix
            return OSType.LINUX;
        }
    }
}
