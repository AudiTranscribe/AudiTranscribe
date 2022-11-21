/*
 * OSMethodsTest.java
 * Description: Test `OSMethods.java`.
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

package app.auditranscribe.system;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.*;

class OSMethodsTest {
    @Test
    void getOrDefault() {
        try {
            assertNotEquals("12345", OSMethods.getOrDefault("PATH", "12345"));
        } catch (AssertionFailedError e) {
            assertNotEquals("12345", OSMethods.getOrDefault("Path", "12345"));
        }
        assertEquals("67890", OSMethods.getOrDefault("not-a-real-environment-variable", "67890"));
    }

    @Test
    @EnabledOnOs({OS.WINDOWS})
    void getOSNameWindows() {
        assertEquals(OSType.WINDOWS, OSMethods.getOS());
    }

    @Test
    @EnabledOnOs({OS.MAC})
    void getOSNameMac() {
        assertEquals(OSType.MAC, OSMethods.getOS());
    }

    @Test
    @EnabledOnOs({OS.LINUX})
    void getOSNameLinux() {
        assertEquals(OSType.LINUX, OSMethods.getOS());
    }
}