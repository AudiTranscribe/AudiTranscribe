package app.auditranscribe.misc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VersionTest {
    @Test
    void newInstance() {
        assertArrayEquals(new int[]{1, 2, 3}, new Version("1.2.3").numbers);
        assertArrayEquals(new int[]{4, 5, 6}, new Version("4.5.6-debug").numbers);
        assertArrayEquals(new int[]{7, 8, 9}, new Version("7.8.9-beta").numbers);
    }

    @Test
    void compareTo() {
        assertEquals(1, new Version("2.0.0").compareTo(new Version("1.0.0")));
        assertEquals(0, new Version("1.0.0").compareTo(new Version("1.0.0")));
        assertEquals(-1, new Version("1.0.0").compareTo(new Version("2.0.0")));

        assertEquals(1, new Version("1.0.1").compareTo(new Version("1")));
        assertEquals(0, new Version("1.0.0").compareTo(new Version("1")));
        assertEquals(-1, new Version("1.0.0").compareTo(new Version("2")));

        assertEquals(1, new Version("2").compareTo(new Version("1.0.0")));
        assertEquals(0, new Version("1").compareTo(new Version("1.0.0")));
        assertEquals(-1, new Version("1").compareTo(new Version("1.0.1")));
    }
}