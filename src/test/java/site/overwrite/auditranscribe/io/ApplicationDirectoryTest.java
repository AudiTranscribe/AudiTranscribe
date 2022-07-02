/*
 * ApplicationDirectoryTest.java
 *
 * Created on 2022-07-02
 * Updated on 2022-07-02
 *
 * Description: Test `ApplicationDirectory.java`.
 */

package site.overwrite.auditranscribe.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationDirectoryTest {
    @Test
    void getUserDataDirectory() {
        // Generate the path to the application directory
        String appDir = ApplicationDirectory.getUserDataDirectory("test", "123");

        // Get the OS name
        String osName = IOMethods.getOSName();

        // Check the user data directory
        if (osName.startsWith("WINDOWS")) {
            assertTrue(appDir.contains("AppData"));
            assertTrue(appDir.contains("test\\123"));
        } else if (osName.startsWith("MAC OS X")) {
            assertTrue(appDir.contains("Application Support"));
            assertTrue(appDir.contains("test/123"));
        } else {
            assertTrue(appDir.contains("test/123"));
        }
    }
}