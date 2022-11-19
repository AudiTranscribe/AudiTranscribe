/*
 * VersionTest.java
 * Description: Test `Version.java`.
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

package site.overwrite.auditranscribe.misc;

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

        assertEquals(1, new Version("2").compareTo(new Version("1.0.0")));
        assertEquals(0, new Version("1").compareTo(new Version("1.0.0")));
        assertEquals(-1, new Version("1").compareTo(new Version("1.0.1")));
    }
}