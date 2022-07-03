/*
 * ApplicationDirectoryTest.java
 *
 * Created on 2022-07-02
 * Updated on 2022-07-03
 *
 * Description: Test `ApplicationDirectory.java`.
 */

package site.overwrite.auditranscribe.io;

import org.junit.jupiter.api.Test;
import site.overwrite.auditranscribe.system.OSMethods;
import site.overwrite.auditranscribe.system.OSType;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationDirectoryTest {
    @Test
    void getUserDataDirectory() {
        // Generate the path to the application directory
        String appDir = ApplicationDirectory.getUserDataDirectory("test", "123");

        // Get the OS
        OSType osType = OSMethods.getOS();

        // Check the user data directory
        if (osType == OSType.WINDOWS) {
            assertTrue(appDir.contains("AppData"));
            assertTrue(appDir.contains("test\\123"));
        } else if (osType == OSType.MAC) {
            assertTrue(appDir.contains("Application Support"));
            assertTrue(appDir.contains("test/123"));
        } else {
            assertTrue(appDir.contains("test/123"));
        }
    }
}