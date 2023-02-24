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
        assertEquals("67890", OSMethods.getOrDefault("fake-environment-variable", "67890"));
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