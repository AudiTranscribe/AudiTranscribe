/*
 * ApplicationDirectoryTest.java
 * Description: Test `ApplicationDirectory.java`.
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationDirectoryTest {
    @Test
    @EnabledOnOs({OS.WINDOWS})
    void windowsDataDirectory() {
        String appDir = ApplicationDirectory.getUserDataDirectory("test", "123");
        assertTrue(appDir.contains("AppData"));
        assertTrue(appDir.contains("test\\123"));
    }

    @Test
    @EnabledOnOs({OS.MAC})
    void macOSDataDirectory() {
        String appDir = ApplicationDirectory.getUserDataDirectory("test", "123");
        assertTrue(appDir.contains("Application Support"));
        assertTrue(appDir.contains("test/123"));
    }

    @Test
    @EnabledOnOs({OS.LINUX})
    void linuxDataDirectory() {
        String appDir = ApplicationDirectory.getUserDataDirectory("test", "123");
        assertTrue(appDir.contains("test/123"));
    }

    @Test
    @EnabledOnOs({OS.OTHER})
    void othersDataDirectory() {
        String appDir = ApplicationDirectory.getUserDataDirectory("test", "123");
        assertNull(appDir);
    }
}